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
package org.apache.sis.util.resources;

import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import org.opengis.util.InternationalString;


/**
 * Locale-dependent resources for error messages.
 *
 * {@section Argument order convention}
 * This resource bundle applies the same convention than JUnit: for every {@code format(…)} method,
 * the first arguments provide information about the context in which the error occurred (e.g. the
 * name of a method argument or the range of valid values), while the erroneous values that caused
 * the error are last. Note that being the last programmatic parameter does not means that the value
 * will appears last in the formatted text, since every localized message can reorder the parameters
 * as they want.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @since   0.3 (derived from geotk-2.2)
 * @version 0.4
 * @module
 */
public final class Errors extends IndexedResourceBundle {
    /**
     * Resource keys. This class is used when compiling sources, but no dependencies to
     * {@code Keys} should appear in any resulting class files. Since the Java compiler
     * inlines final integer values, using long identifiers will not bloat the constant
     * pools of compiled classes.
     *
     * @author  Martin Desruisseaux (IRD, Geomatys)
     * @since   0.3 (derived from geotk-2.2)
     * @version 0.3
     * @module
     */
    public static final class Keys extends KeyConstants {
        /**
         * The unique instance of key constants handler.
         */
        static final Keys INSTANCE = new Keys();

        /**
         * For {@link #INSTANCE} creation only.
         */
        private Keys() {
        }

        /**
         * No element can be added to this set because properties ‘{0}’ and ‘{1}’ are mutually
         * exclusive.
         */
        public static final short CanNotAddToExclusiveSet_2 = 0;

        /**
         * Can not compute the derivative.
         */
        public static final short CanNotComputeDerivative = 1;

        /**
         * Can not connect to “{0}”.
         */
        public static final short CanNotConnectTo_1 = 2;

        /**
         * Can not convert from type ‘{0}’ to type ‘{1}’.
         */
        public static final short CanNotConvertFromType_2 = 3;

        /**
         * Can not convert value “{0}” to type ‘{1}’.
         */
        public static final short CanNotConvertValue_2 = 4;

        /**
         * Can not instantiate an object of type ‘{0}’.
         */
        public static final short CanNotInstantiate_1 = 5;

        /**
         * Can not map an axis from “{0}” to direction “{1}”.
         */
        public static final short CanNotMapAxisToDirection_2 = 6;

        /**
         * Can not open “{0}”.
         */
        public static final short CanNotOpen_1 = 7;

        /**
         * Can not parse “{1}” as a file in the {0} format.
         */
        public static final short CanNotParseFile_2 = 8;

        /**
         * Can not read “{0}”.
         */
        public static final short CanNotRead_1 = 9;

        /**
         * Can not represent “{1}” in the {0} format.
         */
        public static final short CanNotRepresentInFormat_2 = 10;

        /**
         * Can not set a value for property “{0}”.
         */
        public static final short CanNotSetPropertyValue_1 = 11;

        /**
         * Class ‘{0}’ is not final.
         */
        public static final short ClassNotFinal_1 = 12;

        /**
         * Can not clone an object of type ‘{0}’.
         */
        public static final short CloneNotSupported_1 = 13;

        /**
         * Axis directions {0} and {1} are colinear.
         */
        public static final short ColinearAxisDirections_2 = 14;

        /**
         * Thread “{0}” is dead.
         */
        public static final short DeadThread_1 = 15;

        /**
         * Element “{0}” is duplicated.
         */
        public static final short DuplicatedElement_1 = 16;

        /**
         * Identifier “{0}” is duplicated.
         */
        public static final short DuplicatedIdentifier_1 = 17;

        /**
         * Option “{0}” is duplicated.
         */
        public static final short DuplicatedOption_1 = 18;

        /**
         * Element “{0}” is already present.
         */
        public static final short ElementAlreadyPresent_1 = 19;

        /**
         * Argument ‘{0}’ shall not be empty.
         */
        public static final short EmptyArgument_1 = 20;

        /**
         * The dictionary shall contain at least one entry.
         */
        public static final short EmptyDictionary = 21;

