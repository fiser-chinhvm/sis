/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.referencing.operation.transform;

import java.io.Serializable;
import javax.measure.unit.Unit;
import javax.measure.quantity.Length;
import javax.measure.converter.UnitConverter;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.TransformException;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.referencing.datum.DefaultEllipsoid;
import org.apache.sis.internal.referencing.provider.Molodensky;
import org.apache.sis.internal.referencing.provider.MapProjection;
import org.apache.sis.internal.util.Numerics;
import org.apache.sis.parameter.Parameters;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.ComparisonMode;
import org.apache.sis.util.Debug;

import static java.lang.Math.*;


/**
 * Implementation of Molodensky formulas. This class is used by
 *
 * <ul>
 *   <li>The "real" {@link MolodenskyTransform} (see that class for documentation about Molodensky transform).</li>
 *   <li>{@link InterpolatedGeocentricTransform}, which conceptually works on geocentric coordinates but
 *       is implemented in Apache SIS using Molodensky (never abridged) formulas for performance reasons.
 *       However this implementation choice should be hidden to users (except by mention in javadoc).</li>
 * </ul>
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.7
 * @version 0.7
 * @module
 */
abstract class MolodenskyFormula extends AbstractMathTransform implements Serializable {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = 7684676923384073055L;

    /**
     * The value of 1/sin(1″) multiplied by the conversion factor from arc-seconds to radians (π/180)/(60⋅60).
     * This is the final multiplication factor for Δλ and Δφ.
     */
    static final double ANGULAR_SCALE = 1.00000000000391744;

    /**
     * {@code true} if the source coordinates have a height.
     */
    final boolean isSource3D;

    /**
     * {@code true} if the target coordinates have a height.
     */
    final boolean isTarget3D;

    /**
     * {@code true} for the abridged formula, or {@code false} for the complete one.
     */
    final boolean isAbridged;

    /**
     * Shift along the geocentric X axis (toward prime meridian)
     * in units of the semi-major axis of the source ellipsoid.
     *
     * @see org.apache.sis.referencing.datum.BursaWolfParameters#tX
     */
    protected final double tX;

    /**
     * Shift along the geocentric Y axis (toward 90°E)
     * in units of the semi-major axis of the source ellipsoid.
     *
     * @see org.apache.sis.referencing.datum.BursaWolfParameters#tY
     */
    protected final double tY;

    /**
     * Shift along the geocentric Z axis (toward north pole)
     * in units of the semi-major axis of the source ellipsoid.
     *
     * @see org.apache.sis.referencing.datum.BursaWolfParameters#tZ
     */
    protected final double tZ;

    /**
     * Difference in the semi-major axes of the target and source ellipsoids: {@code Δa = target a - source a}.
     *
     * @see DefaultEllipsoid#semiMajorAxisDifference(Ellipsoid)
     */
    final double Δa;

    /**
     * Difference between the flattening of the target and source ellipsoids (Δf), opportunistically modified
     * with additional terms. The value depends on whether this Molodensky transform is abridged or not:
     *
     * <ul>
     *   <li>For Molodensky, this field is set to (b⋅Δf).</li>
     *   <li>For Abridged Molodensky, this field is set to (a⋅Δf) + (f⋅Δa).</li>
     * </ul>
     *
     * where Δf = <var>target flattening</var> - <var>source flattening</var>.
     */
    final double Δfmod;

    /**
     * Semi-major axis length (<var>a</var>) of the source ellipsoid.
     */
    protected final double semiMajor;

    /**
     * The square of eccentricity of the source ellipsoid.
     * This can be computed by ℯ² = (a²-b²)/a² where
     * <var>a</var> is the <cite>semi-major</cite> axis length and
     * <var>b</var> is the <cite>semi-minor</cite> axis length.
     *
     * @see DefaultEllipsoid#getEccentricitySquared()
     */
    protected final double eccentricitySquared;

