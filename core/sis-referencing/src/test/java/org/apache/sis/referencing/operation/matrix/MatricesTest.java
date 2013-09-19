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
package org.apache.sis.referencing.operation.matrix;

import org.opengis.geometry.Envelope;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.cs.AxisDirection;
import org.apache.sis.geometry.Envelope2D;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.util.iso.Types;
import org.apache.sis.test.DependsOnMethod;
import org.apache.sis.test.DependsOn;
import org.apache.sis.test.TestCase;
import org.junit.Test;

import static org.apache.sis.test.Assert.*;
import static org.opengis.referencing.cs.AxisDirection.*;


/**
 * Tests the {@link Matrices} implementation.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.4
 * @version 0.4
 * @module
 */
@DependsOn({
    Matrix1Test.class,
    Matrix2Test.class,
    Matrix3Test.class,
    Matrix4Test.class,
    GeneralMatrixTest.class,
    NonSquareMatrixTest.class
})
public final strictfp class MatricesTest extends TestCase {
    /**
     * Tests {@link Matrices#createTransform(AxisDirection[], AxisDirection[])} with the same sequence of axes.
     * The result shall be an identity matrix.
     *
     * {@note <code>Matrices.createTransform(AxisDirection[], AxisDirection[])</code> needs to be tested with
     *        special care, because this method will be the most frequently invoked one when building CRS.}
     */
    @Test
    public void testCreateTransformWithSameAxes() {
        final MatrixSIS matrix = Matrices.createTransform(
                new AxisDirection[] {NORTH, EAST, UP},
                new AxisDirection[] {NORTH, EAST, UP});

        assertTrue ("isAffine",   matrix.isAffine());
        assertTrue ("isIdentity", matrix.isIdentity());
        assertEquals("numRow", 4, matrix.getNumRow());
        assertEquals("numCol", 4, matrix.getNumCol());
    }

    /**
     * Tests {@link Matrices#createTransform(AxisDirection[], AxisDirection[])} with different axes.
     * Axes are moved in different positions, and some axes will have opposite directions.
     * However the number of axes stay the same.
     *
     * {@note <code>Matrices.createTransform(AxisDirection[], AxisDirection[])</code> needs to be tested with
     *        special care, because this method will be the most frequently invoked one when building CRS.}
     */
    @Test
    @DependsOnMethod("testCreateTransformWithSameAxes")
    public void testCreateTransformWithDifferentAxes() {
        final MatrixSIS matrix = Matrices.createTransform(
                new AxisDirection[] {NORTH, EAST, UP},
                new AxisDirection[] {WEST, UP, SOUTH});

        assertTrue ("isAffine",   matrix.isAffine());
        assertFalse("isIdentity", matrix.isIdentity());
        assertEquals("numRow", 4, matrix.getNumRow());
        assertEquals("numCol", 4, matrix.getNumCol());
        assertEquals(Matrices.create(4, 4, new double[] {
             0,-1, 0, 0,
             0, 0, 1, 0,
            -1, 0, 0, 0,
             0, 0, 0, 1
        }), matrix);
    }

    /**
     * Tests {@link Matrices#createTransform(AxisDirection[], AxisDirection[])} with less axes
     * in the destination than in the source.
     *
     * {@note <code>Matrices.createTransform(AxisDirection[], AxisDirection[])</code> needs to be tested with
     *        special care, because this method will be the most frequently invoked one when building CRS.}
     */
    @Test
    @DependsOnMethod("testCreateTransformWithDifferentAxes")
    public void testCreateTransformWithLessAxes() {
        final MatrixSIS matrix = Matrices.createTransform(
                new AxisDirection[] {NORTH, EAST, UP},
                new AxisDirection[] {DOWN, NORTH});

        assertFalse("isIdentity", matrix.isIdentity());
        assertEquals("numRow", 3, matrix.getNumRow());
        assertEquals("numCol", 4, matrix.getNumCol());
        assertEquals(Matrices.create(3, 4, new double[] {
            0, 0,-1, 0,
            1, 0, 0, 0,
            0, 0, 0, 1
        }), matrix);
    }

    /**
     * Tests {@link Matrices#createTransform(AxisDirection[], AxisDirection[])} with the axis repeated twice.
     * This unusual, but shall nevertheless be supported.
     *
     * {@note <code>Matrices.createTransform(AxisDirection[], AxisDirection[])</code> needs to be tested with
     *        special care, because this method will be the most frequently invoked one when building CRS.}
     */
    @Test
    @DependsOnMethod("testCreateTransformWithLessAxes")
    public void testCreateTransformWithRepeatedAxes() {
        final MatrixSIS matrix = Matrices.createTransform(
                new AxisDirection[] {NORTH, EAST, UP},
                new AxisDirection[] {DOWN, DOWN});

        assertFalse("isIdentity", matrix.isIdentity());
        assertEquals("numRow", 3, matrix.getNumRow());
        assertEquals("numCol", 4, matrix.getNumCol());
        assertEquals(Matrices.create(3, 4, new double[] {
            0, 0,-1, 0,
            0, 0,-1, 0,
            0, 0, 0, 1
        }), matrix);
    }

    /**
     * Tests that {@link Matrices#createTransform(AxisDirection[], AxisDirection[])}
     * throw an exception if a destination axis is not in the source.
     *
     * {@note <code>Matrices.createTransform(AxisDirection[], AxisDirection[])</code> needs to be tested with
     *        special care, because this method will be the most frequently invoked one when building CRS.}
     */
    @Test
    public void testCreateTransformWithAxisNotInSource() {
        try {
            Matrices.createTransform(
                    new AxisDirection[] {NORTH, EAST, UP},
                    new AxisDirection[] {DOWN, GEOCENTRIC_X});
            fail("Expected an exception.");
        } catch (IllegalArgumentException e) {
            assertMessageContainsDirection(e, GEOCENTRIC_X);
        }
    }

    /**
     * Tests that {@link Matrices#createTransform(AxisDirection[], AxisDirection[])}
     * throw an exception if the arguments contain colinear axis directions.
     *
     * {@note <code>Matrices.createTransform(AxisDirection[], AxisDirection[])</code> needs to be tested with
     *        special care, because this method will be the most frequently invoked one when building CRS.}
     */
    @Test
    public void testCreateTransformWithColinearAxes() {
        try {
            Matrices.createTransform(
                    new AxisDirection[] {NORTH, EAST, UP, WEST},
                    new AxisDirection[] {NORTH, EAST, UP});
            fail("Expected an exception.");
        } catch (IllegalArgumentException e) {
            assertMessageContainsDirection(e, EAST);
            assertMessageContainsDirection(e, WEST);
        }
    }

    /**
     * Asserts that the message of the given exception contains the given axis direction.
     */
    private static void assertMessageContainsDirection(final Throwable e, final AxisDirection direction) {
        final String message = e.getMessage();
        final String label = Types.getCodeTitle(direction).toString();
        if (!message.contains(label)) {
            fail("Direction \"" + label + "\" not found in error message: " + message);
        }
    }

    /**
     * Tests {@link Matrices#createTransform(Envelope, Envelope)}.
     * This method tests the example given in {@code Matrices.createTransform(…)} javadoc.
     */
    @Test
    public void testCreateTransformFromEnvelopes() {
        final Envelope srcEnvelope = new Envelope2D(null, -20, -40, 100, 200);
        final Envelope dstEnvelope = new Envelope2D(null, -10, -25, 300, 500);
        MatrixSIS matrix = Matrices.createTransform(srcEnvelope, dstEnvelope);
        assertTrue ("isAffine",   matrix.isAffine());
        assertFalse("isIdentity", matrix.isIdentity());
        assertEquals("numRow", 3, matrix.getNumRow());
        assertEquals("numCol", 3, matrix.getNumCol());
        assertEquals(Matrices.create(3, 3, new double[] {
            3.0,  0,    50,
            0,    2.5,  75,
            0,    0,     1
        }), matrix);
        /*
         * Test dropping a dimension.
         */
        final GeneralEnvelope expanded = new GeneralEnvelope(3);
        expanded.subEnvelope(0, 2).setEnvelope(srcEnvelope);
        expanded.setRange(2, 1000, 2000);
        matrix = Matrices.createTransform(expanded, dstEnvelope);
        assertEquals("numRow", 3, matrix.getNumRow());
        assertEquals("numCol", 4, matrix.getNumCol());
        assertEquals(Matrices.create(3, 4, new double[] {
            3.0,  0,   0,  50,
            0,    2.5, 0,  75,
            0,    0,   0,   1
        }), matrix);
        /*
         * Test adding a dimension with ordinate values set to zero.
         */
        expanded.subEnvelope(0, 2).setEnvelope(dstEnvelope);
        matrix = Matrices.createTransform(srcEnvelope, expanded);
        assertEquals("numRow", 4, matrix.getNumRow());
        assertEquals("numCol", 3, matrix.getNumCol());
        assertEquals(Matrices.create(4, 3, new double[] {
            3.0,  0,    50,
            0,    2.5,  75,
            0,    0,     0,
            0,    0,     1
        }), matrix);
    }

    /**
     * Tests {@link Matrices#createTransform(Envelope, AxisDirection[], Envelope, AxisDirection[])}.
     * This method tests the example given in {@code Matrices.createTransform(…)} javadoc.
     */
    @Test
    @DependsOnMethod({"testCreateTransformFromEnvelopes", "testCreateTransformWithLessAxes"})
    public void testCreateTransformFromEnvelopesAndAxes() {
        final Envelope srcEnvelope = new Envelope2D(null, -40, +20, 200, 100); // swapped (y,-x)
        final Envelope dstEnvelope = new Envelope2D(null, -10, -25, 300, 500);
        MatrixSIS matrix = Matrices.createTransform(
                srcEnvelope, new AxisDirection[] {NORTH, WEST},
                dstEnvelope, new AxisDirection[] {EAST, NORTH});
        assertTrue ("isAffine",   matrix.isAffine());
        assertFalse("isIdentity", matrix.isIdentity());
        assertEquals("numRow", 3, matrix.getNumRow());
        assertEquals("numCol", 3, matrix.getNumCol());
        assertEquals(Matrices.create(3, 3, new double[] {
            0,   -3.0, 350,
            2.5,  0,    75,
            0,    0,     1
        }), matrix);
        /*
         * Test dropping a dimension.
         */
        final GeneralEnvelope expanded = new GeneralEnvelope(3);
        expanded.subEnvelope(0, 2).setEnvelope(srcEnvelope);
        expanded.setRange(2, 1000, 2000);
        matrix = Matrices.createTransform(
                expanded,    new AxisDirection[] {NORTH, WEST, UP},
                dstEnvelope, new AxisDirection[] {EAST, NORTH});
        assertEquals("numRow", 3, matrix.getNumRow());
        assertEquals("numCol", 4, matrix.getNumCol());
        assertEquals(Matrices.create(3, 4, new double[] {
            0,   -3.0, 0, 350,
            2.5,  0,   0,  75,
            0,    0,   0,   1
        }), matrix);
    }

    /**
     * Tests {@link Matrices#createDimensionSelect(int, int[])}.
     * This method tests the example given in {@code Matrices.createDimensionSelect(…)} javadoc.
     */
    @Test
    public void testCreateDimensionSelect() {
        final MatrixSIS matrix = Matrices.createDimensionSelect(4, new int[] {1, 0, 3});
        assertEquals(Matrices.create(4, 5, new double[] {
            0, 1, 0, 0, 0,
            1, 0, 0, 0, 0,
            0, 0, 0, 1, 0,
            0, 0, 0, 0, 1
        }), matrix);
    }

    /**
     * Tests {@link Matrices#createPassThrough(int, Matrix, int)} with dimensions
     * added both before and after the sub-matrix.
     */
    @Test
    public void testCreatePassThrough() {
        MatrixSIS matrix = Matrices.create(3, 4, new double[] {
            2, 0, 3, 8,
            0, 4, 7, 5,
            0, 0, 0, 1
        });
        matrix = Matrices.createPassThrough(2, matrix, 1);
        assertEquals(Matrices.create(6, 7, new double[] {
            1, 0, 0, 0, 0, 0, 0,  // Dimension added
            0, 1, 0, 0, 0, 0, 0,  // Dimension added
            0, 0, 2, 0, 3, 0, 8,  // Sub-matrix, row 0
            0, 0, 0, 4, 7, 0, 5,  // Sub-matrix, row 1
            0, 0, 0, 0, 0, 1, 0,  // Dimension added
            0, 0, 0, 0, 0, 0, 1   // Last sub-matrix row
        }), matrix);
    }

    /**
     * Tests {@link Matrices#copy(Matrix)}
     */
    @Test
    public void testCopy() {
        final Matrix matrix = new Matrix3(10, 20, 30, 40, 50, 60, 70, 80, 90);
        final Matrix copy = Matrices.copy(matrix);
        assertNotSame("copy", matrix, copy);
        assertEquals ("copy", matrix, copy);
    }

    /**
     * Tests {@link Matrices#toString(Matrix)}
     */
    @Test
    public void testToString() {
        final MatrixSIS matrix = Matrices.create(4, 4, new double[] {
            39.5193682106975150,  -68.5200,     80.0,  98,
           -66.0358637477182200,   Double.NaN,  43.9,  Double.NEGATIVE_INFINITY,
             2.0741018968776337,   83.7260,     37.0,  -3,
            91.8796187759200600,  -18.2674,     24.0,  36
        });
        assertMultilinesEquals(
                "┌                                            ┐\n" +
                "│  39.5193682106975150  -68.5200  80.0  98.0 │\n" +
                "│ -66.0358637477182200       NaN  43.9    -∞ │\n" +
                "│   2.0741018968776337   83.7260  37.0  -3.0 │\n" +
                "│  91.8796187759200600  -18.2674  24.0  36.0 │\n" +
                "└                                            ┘\n", matrix.toString());
    }
}