        /**
         * Envelope must be at least two-dimensional and non-empty.
         */
        public static final short EmptyEnvelope2D = 22;

        /**
         * Property named “{0}” shall not be empty.
         */
        public static final short EmptyProperty_1 = 23;

        /**
         * Argument ‘{0}’ shall not contain more than {1} elements. A number of {2} is excessive.
         */
        public static final short ExcessiveArgumentSize_3 = 24;

        /**
         * A size of {1} elements is excessive for the “{0}” list.
         */
        public static final short ExcessiveListSize_2 = 25;

        /**
         * Attribute “{0}” is not allowed for an object of type ‘{1}’.
         */
        public static final short ForbiddenAttribute_2 = 26;

        /**
         * Identifier “{0}” is already associated to another object.
         */
        public static final short IdentifierAlreadyBound_1 = 27;

        /**
         * Argument ‘{0}’ can not be an instance of ‘{1}’.
         */
        public static final short IllegalArgumentClass_2 = 28;

        /**
         * Argument ‘{0}’ can not be an instance of ‘{2}’. Expected an instance of ‘{1}’ or derived
         * type.
         */
        public static final short IllegalArgumentClass_3 = 29;

        /**
         * Argument ‘{0}’ can not take the “{1}” value, because the ‘{2}’ field can not take the “{3}”
         * value.
         */
        public static final short IllegalArgumentField_4 = 30;

        /**
         * Argument ‘{0}’ can not take the “{1}” value.
         */
        public static final short IllegalArgumentValue_2 = 31;

        /**
         * Coordinate system of class ‘{0}’ can not have axis in the {1} direction.
         */
        public static final short IllegalAxisDirection_2 = 32;

        /**
         * Illegal bits pattern: {0}.
         */
        public static final short IllegalBitsPattern_1 = 33;

        /**
         * Class ‘{1}’ is illegal. It must be ‘{0}’ or a derived class.
         */
        public static final short IllegalClass_2 = 34;

        /**
         * The “{1}” pattern can not be applied to formating of objects of type ‘{0}’.
         */
        public static final short IllegalFormatPatternForClass_2 = 35;

        /**
         * The “{0}” language is not recognized.
         */
        public static final short IllegalLanguageCode_1 = 36;

        /**
         * Member “{0}” can not be associated to type “{1}”.
         */
        public static final short IllegalMemberType_2 = 37;

        /**
         * Option ‘{0}’ can not take the “{1}” value.
         */
        public static final short IllegalOptionValue_2 = 38;

        /**
         * The [{0} … {1}] range of ordinate values is not valid for the “{2}” axis.
         */
        public static final short IllegalOrdinateRange_3 = 39;

        /**
         * Property ‘{0}’ does not accept instances of ‘{1}’.
         */
        public static final short IllegalPropertyClass_2 = 40;

        /**
         * Range [{0} … {1}] is not valid.
         */
        public static final short IllegalRange_2 = 41;

        /**
         * Value {1} for “{0}” is not a valid Unicode code point.
         */
        public static final short IllegalUnicodeCodePoint_2 = 42;

        /**
         * Unit of measurement “{1}” is not valid for “{0}” values.
         */
        public static final short IllegalUnitFor_2 = 43;

        /**
         * Incompatible coordinate system types.
         */
        public static final short IncompatibleCoordinateSystemTypes = 44;

        /**
         * Property “{0}” has an incompatible value.
         */
        public static final short IncompatiblePropertyValue_1 = 45;

        /**
         * Units “{0}” and “{1}” are incompatible.
         */
        public static final short IncompatibleUnits_2 = 46;

        /**
         * Value “{1}” of attribute ‘{0}’ is inconsistent with other attributes.
         */
        public static final short InconsistentAttribute_2 = 47;

        /**
         * Inconsistent table columns.
         */
        public static final short InconsistentTableColumns = 48;

        /**
         * Index {0} is out of bounds.
         */
        public static final short IndexOutOfBounds_1 = 49;