    /**
     * The parameters used for creating this conversion.
     * They are used for formatting <cite>Well Known Text</cite> (WKT) and error messages.
     *
     * @see #getContextualParameters()
     */
    final ContextualParameters context;

    /**
     * Creates a Molodensky transform from the specified parameters.
     *
     * @param source      The source ellipsoid.
     * @param isSource3D  {@code true} if the source coordinates have a height.
     * @param target      The target ellipsoid.
     * @param isTarget3D  {@code true} if the target coordinates have a height.
     * @param tX          The geocentric <var>X</var> translation in same units than the source ellipsoid axes.
     * @param tY          The geocentric <var>Y</var> translation in same units than the source ellipsoid axes.
     * @param tZ          The geocentric <var>Z</var> translation in same units than the source ellipsoid axes.
     * @param isAbridged  {@code true} for the abridged formula, or {@code false} for the complete one.
     * @param descriptor  The contextual parameter descriptor.
     */
    @SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
    MolodenskyFormula(final Ellipsoid source, final boolean isSource3D,
                      final Ellipsoid target, final boolean isTarget3D,
                      final double tX, final double tY, final double tZ,
                      final boolean isAbridged, final boolean setAxisLengths,
                      final ParameterDescriptorGroup descriptor)
    {
        ArgumentChecks.ensureNonNull("source", source);
        ArgumentChecks.ensureNonNull("target", target);
        final DefaultEllipsoid src = DefaultEllipsoid.castOrCopy(source);
        this.isSource3D = isSource3D;
        this.isTarget3D = isTarget3D;
        this.isAbridged = isAbridged;
        this.semiMajor  = src.getSemiMajorAxis();
        this.Δa         = src.semiMajorAxisDifference(target);
        this.tX         = tX;
        this.tY         = tY;
        this.tZ         = tZ;

        final double semiMinor = src.getSemiMinorAxis();
        final double Δf = src.flatteningDifference(target);
        eccentricitySquared = src.getEccentricitySquared();
        Δfmod = isAbridged ? (semiMajor * Δf) + (semiMajor - semiMinor) * (Δa / semiMajor)
                           : (semiMinor * Δf);
        /*
         * Copy parameters to the ContextualParameter. Those parameters are not used directly
         * by MolodenskyTransform, but we need to store them in case the user asks for them.
         */
        final Unit<Length> unit = src.getAxisUnit();
        final UnitConverter c = target.getAxisUnit().getConverterTo(unit);
        context = new ContextualParameters(descriptor, isSource3D ? 4 : 3, isTarget3D ? 4 : 3);
        setContextualParameters(context, unit, Δf);
        if (setAxisLengths) {
            context.getOrCreate(Molodensky.SRC_SEMI_MAJOR).setValue(semiMajor, unit);
            context.getOrCreate(Molodensky.SRC_SEMI_MINOR).setValue(semiMinor, unit);
            context.getOrCreate(Molodensky.TGT_SEMI_MAJOR).setValue(c.convert(target.getSemiMajorAxis()), unit);
            context.getOrCreate(Molodensky.TGT_SEMI_MINOR).setValue(c.convert(target.getSemiMinorAxis()), unit);
        }
        /*
         * Prepare two affine transforms to be executed before and after the MolodenskyTransform:
         *
         *   - A "normalization" transform for converting degrees to radians,
         *   - A "denormalization" transform for for converting radians to degrees.
         */
        context.normalizeGeographicInputs(0);
        context.denormalizeGeographicOutputs(0);
    }

    /**
     * Invoked by the constructor for setting the contextual parameters. This base implementation sets
     * only the "dim" parameter, but the {@link MolodenskyTransform} subclass will override this method
     * for setting also the EPSG parameters.
     *
     * @param pg   Where to set the parameters.
     * @param unit The unit of measurement to declare.
     * @param Δf   The flattening difference to set.
     */
    void setContextualParameters(final Parameters pg, final Unit<?> unit, final double Δf) {
        final int dim = getSourceDimensions();
        if (dim == getTargetDimensions()) {
            pg.getOrCreate(Molodensky.DIMENSION).setValue(dim);
        }
    }

