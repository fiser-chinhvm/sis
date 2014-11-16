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
package org.apache.sis.feature;

import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.io.Serializable;
import org.opengis.util.GenericName;
import org.opengis.metadata.quality.DataQuality;
import org.opengis.metadata.maintenance.ScopeCode;
import org.apache.sis.util.Debug;
import org.apache.sis.util.Classes;
import org.apache.sis.util.ArgumentChecks;

// Branch-dependent imports
import org.opengis.feature.Attribute;
import org.opengis.feature.AttributeType;


/**
 * An instance of an {@linkplain DefaultAttributeType attribute type} containing the value of an attribute in a feature.
 * {@code Attribute} holds two main information:
 *
 * <ul>
 *   <li>A reference to an {@linkplain DefaultAttributeType attribute type}
 *       which define the base Java type and domain of valid values.</li>
 *   <li>A value, which may be a singleton ([0 … 1] cardinality) or multi-valued ([0 … ∞] cardinality).</li>
 * </ul>
 *
 * {@section Limitations}
 * <ul>
 *   <li><b>Multi-threading:</b> {@code AbstractAttribute} instances are <strong>not</strong> thread-safe.
 *       Synchronization, if needed, shall be done externally by the caller.</li>
 *   <li><b>Serialization:</b> serialized objects of this class are not guaranteed to be compatible with future
 *       versions. Serialization should be used only for short term storage or RMI between applications running
 *       the same SIS version.</li>
 * </ul>
 *
 * @param <V> The type of attribute values.
 *
 * @author  Johann Sorel (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.5
 * @version 0.5
 * @module
 *
 * @see DefaultAttributeType
 */
