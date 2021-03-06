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
package org.apache.sis.referencing.operation.projection;

import java.io.IOException;
import java.io.ObjectInputStream;

import static java.lang.Math.*;
import static org.apache.sis.math.MathFunctions.atanh;


/**
 * Provides formulas common to Equal Area projections.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.8
 * @version 0.8
 * @module
 */
abstract class EqualAreaProjection extends NormalizedProjection {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -6175270149094989517L;

    /**
     * {@code false} for using the original formulas as published by Synder, or {@code true} for using formulas
     * modified using trigonometric identities. The use of trigonometric identities is for reducing the amount
     * of calls to the {@link Math#sin(double)} and similar methods. Some identities used are:
     *
     * <ul>
     *   <li>sin(2β) = 2⋅sinβ⋅cosβ</li>
     *   <li>sin(4β) = (2 - 4⋅sin²β)⋅sin(2β)</li>
     *   <li>sin(8β) = 4⋅sin(2β)⋅(cos²β - sin²β)⋅(8⋅cos⁴β - 8⋅cos²β + 1)</li>
     * </ul>
     *
     * Note that since this boolean is static final, the compiler should exclude the code in the branch that is never
     * executed (no need to comment-out that code).
     */
    private static final boolean ALLOW_TRIGONOMETRIC_IDENTITIES = false;

    /**
     * Coefficients in the series expansion of the inverse projection,
     * depending only on {@linkplain #eccentricity eccentricity} value.
     * The series expansion is of the following form:
     *
     *     <blockquote>φ = ci₂⋅sin(2β) + ci₄⋅sin(4β) + ci₈⋅sin(8β)</blockquote>
     *
     * This {@code EqualAreaProjection} class uses those coefficients in {@link #φ(double)}.
     *
     * <p><strong>Consider those fields as final!</strong> They are not final only for sub-class
     * constructors convenience and for the purpose of {@link #readObject(ObjectInputStream)}.</p>
     *
     * @see #computeCoefficients()
     */
    private transient double ci2, ci4, ci8;

    /**
     * Creates a new normalized projection from the parameters computed by the given initializer.
     *
     * @param initializer The initializer for computing map projection internal parameters.
     */
    EqualAreaProjection(final Initializer initializer) {
        super(initializer);
    }

    /**
     * Computes the coefficients in the series expansions from the {@link #eccentricitySquared} value.
     * This method shall be invoked after {@code EqualAreaProjection} construction or deserialization.
     */
    void computeCoefficients() {
        final double e2 = eccentricitySquared;
        final double e4 = e2 * e2;
        final double e6 = e2 * e4;
        ci2  =  517/5040.  * e6  +  31/180. * e4  +  1/3. * e2;
        ci4  =  251/3780.  * e6  +  23/360. * e4;
        ci8  =  761/45360. * e6;
        /*
         * When rewriting equations using trigonometric identities, some constants appear.
         * For example sin(2β) = 2⋅sinβ⋅cosβ, so we can factor out the 2 constant into the
         * into the corresponding 'c' field.
         */
        if (ALLOW_TRIGONOMETRIC_IDENTITIES) {
            // Multiplication by powers of 2 does not bring any additional rounding error.
            ci2 *=  2;
            ci4 *=  8;
            ci8 *= 64;
        }
    }

    /**
     * Creates a new projection initialized to the values of the given one. This constructor may be invoked after
     * we determined that the default implementation can be replaced by an other one, for example using spherical
     * formulas instead than the ellipsoidal ones. This constructor allows to transfer all parameters to the new
     * instance without recomputing them.
     */
    EqualAreaProjection(final EqualAreaProjection other) {
        super(other);
        ci2 = other.ci2;
        ci4 = other.ci4;
        ci8 = other.ci8;
    }