        /**
         * Indices ({0}, {1}) are out of bounds.
         */
        public static final short IndicesOutOfBounds_2 = 50;

        /**
         * Argument ‘{0}’ can not take an infinite value.
         */
        public static final short InfiniteArgumentValue_1 = 51;

        /**
         * Infinite recursivity.
         */
        public static final short InfiniteRecursivity = 52;

        /**
         * Argument ‘{0}’ shall contain at least {1} elements. A number of {2} is insufficient.
         */
        public static final short InsufficientArgumentSize_3 = 53;

        /**
         * A different value is already associated to the “{0}” key.
         */
        public static final short KeyCollision_1 = 54;

        /**
         * Attribute “{0}” is mandatory for an object of type ‘{1}’.
         */
        public static final short MandatoryAttribute_2 = 55;

        /**
         * Mismatched array lengths.
         */
        public static final short MismatchedArrayLengths = 56;

        /**
         * The coordinate reference system must be the same for all objects.
         */
        public static final short MismatchedCRS = 57;

        /**
         * Mismatched object dimensions: {0}D and {1}D.
         */
        public static final short MismatchedDimension_2 = 58;

        /**
         * Argument ‘{0}’ has {2} dimension{2,choice,1#|2#s}, while {1} was expected.
         */
        public static final short MismatchedDimension_3 = 59;

        /**
         * Mismatched matrix sizes: expected {0}×{1} but got {2}×{3}.
         */
        public static final short MismatchedMatrixSize_4 = 60;

        /**
         * This operation requires the “{0}” module.
         */
        public static final short MissingRequiredModule_1 = 61;

        /**
         * Missing scheme in URI.
         */
        public static final short MissingSchemeInURI = 62;

        /**
         * Missing value for option “{0}”.
         */
        public static final short MissingValueForOption_1 = 63;

        /**
         * Missing value for property “{0}”.
         */
        public static final short MissingValueForProperty_1 = 64;

        /**
         * Missing value in the “{0}” column.
         */
        public static final short MissingValueInColumn_1 = 65;

        /**
         * Options “{0}” and “{1}” are mutually exclusive.
         */
        public static final short MutuallyExclusiveOptions_2 = 66;

        /**
         * Argument ‘{0}’ shall not be negative. The given value was {1}.
         */
        public static final short NegativeArgument_2 = 67;

        /**
         * Can not create a “{0}” array of negative length.
         */
        public static final short NegativeArrayLength_1 = 68;

        /**
         * No convergence for points {0} and {1}.
         */
        public static final short NoConvergenceForPoints_2 = 69;

        /**
         * Element “{0}” has not been found.
         */
        public static final short NoSuchElement_1 = 70;

        /**
         * No property named “{0}” has been found in “{1}”.
         */
        public static final short NoSuchProperty_2 = 71;

        /**
         * No unit of measurement has been specified.
         */
        public static final short NoUnit = 72;

        /**
         * Node “{0}” can not be a child of itself.
         */
        public static final short NodeChildOfItself_1 = 73;

        /**
         * Node “{0}” already has another parent.
         */
        public static final short NodeHasAnotherParent_1 = 74;

        /**
         * Node “{0}” has no parent.
         */
        public static final short NodeHasNoParent_1 = 75;

        /**
         * Node “{0}” is a leaf.
         */
        public static final short NodeIsLeaf_1 = 76;

        /**
         * No “{0}” node found.
         */
        public static final short NodeNotFound_1 = 77;

        /**
         * “{0}” is not an angular unit.
         */
        public static final short NonAngularUnit_1 = 78;

        /**
         * Missing a ‘{1}’ parenthesis in “{0}”.
         */
        public static final short NonEquilibratedParenthesis_2 = 79;

        /**
         * Conversion is not invertible.
         */
        public static final short NonInvertibleConversion = 80;

        /**
         * Non invertible {0}×{1} matrix.
         */
        public static final short NonInvertibleMatrix_2 = 81;

        /**
         * Transform is not invertible.
         */
        public static final short NonInvertibleTransform = 82;

