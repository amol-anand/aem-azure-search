package com.adobe.aem.poc.azuresearch.components.predicates;

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.adobe.cq.wcm.core.components.models.form.Options;

@ProviderType
public interface PropertyPredicate extends Predicate {

  /**
   * @return true of an operation is set.
   */
  default boolean hasOperation() {
    return false;
  }

  /**
   * @return the querybuilder predication operation (equals, not equals, exists)
   */
  String getOperation();

  /**
   * @return true if the predicate's "and" operation is set.
   */
  boolean hasAnd();

  /**
   * This is typically preceded by hasAnd() since if the and operation is NOT set, then this will
   * return false.
   *
   * @return the value of the and operation.
   */
  Boolean getAnd();

  /**
   * @return the option items for this predicate.
   */
  List<OptionItem> getItems();

  /**
   * @return the configured input type (checkbox, radio, drop-down, etc.)
   */
  Options.Type getType();

  /**
   * @return the configured sub-type (checkbox, toggle, slider, radio buttons)
   */
  String getSubType();

  /**
   * @return the relative property path used for this predicate.
   */
  String getProperty();

  /**
   * @return the predicate's key that indicates the predicates values.
   */
  String getValuesKey();

}