    /**
     * Calculates <strong>part</strong> of <var>q</var> from Snyder equation (3-12).
     * In order to get the <var>q</var> function, this method output must be multiplied
     * by <code>(1 - {@linkplain #eccentricitySquared})</code>.
     *
     * <p>This equation has the following properties:</p>
     *
     * <ul>
     *   <li>Input in the [-1 … +1] range</li>
     *   <li>Output multiplied by {@code (1 - ℯ²)} in the [-2 … +2] range</li>
     *   <li>Output of the same sign than input</li>
     *   <li>q(-sinφ) = -q(sinφ)</li>
     *   <li>q(0) = 0</li>
     * </ul>
     *
     * In the spherical case, <var>q</var> = 2⋅sinφ. It is caller responsibility to ensure that this
     * method is not invoked in the spherical case, since this implementation does not work in such case.
     *
     * @param  sinφ sine of the latitude <var>q</var> is calculated for.
     * @return <var>q</var> from Snyder equation (3-12).
     */
    final double qm(final double sinφ) {
        final double ℯsinφ = eccentricity * sinφ;
        return sinφ / (1 - ℯsinφ*ℯsinφ) + atanh(ℯsinφ) / eccentricity;
    }

    /**
     * Gets the derivative of the {@link #qm(double)} method.
     * Callers must multiply the returned value by <code>(1 - {@linkplain #eccentricitySquared})</code>
     * in order to get the derivative of Snyder equation (3-12).
     *
     * @param  sinφ  the sine of latitude.
     * @param  cosφ  the cosines of latitude.
     * @return the {@code qm} derivative at the specified latitude.
     */
    final double dqm_dφ(final double sinφ, final double cosφ) {
        final double ℯsinφ2 = eccentricitySquared * (sinφ*sinφ);
        return (cosφ / (1 - ℯsinφ2)) * (1 + ((1 + ℯsinφ2) / (1 - ℯsinφ2)));
    }

    /**
     * Computes the latitude using equation 3-18 from Synder.
     *
     * @param  sinβ see Synder equation 10-26.
     * @return the latitude in radians.
     */
    final double φ(final double sinβ) {
        final double β = asin(sinβ);
        if (!ALLOW_TRIGONOMETRIC_IDENTITIES) {
            return ci8 * sin(8*β)
                 + ci4 * sin(4*β)
                 + ci2 * sin(2*β)
                 + β;                                                               // Synder 3-18
        } else {
            /*
             * Same formula than above, but rewriten using trigonometric identities in order to avoid
             * multiple calls to sin(double) method. The cost is only one sqrt(double) method call.
             */
            final double sin2_β = sinβ*sinβ;                                        // = sin²β
            final double cos2_β = 1 - sin2_β;                                       // = cos²β
            final double t2β = sinβ * sqrt(cos2_β);                                 // = sin(2β) /   2
            final double t4β = 0.5 - sin2_β;                                        // = sin(4β) / ( 4⋅sin(2β))
            final double t8β = (cos2_β - sin2_β)*(cos2_β*cos2_β - cos2_β + 1./8);   // = sin(8β) / (32⋅sin(2β))

            assert identityEquals(t2β, sin(2*β) / ( 2      ));
            assert identityEquals(t4β, sin(4*β) / ( 8 * t2β));
            assert identityEquals(t8β, sin(8*β) / (64 * t2β));

            return (ci8*t8β  +  ci4*t4β  +  ci2) * t2β  +  β;
        }
    }

    /**
     * Verifies if a trigonometric identity produced the expected value. This method is used in assertions only.
     * The tolerance threshold is approximatively 1.5E-12 (note that it still about 7000 time greater than
     * {@code Math.ulp(1.0)}).
     *
     * @see #ALLOW_TRIGONOMETRIC_IDENTITIES
     */
    private static boolean identityEquals(final double actual, final double expected) {
        // Use !(a > b) instead of (a <= b) in order to tolerate NaN.
        return !(abs(actual - expected) > (ANGULAR_TOLERANCE / 1000));
    }

    /**
     * Restores transient fields after deserialization.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        computeCoefficients();
    }
}