        /**
         * Unit conversion from “{0}” to “{1}” is non-linear.
         */
        public static final short NonLinearUnitConversion_2 = 83;

        /**
         * “{0}” is not a linear unit.
         */
        public static final short NonLinearUnit_1 = 84;

        /**
         * Axis directions {0} and {1} are not perpendicular.
         */
        public static final short NonPerpendicularDirections_2 = 85;

        /**
         * “{0}” is not a scale unit.
         */
        public static final short NonScaleUnit_1 = 86;

        /**
         * “{0}” is not a time unit.
         */
        public static final short NonTemporalUnit_1 = 87;

        /**
         * Scale is not uniform.
         */
        public static final short NonUniformScale = 88;

        /**
         * Argument ‘{0}’ shall not be NaN (Not-a-Number).
         */
        public static final short NotANumber_1 = 89;

        /**
         * Class ‘{0}’ is not a primitive type wrapper.
         */
        public static final short NotAPrimitiveWrapper_1 = 90;

        /**
         * Matrix is not skew-symmetric.
         */
        public static final short NotASkewSymmetricMatrix = 91;

        /**
         * Text “{0}” is not a Unicode identifier.
         */
        public static final short NotAUnicodeIdentifier_1 = 92;

        /**
         * Transform is not affine.
         */
        public static final short NotAnAffineTransform = 93;

        /**
         * Class ‘{0}’ is not a comparable.
         */
        public static final short NotComparableClass_1 = 94;

        /**
         * Argument ‘{0}’ shall not be null.
         */
        public static final short NullArgument_1 = 95;

        /**
         * ‘{0}’ collection does not accept null elements.
         */
        public static final short NullCollectionElement_1 = 134;

        /**
         * Null key is not allowed in this dictionary.
         */
        public static final short NullMapKey = 96;

        /**
         * Null values are not allowed in this dictionary.
         */
        public static final short NullMapValue = 97;

        /**
         * Array length is {0}, while we expected an even length.
         */
        public static final short OddArrayLength_1 = 98;

        /**
         * Recursive call while creating an object for the “{0}” key.
         */
        public static final short RecursiveCreateCallForKey_1 = 99;

        /**
         * A decimal separator is required.
         */
        public static final short RequireDecimalSeparator = 100;

        /**
         * Matrix is singular.
         */
        public static final short SingularMatrix = 101;

        /**
         * Thread “{0}” seems stalled.
         */
        public static final short StalledThread_1 = 102;

        /**
         * Can not move backward in the “{0}” stream.
         */
        public static final short StreamIsForwardOnly_1 = 103;

        /**
         * Expected at least {0} argument{0,choice,1#|2#s}, but got {1}.
         */
        public static final short TooFewArguments_2 = 104;

        /**
         * Expected at most {0} argument{0,choice,1#|2#s}, but got {1}.
         */
        public static final short TooManyArguments_2 = 105;

        /**
         * Ordering between “{0}” and “{1}” elements is undefined.
         */
        public static final short UndefinedOrderingForElements_2 = 106;

        /**
         * Expected an array of length {0}, but got {1}.
         */
        public static final short UnexpectedArrayLength_2 = 107;

        /**
         * Unexpected change in ‘{0}’.
         */
        public static final short UnexpectedChange_1 = 108;

        /**
         * Unexpected end of file while reading “{0}”.
         */
        public static final short UnexpectedEndOfFile_1 = 109;

        /**
         * More characters were expected at the end of “{0}”.
         */
        public static final short UnexpectedEndOfString_1 = 110;

        /**
         * File “{1}” seems to be encoded in an other format than {0}.
         */
        public static final short UnexpectedFileFormat_2 = 111;

        /**
         * Axis direction “{0}” is unknown.
         */
        public static final short UnknownAxisDirection_1 = 112;

        /**
         * Command “{0}” is not recognized.
         */
        public static final short UnknownCommand_1 = 113;

        /**
         * Unknown enumeration value: {0}.
         */
        public static final short UnknownEnumValue_1 = 114;