    /**
     * Returns the parameters used for creating the complete transformation. Those parameters describe a sequence
     * of <cite>normalize</cite> → {@code this} → <cite>denormalize</cite> transforms, <strong>not</strong>
     * including {@linkplain org.apache.sis.referencing.cs.CoordinateSystems#swapAndScaleAxes axis swapping}.
     * Those parameters are used for formatting <cite>Well Known Text</cite> (WKT) and error messages.
     *
     * @return The parameters values for the sequence of
     *         <cite>normalize</cite> → {@code this} → <cite>denormalize</cite> transforms.
     */
    @Override
    protected ContextualParameters getContextualParameters() {
        return context;
    }

    /**
     * Returns a copy of internal parameter values of this transform.
     * The returned group contains parameter values for the eccentricity and the shift among others.
     *
     * <div class="note"><b>Note:</b>
     * this method is mostly for {@linkplain org.apache.sis.io.wkt.Convention#INTERNAL debugging purposes}
     * since the isolation of non-linear parameters in this class is highly implementation dependent.
     * Most GIS applications will instead be interested in the {@linkplain #getContextualParameters()
     * contextual parameters}.</div>
     *
     * @return A copy of the internal parameter values for this transform.
     */
    @Debug
    @Override
    public ParameterValueGroup getParameterValues() {
        final Parameters pg = Parameters.castOrWrap(getParameterDescriptors().createValue());
        final Unit<?> unit = context.getOrCreate(Molodensky.SRC_SEMI_MAJOR).getUnit();
        setContextualParameters(pg, unit, context.doubleValue(Molodensky.FLATTENING_DIFFERENCE));
        pg.getOrCreate(Molodensky.SRC_SEMI_MAJOR).setValue(semiMajor, unit);
        pg.getOrCreate(MapProjection.ECCENTRICITY).setValue(sqrt(eccentricitySquared));
        pg.parameter("abridged").setValue(isAbridged);
        return pg;
    }

    /**
     * Gets the dimension of input points.
     *
     * @return The input dimension, which is 2 or 3.
     */
    @Override
    public final int getSourceDimensions() {
        return isSource3D ? 3 : 2;
    }

    /**
     * Gets the dimension of output points.
     *
     * @return The output dimension, which is 2 or 3.
     */
    @Override
    public final int getTargetDimensions() {
        return isTarget3D ? 3 : 2;
    }