public abstract class AbstractAttribute<V> extends Field<V> implements Attribute<V>, Cloneable, Serializable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 7442739120526654676L;

    /**
     * Information about the attribute (base Java class, domain of values, <i>etc.</i>).
     */
    final AttributeType<V> type;

    /**
     * Other attributes that describes this attribute, or {@code null} if not yet created.
     *
     * @see #characteristics()
     */
    private transient Map<String,Attribute<?>> characteristics;

    /**
     * Creates a new attribute of the given type.
     *
     * @param type Information about the attribute (base Java class, domain of values, <i>etc.</i>).
     *
     * @see #create(AttributeType)
     */
    protected AbstractAttribute(final AttributeType<V> type) {
        this.type = type;
    }

    /**
     * Creates a new attribute of the given type initialized to the
     * {@linkplain DefaultAttributeType#getDefaultValue() default value}.
     *
     * @param  <V>  The type of attribute values.
     * @param  type Information about the attribute (base Java class, domain of values, <i>etc.</i>).
     * @return The new attribute.
     */
    public static <V> AbstractAttribute<V> create(final AttributeType<V> type) {
        ArgumentChecks.ensureNonNull("type", type);
        return isSingleton(type.getMaximumOccurs())
               ? new SingletonAttribute<>(type)
               : new MultiValuedAttribute<>(type);
    }

    /**
     * Creates a new attribute of the given type initialized to the given value.
     * Note that a {@code null} value may not be the same as the default value.
     *
     * @param  <V>   The type of attribute values.
     * @param  type  Information about the attribute (base Java class, domain of values, <i>etc.</i>).
     * @param  value The initial value (may be {@code null}).
     * @return The new attribute.
     */
    static <V> AbstractAttribute<V> create(final AttributeType<V> type, final Object value) {
        ArgumentChecks.ensureNonNull("type", type);
        return isSingleton(type.getMaximumOccurs())
               ? new SingletonAttribute<>(type, value)
               : new MultiValuedAttribute<>(type, value);
    }

    /**
     * Returns the name of this attribute as defined by its {@linkplain #getType() type}.
     * This convenience method delegates to {@link AttributeType#getName()}.
     *
     * @return The attribute name specified by its type.
     */
    @Override
    public GenericName getName() {
        return type.getName();
    }

    /**
     * Returns information about the attribute (base Java class, domain of values, <i>etc.</i>).
     *
     * @return Information about the attribute.
     */
    @Override
    public AttributeType<V> getType() {
        return type;
    }

    /**
     * Returns the attribute value, or {@code null} if none. This convenience method can be invoked in
     * the common case where the {@linkplain DefaultAttributeType#getMaximumOccurs() maximum number}
     * of attribute values is restricted to 1 or 0.
     *
     * @return The attribute value (may be {@code null}).
     * @throws IllegalStateException if this attribute contains more than one value.
     *
     * @see AbstractFeature#getPropertyValue(String)
     */
    @Override
    public abstract V getValue() throws IllegalStateException;

    /**
     * Returns all attribute values, or an empty collection if none.
     * The returned collection is <cite>live</cite>: changes in the returned collection
     * will be reflected immediately in this {@code Attribute} instance, and conversely.
     *
     * <p>The default implementation returns a collection which will delegate its work to
     * {@link #getValue()} and {@link #setValue(Object)}.</p>
     *
     * @return The attribute values in a <cite>live</cite> collection.
     */
    @Override
    public Collection<V> getValues() {
        return super.getValues();
    }

    /**
     * Sets the attribute value. All previous values are replaced by the given singleton.
     *
     * {@section Validation}
     * The amount of validation performed by this method is implementation dependent.
     * Usually, only the most basic constraints are verified. This is so for performance reasons
     * and also because some rules may be temporarily broken while constructing a feature.
     * A more exhaustive verification can be performed by invoking the {@link #quality()} method.
     *
     * @param value The new value, or {@code null} for removing all values from this attribute.
     *
     * @see AbstractFeature#setPropertyValue(String, Object)
     */
    @Override
    public abstract void setValue(final V value);

    /**
     * Sets the attribute values. All previous values are replaced by the given collection.
     *
     * <p>The default implementation ensures that the given collection contains at most one element,
     * then delegates to {@link #setValue(Object)}.</p>
     *
     * @param  values The new values.
     * @throws IllegalArgumentException if the given collection contains too many elements.
     */
    @Override
    public void setValues(final Collection<? extends V> values) throws IllegalArgumentException {
        super.setValues(values);
    }

    /**
     * Other attributes that describes this attribute. For example if this attribute carries a measurement,
     * then a characteristic of this attribute could be the measurement accuracy.
     * See "<cite>Attribute characterization</cite>" in {@link DefaultAttributeType} Javadoc for more information.
     *
     * <p>The map returned by this method contains only the characteristics explicitely defined for this attribute.
     * If the map contains no characteristic for a given name, a {@linkplain DefaultAttributeType#getDefaultValue()
     * default value} may still exist.
     * In such cases, callers may also need to inspect the {@link DefaultAttributeType#characteristics()}
     * as shown in the <cite>Reading a characteristic</cite> section below.</p>
     *
     * <div class="note"><b>Rational:</b>
     * Very often, all attributes of a given type in the same file have the same characteristics.
     * For example it is very common that all temperature measurements in a file have the same accuracy,
     * and setting a different accuracy for a single measurement is relatively rare.
     * Consequently, {@code characteristics.isEmpty()} is a convenient way to check that an attribute have
     * all the "standard" characteristics and need no special processing.</div>
     *
     * {@section Reading a characteristic}
     * If an attribute is known to be a measurement with a characteristic named "accuracy" of type {@link Float},
     * then the accuracy value could be read as below:
     *
     * {@preformat java
     *     Float getAccuracy(Attribute<?> measurement) {
     *         Attribute<?> accuracy = measurement.characteristics().get("accuracy");
     *         if (accuracy != null) {
     *             return (Float) accuracy.getValue(); // Value may be null.
     *         } else {
     *             return (Float) measurement.getType().characteristics().get("accuracy").getDefaultValue();
     *             // A more sophisticated implementation would probably cache the default value somewhere.
     *         }
     *     }
     * }
     *
     * {@section Adding a characteristic}
     * A new characteristic can be added in the map in three different ways:
     * <ol>
     *   <li>Putting the (<var>name</var>, <var>characteristic</var>) pair explicitely.
     *     If an older characteristic existed for that name, it will be replaced.
     *     Example:
     *
     *     {@preformat java
     *       Attribute<?> accuracy = ...; // To be created by the caller.
     *       characteristics.put("accuracy", accuracy);
     *     }</li>
     *
     *   <li>Adding the new characteristic to the {@linkplain Map#values() values} collection.
     *     The name is inferred automatically from the characteristic type.
     *     If an older characteristic existed for the same name, an {@link IllegalStateException} will be thrown.
     *     Example:
     *
     *     {@preformat java
     *       Attribute<?> accuracy = ...; // To be created by the caller.
     *       characteristics.values().add(accuracy);
     *     }</li>
     *
     *   <li>Adding the characteristic name to the {@linkplain Map#keySet() key set}.
     *     If no characteristic existed for that name, a default one will be created.
     *     Example:
     *
     *     {@preformat java
     *       characteristics.keySet().add("accuracy"); // Ensure that an entry will exist for that name.
     *       Attribute<?> accuracy = characteristics.get("accuracy");
     *       Features.cast(accuracy, Float.class).setValue(...); // Set new accuracy value here as a float.
     *     }</li>
     * </ol>
     *
     * @return Other attribute types that describes this attribute type, or an empty set if none.
     *
     * @see DefaultAttributeType#characteristics()
     */
    public Map<String,Attribute<?>> characteristics() {
        if (characteristics == null) {
            if (type instanceof DefaultAttributeType<?>) {
                Map<String, AttributeType<?>> map = ((DefaultAttributeType<?>) type).characteristics();
                if (map.isEmpty()) {
                    characteristics = Collections.emptyMap();
                } else {
                    if (!(map instanceof CharacteristicTypeMap)) {
                        final Collection<AttributeType<?>> types = map.values();
                        map = CharacteristicTypeMap.create(type, types.toArray(new AttributeType<?>[types.size()]));
                    }
                    characteristics = new CharacteristicMap(this, (CharacteristicTypeMap) map);
                }
            }
        }
        return characteristics;
    }

    /**
     * Evaluates the quality of this attribute at this method invocation time. The data quality reports
     * may include information about whether the attribute value mets the constraints defined by the
     * {@linkplain DefaultAttributeType attribute type}, or any other criterion at implementation choice.
     *
     * <p>The default implementation reports data quality with at least the following information:</p>
     * <ul>
     *   <li>
     *     The {@linkplain org.apache.sis.metadata.iso.quality.DefaultDataQuality#getScope() scope}
     *     {@linkplain org.apache.sis.metadata.iso.quality.DefaultScope#getLevel() level} is set to
     *     {@link org.opengis.metadata.maintenance.ScopeCode#ATTRIBUTE}.
     *   </li><li>
     *     At most one {@linkplain org.apache.sis.metadata.iso.quality.DefaultDomainConsistency domain consistency}
     *     element is added to the {@linkplain org.apache.sis.metadata.iso.quality.DefaultDataQuality#getReports()
     *     reports} list (implementations are free to omit that element if they have nothing to report).
     *     If a report is provided, then it will contain at least the following information:
     *     <ul>
     *       <li>
     *         <p>The {@linkplain #getName() attribute name} as the data quality
     *         {@linkplain org.apache.sis.metadata.iso.quality.DefaultDomainConsistency#getMeasureIdentification()
     *         measure identification}.</p>
     *
     *         <div class="note"><b>Note:</b> strictly speaking, {@code measureIdentification} identifies the
     *         <em>quality measurement</em>, not the “real” measurement itself. However this implementation
     *         uses the same set of identifiers for both for simplicity.</div>
     *       </li><li>
     *         <p>If the attribute {@linkplain #getValue() value} is not an {@linkplain Class#isInstance instance}
     *         of the expected {@linkplain DefaultAttributeType#getValueClass() value class}, or if the number
     *         of occurrences is not inside the cardinality range, or if any other constraint is violated, then
     *         a {@linkplain org.apache.sis.metadata.iso.quality.DefaultConformanceResult conformance result} is
     *         added for each violation with an
     *         {@linkplain org.apache.sis.metadata.iso.quality.DefaultConformanceResult#getExplanation() explanation}
     *         set to the error message.</p>
     *
     *         <div class="warning"><b>Note:</b> this is a departure from ISO intend, since {@code explanation}
     *         should be a statement about what a successful conformance means. This point may be reformulated
     *         in a future SIS version.</div>
     *       </li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * This attribute is valid if this method does not report any
     * {@linkplain org.apache.sis.metadata.iso.quality.DefaultConformanceResult conformance result} having a
     * {@linkplain org.apache.sis.metadata.iso.quality.DefaultConformanceResult#pass() pass} value of {@code false}.
     *
     * <div class="note"><b>Example:</b> given an attribute named “population” with [1 … 1] cardinality,
     * if no value has been assigned to that attribute, then this {@code quality()} method will return
     * the following data quality report:
     *
     * {@preformat text
     *   Data quality
     *     ├─Scope
     *     │   └─Level………………………………………………… Attribute
     *     └─Report
     *         ├─Measure identification
     *         │   └─Code………………………………………… population
     *         ├─Evaluation method type…… Direct internal
     *         └─Result
     *             ├─Explanation……………………… Missing value for “population” property.
     *             └─Pass………………………………………… false
     * }
     * </div>
     *
     * @return Reports on all constraint violations found.
     *
     * @see AbstractFeature#quality()
     */
    public DataQuality quality() {
        final Validator v = new Validator(ScopeCode.ATTRIBUTE);
        v.validate(type, getValues());
        return v.quality;
    }

    /**
     * Returns a string representation of this attribute.
     * The returned string is for debugging purpose and may change in any future SIS version.
     *
     * @return A string representation of this attribute for debugging purpose.
     */
    @Debug
    @Override
    public String toString() {
        return FieldType.toString("Attribute", type, Classes.getShortName(type.getValueClass()), getValues().iterator());
    }

    /**
     * Returns a copy of this attribute.
     * The default implementation returns a <em>shallow</em> copy:
     * the attribute {@linkplain #getValue() value} and {@linkplain #characteristics() characteristics}
     * are <strong>not</strong> cloned.
     * However subclasses may choose to do otherwise.
     *
     * @return A clone of this attribute.
     * @throws CloneNotSupportedException if this attribute, the {@linkplain #getValue() value}
     *         or one of its {@linkplain #characteristics() characteristics} can not be cloned.
     */
    @Override
    @SuppressWarnings("unchecked")
    public AbstractAttribute<V> clone() throws CloneNotSupportedException {
        final AbstractAttribute<V> clone = (AbstractAttribute<V>) super.clone();
        final Map<String,Attribute<?>> c = clone.characteristics;
        if (c instanceof CharacteristicMap) {
            clone.characteristics = ((CharacteristicMap) c).clone();
        }
        return clone;
    }
}