        /**
         * Format of “{0}” is not recognized.
         */
        public static final short UnknownFormatFor_1 = 115;

        /**
         * Option “{0}” is not recognized.
         */
        public static final short UnknownOption_1 = 116;

        /**
         * Type of the “{0}” property is unknown.
         */
        public static final short UnknownTypeForProperty_1 = 117;

        /**
         * Type ‘{0}’ is unknown in this context.
         */
        public static final short UnknownType_1 = 118;

        /**
         * This affine transform is unmodifiable.
         */
        public static final short UnmodifiableAffineTransform = 119;

        /**
         * The cell at column “{1}” of row “{0}” is unmodifiable.
         */
        public static final short UnmodifiableCellValue_2 = 120;

        /**
         * This geometry is unmodifiable.
         */
        public static final short UnmodifiableGeometry = 121;

        /**
         * This metadata is unmodifiable.
         */
        public static final short UnmodifiableMetadata = 122;

        /**
         * Object ‘{0}’ is unmodifiable.
         */
        public static final short UnmodifiableObject_1 = 123;

        /**
         * Text “{1}” can not be parsed as an object of type ‘{0}’.
         */
        public static final short UnparsableStringForClass_2 = 124;

        /**
         * Text “{1}” can not be parsed as an object of type ‘{0}’, because of the “{2}” characters.
         */
        public static final short UnparsableStringForClass_3 = 125;

        /**
         * No format is specified for objects of class ‘{0}’.
         */
        public static final short UnspecifiedFormatForClass_1 = 126;

        /**
         * Can not handle instances of ‘{0}’ because arbitrary implementations are not yet supported.
         */
        public static final short UnsupportedImplementation_1 = 127;

        /**
         * The ‘{0}’ operation is unsupported.
         */
        public static final short UnsupportedOperation_1 = 128;

        /**
         * The ‘{0}’ type is unsupported.
         */
        public static final short UnsupportedType_1 = 129;

        /**
         * Version {0} is not supported.
         */
        public static final short UnsupportedVersion_1 = 130;

        /**
         * A value is already defined for “{0}”.
         */
        public static final short ValueAlreadyDefined_1 = 131;

        /**
         * Value ‘{0}’={1} is invalid. Expected a number greater than 0.
         */
        public static final short ValueNotGreaterThanZero_2 = 132;

        /**
         * Value ‘{0}’={3} is invalid. Expected a value in the [{1} … {2}] range.
         */
        public static final short ValueOutOfRange_4 = 133;
    }

    /**
     * Constructs a new resource bundle loading data from the given UTF file.
     *
     * @param resources The path of the binary file containing resources, or {@code null} if
     *        there is no resources. The resources may be a file or an entry in a JAR file.
     */
    Errors(final URL resources) {
        super(resources);
    }

    /**
     * Returns the handle for the {@code Keys} constants.
     */
    @Override
    final KeyConstants getKeyConstants() {
        return Keys.INSTANCE;
    }

    /**
     * Returns resources in the given locale.
     *
     * @param  locale The locale, or {@code null} for the default locale.
     * @return Resources in the given locale.
     * @throws MissingResourceException if resources can't be found.
     */
    public static Errors getResources(final Locale locale) throws MissingResourceException {
        return getBundle(Errors.class, locale);
    }

    /**
     * Gets a string for the given key from this resource bundle or one of its parents.
     *
     * @param  key The key for the desired string.
     * @return The string for the given key.
     * @throws MissingResourceException If no object for the given key can be found.
     */
    public static String format(final short key) throws MissingResourceException {
        return getResources(null).getString(key);
    }

    /**
     * Gets a string for the given key are replace all occurrence of "{0}"
     * with values of {@code arg0}.
     *
     * @param  key The key for the desired string.
     * @param  arg0 Value to substitute to "{0}".
     * @return The formatted string for the given key.
     * @throws MissingResourceException If no object for the given key can be found.
     */
    public static String format(final short  key,
                                final Object arg0) throws MissingResourceException
    {
        return getResources(null).getString(key, arg0);
    }