    /**
     * Implementation of {@link #transform(double[], int, double[], int, boolean)} with possibility
     * to override some field values. In this method signature, parameters having the same name than
     * fields have the same value, except if some special circumstances:
     *
     * <ul>
     *   <li>{@code tX}, {@code tY} and {@code tZ} parameters always have the values of {@link #tX}, {@link #tY}
     *       and {@link #tZ} fields when this method is invoked by {@link MolodenskyTransform}. But those values
     *       may be slightly different when this method is invoked by {@link InterpolatedGeocentricTransform}.</li>
     * </ul>
     *
     * @param λ           Longitude (radians).
     * @param φ           Latitude (radians).
     * @param h           Height above the ellipsoid in unit of semi-major axis.
     * @param dstPts      The array into which the transformed coordinate is returned, or {@code null}.
     * @param dstOff      The offset to the location of the transformed point that is stored in the destination array.
     * @param tX          The {@link #tX} field value (or a slightly different value during geocentric interpolation).
     * @param tY          The {@link #tY} field value (or a slightly different value during geocentric interpolation).
     * @param tZ          The {@link #tZ} field value (or a slightly different value during geocentric interpolation).
     * @param derivate    {@code true} for computing the derivative, or {@code false} if not needed.
     * @throws TransformException if a point can not be transformed.
     */
    final Matrix transform(final double λ, final double φ, final double h, final double[] dstPts, int dstOff,
                           double tX, double tY, double tZ, final boolean derivate) throws TransformException
    {
        /*
         * Abridged Molodensky formulas from EPSG guidance note:
         *
         *     ν   = a / √(1 - ℯ²⋅sin²φ)                        : radius of curvature in the prime vertical
         *     ρ   = a⋅(1 – ℯ²) / (1 – ℯ²⋅sin²φ)^(3/2)          : radius of curvature in the meridian
         *     Δλ″ = (-tX⋅sinλ + tY⋅cosλ) / (ν⋅cosφ⋅sin1″)
         *     Δφ″ = (-tX⋅sinφ⋅cosλ - tY⋅sinφ⋅sinλ + tZ⋅cosφ + [a⋅Δf + f⋅Δa]⋅sin(2φ)) / (ρ⋅sin1″)
         *     Δh  = tX⋅cosφ⋅cosλ + tY⋅cosφ⋅sinλ + tZ⋅sinφ + (a⋅Δf + f⋅Δa)⋅sin²φ - Δa
         *
         * we set:
         *
         *    dfm     = (a⋅Δf + f⋅Δa) in abridged case (b⋅Δf in non-abridged case)
         *    sin(2φ) = 2⋅sin(φ)⋅cos(φ)
         */
        final double sinλ  = sin(λ);
        final double cosλ  = cos(λ);
        final double sinφ  = sin(φ);
        final double cosφ  = cos(φ);
        final double sin2φ = sinφ * sinφ;
        final double ν2den = 1 - eccentricitySquared*sin2φ;                 // Square of the denominator of ν
        final double νden  = sqrt(ν2den);                                   // Denominator of ν
        final double ρden  = ν2den * νden;                                  // Denominator of ρ
        double ρ = semiMajor * (1 - eccentricitySquared) / ρden;            // Other notation: Rm = ρ
        double ν = semiMajor / νden;                                        // Other notation: Rn = ν
        double t = Δfmod * 2;                                               // A term in the calculation of Δφ
        if (!isAbridged) {
            ρ += h;
            ν += h;
            t = t*(0.5/νden + 0.5/ρden)                 // = Δf⋅[ν⋅(b/a) + ρ⋅(a/b)]     (without the +h in ν and ρ)
                    + Δa*eccentricitySquared/νden;      // = Δa⋅[ℯ²⋅ν/a]
        }
        final double spcλ = tY*sinλ + tX*cosλ;                      // "spc" stands for "sin plus cos"
        final double cmsλ = tY*cosλ - tX*sinλ;                      // "cms" stands for "cos minus sin"
        final double cmsφ = (tZ + t*sinφ)*cosφ - spcλ*sinφ;
        final double scaleX = ANGULAR_SCALE / (ν*cosφ);
        final double scaleY = ANGULAR_SCALE / ρ;
        if (dstPts != null) {
            dstPts[dstOff++] = λ + (cmsλ * scaleX);
            dstPts[dstOff++] = φ + (cmsφ * scaleY);
            if (isTarget3D) {
                double t1 = Δfmod * sin2φ;          // A term in the calculation of Δh
                double t2 = Δa;
                if (!isAbridged) {
                    t1 /= νden;                     // = Δf⋅(b/a)⋅ν⋅sin²φ
                    t2 *= νden;                     // = Δa⋅(a/ν)
                }
                dstPts[dstOff++] = h + spcλ*cosφ + tZ*sinφ + t1 - t2;
            }
        }
        if (!derivate) {
            return null;
        }
        /*
         * At this point the (Abridged) Molodensky transformation is finished.
         * Code below this point is only for computing the derivative, if requested.
         * Note: variable names do not necessarily tell all the terms that they contain.
         */
        final Matrix matrix   = Matrices.createDiagonal(getTargetDimensions(), getSourceDimensions());
        final double sinφcosφ = sinφ * cosφ;
        final double dν       = eccentricitySquared*sinφcosφ / ν2den;
        final double dν3ρ     = 3*dν * (1 - eccentricitySquared) / ν2den;
        //    double dXdλ     = spcλ;
        final double dYdλ     = cmsλ * sinφ;
        final double dZdλ     = cmsλ * cosφ;
              double dXdφ     = dYdλ / cosφ;
              double dYdφ     = -tZ*sinφ - cosφ*spcλ  +  t*(1 - 2*sin2φ);
              double dZdφ     =  tZ*cosφ - sinφ*spcλ;
        if (isAbridged) {
            /*
             *   Δfmod  =  (a⋅Δf) + (f⋅Δa)
             *   t      =  2⋅Δfmod
             *   dXdh   =  0  so no need to set the matrix element.
             *   dYdh   =  0  so no need to set the matrix element.
             */
            dXdφ -= cmsλ * dν;
            dYdφ -= cmsφ * dν3ρ;
            dZdφ += t*cosφ*sinφ;
        } else {
            /*
             *   Δfmod  =  b⋅Δf
             *   t      =  Δf⋅[ν⋅(b/a) + ρ⋅(a/b)]    (real ν and ρ, without + h)
             *   ν         is actually ν + h
             *   ρ         is actually ρ + h
             */
            final double dρ = dν3ρ * νden * (semiMajor / ρ);    // Reminder: that ρ contains a h term.
            dXdφ -= dν * cmsλ * semiMajor / (νden*ν);           // Reminder: that ν contains a h term.
            dYdφ -= dρ * dZdφ - (Δfmod*(dν*2/(1 - eccentricitySquared) + (1 + 1/ν2den)*(dν - dρ))
                                  + Δa*(dν + 1)*eccentricitySquared) * sinφcosφ / νden;
            if (isSource3D) {
                final double dXdh =  cmsλ / ν;
                final double dYdh = -cmsφ / ρ;
                matrix.setElement(0, 2, -dXdh * scaleX);
                matrix.setElement(1, 2, +dYdh * scaleY);
            }
            final double t1 = Δfmod * (dν*sin2φ + 2*sinφcosφ);
            final double t2 = Δa * dν;
            dZdφ += t1/νden + t2*νden;
        }
        matrix.setElement(0, 0, 1 - spcλ * scaleX);
        matrix.setElement(1, 1, 1 + dYdφ * scaleY);
        matrix.setElement(0, 1,   + dXdφ * scaleX);
        matrix.setElement(1, 0,   - dYdλ * scaleY);
        if (isTarget3D) {
            matrix.setElement(2, 0, dZdλ);
            matrix.setElement(2, 1, dZdφ);
        }
        return matrix;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    protected int computeHashCode() {
        int code = super.computeHashCode() + Numerics.hashCode(
                        Double.doubleToLongBits(Δa)
                +       Double.doubleToLongBits(Δfmod)
                + 31 * (Double.doubleToLongBits(tX)
                + 31 * (Double.doubleToLongBits(tY)
                + 31 * (Double.doubleToLongBits(tZ)))));
        if (isAbridged) code = ~code;
        return code;
    }

    /**
     * Compares the specified object with this math transform for equality.
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(final Object object, final ComparisonMode mode) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object, mode)) {
            final MolodenskyFormula that = (MolodenskyFormula) object;
            return isSource3D == that.isSource3D
                && isTarget3D == that.isTarget3D
                && isAbridged == that.isAbridged
                && Numerics.equals(tX,                  that.tX)
                && Numerics.equals(tY,                  that.tY)
                && Numerics.equals(tZ,                  that.tZ)
                && Numerics.equals(Δa,                  that.Δa)
                && Numerics.equals(Δfmod,               that.Δfmod)
                && Numerics.equals(semiMajor,           that.semiMajor)
                && Numerics.equals(eccentricitySquared, that.eccentricitySquared);
        }
        return false;
    }
}
