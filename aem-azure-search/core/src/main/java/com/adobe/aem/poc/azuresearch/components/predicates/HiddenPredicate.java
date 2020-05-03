package com.adobe.aem.poc.azuresearch.components.predicates;

import java.util.Map;

import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.search.PredicateGroup;

@ProviderType
public interface HiddenPredicate extends Predicate {
  /**
   * @return a PredicateGroup that represents the HiddenPredicate configuration.
   */
  PredicateGroup getPredicateGroup();

  Map<String, String> getParams();

}