    /**
     * Gets a string for the given key are replace all occurrence of "{0}",
     * "{1}", with values of {@code arg0}, {@code arg1}.
     *
     * @param  key The key for the desired string.
     * @param  arg0 Value to substitute to "{0}".
     * @param  arg1 Value to substitute to "{1}".
     * @return The formatted string for the given key.
     * @throws MissingResourceException If no object for the given key can be found.
     */
    public static String format(final short  key,
                                final Object arg0,
                                final Object arg1) throws MissingResourceException
    {
        return getResources(null).getString(key, arg0, arg1);
    }

    /**
     * Gets a string for the given key are replace all occurrence of "{0}",
     * "{1}", with values of {@code arg0}, {@code arg1}, etc.
     *
     * @param  key The key for the desired string.
     * @param  arg0 Value to substitute to "{0}".
     * @param  arg1 Value to substitute to "{1}".
     * @param  arg2 Value to substitute to "{2}".
     * @return The formatted string for the given key.
     * @throws MissingResourceException If no object for the given key can be found.
     */
    public static String format(final short  key,
                                final Object arg0,
                                final Object arg1,
                                final Object arg2) throws MissingResourceException
    {
        return getResources(null).getString(key, arg0, arg1, arg2);
    }

    /**
     * Gets a string for the given key are replace all occurrence of "{0}",
     * "{1}", with values of {@code arg0}, {@code arg1}, etc.
     *
     * @param  key The key for the desired string.
     * @param  arg0 Value to substitute to "{0}".
     * @param  arg1 Value to substitute to "{1}".
     * @param  arg2 Value to substitute to "{2}".
     * @param  arg3 Value to substitute to "{3}".
     * @return The formatted string for the given key.
     * @throws MissingResourceException If no object for the given key can be found.
     */
    public static String format(final short  key,
                                final Object arg0,
                                final Object arg1,
                                final Object arg2,
                                final Object arg3) throws MissingResourceException
    {
        return getResources(null).getString(key, arg0, arg1, arg2, arg3);
    }

    /**
     * The international string to be returned by {@link formatInternational}.
     */
    private static final class International extends ResourceInternationalString {
        private static final long serialVersionUID = -5355796215044405012L;

        International(short key)                 {super(key);}
        International(short key, Object args)    {super(key, args);}
        @Override KeyConstants getKeyConstants() {return Keys.INSTANCE;}
        @Override IndexedResourceBundle getBundle(final Locale locale) {
            return getResources(locale);
        }
    }

    /**
     * Gets an international string for the given key. This method does not check for the key
     * validity. If the key is invalid, then a {@link MissingResourceException} may be thrown
     * when a {@link InternationalString#toString(Locale)} method is invoked.
     *
     * @param  key The key for the desired string.
     * @return An international string for the given key.
     */
    public static InternationalString formatInternational(final short key) {
        return new International(key);
    }

    /**
     * Gets an international string for the given key. This method does not check for the key
     * validity. If the key is invalid, then a {@link MissingResourceException} may be thrown
     * when a {@link InternationalString#toString(Locale)} method is invoked.
     *
     * {@note This method is redundant with the one expecting <code>Object...</code>, but avoid
     *        the creation of a temporary array. There is no risk of confusion since the two
     *        methods delegate their work to the same <code>format</code> method anyway.}
     *
     * @param  key The key for the desired string.
     * @param  arg Values to substitute to "{0}".
     * @return An international string for the given key.
     */
    public static InternationalString formatInternational(final short key, final Object arg) {
        return new International(key, arg);
    }

    /**
     * Gets an international string for the given key. This method does not check for the key
     * validity. If the key is invalid, then a {@link MissingResourceException} may be thrown
     * when a {@link InternationalString#toString(Locale)} method is invoked.
     *
     * @param  key  The key for the desired string.
     * @param  args Values to substitute to "{0}", "{1}", <i>etc</i>.
     * @return An international string for the given key.
     */
    public static InternationalString formatInternational(final short key, final Object... args) {
        return new International(key, args);
    }
}
