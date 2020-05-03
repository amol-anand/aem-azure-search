package com.adobe.aem.poc.azuresearch.components.predicates.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.adobe.aem.poc.azuresearch.components.predicates.AbstractPredicate;
import com.adobe.aem.poc.azuresearch.components.predicates.PropertyPredicate;
import com.adobe.aem.poc.azuresearch.components.predicates.impl.options.SelectedOptionItem;
import com.adobe.aem.poc.azuresearch.components.predicates.impl.options.UnselectedOptionItem;
import com.adobe.aem.poc.azuresearch.search.impl.predicateevaluators.PropertyValuesPredicateEvaluator;
import com.adobe.aem.poc.azuresearch.util.PredicateUtil;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.adobe.cq.wcm.core.components.models.form.Options;

@Model(adaptables = {SlingHttpServletRequest.class},
    adapters = {PropertyPredicate.class, ComponentExporter.class},
    resourceType = {PropertyPredicateImpl.RESOURCE_TYPE},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)

@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME,
    extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class PropertyPredicateImpl extends AbstractPredicate implements PropertyPredicate, Options {

  protected static final String RESOURCE_TYPE = "azuresearch/components/search/property";
  protected static final String PN_TYPE = "type";

  protected String valueFromRequest = null;
  protected ValueMap valuesFromRequest = null;

  @Self
  @Required
  private SlingHttpServletRequest request;

  @Self
  @Required
  private Options coreOptions;

  @ValueMapValue
  private String label;

  @ValueMapValue
  private String property;

  @ValueMapValue
  private String operation;

  @ValueMapValue
  private Boolean expanded;

  @ValueMapValue(name = PropertyPredicateImpl.PN_TYPE)
  private String typeString;

  @ValueMapValue
  @Named("and")
  @Default(booleanValues = false)
  private boolean and;

  @PostConstruct
  protected void init() {
    initPredicate(request, coreOptions);
  }

  /* Options - Core Component Delegates */

  @Override
  public List<OptionItem> getItems() {
    final ValueMap initialValues = getInitialValues();
    final List<OptionItem> processedOptionItems = new ArrayList<>();
    final boolean useDefaultSelected = !isParameterizedSearchRequest();

    coreOptions.getItems().stream().forEach(optionItem -> {
      if (PredicateUtil.isOptionInInitialValues(optionItem, initialValues)) {
        processedOptionItems.add(new SelectedOptionItem(optionItem));
      } else if (useDefaultSelected) {
        processedOptionItems.add(optionItem);
      } else {
        processedOptionItems.add(new UnselectedOptionItem(optionItem));
      }
    });

    return processedOptionItems;
  }

  @Override
  public Type getType() {
    return coreOptions.getType();
  }

  /* Property Predicate Specific */

  @Override
  public String getSubType() {
    // support variation of Checkboxes
    return typeString;
  }

  @Override
  public String getProperty() {
    return property;
  }

  @Override
  public String getValuesKey() {
    return PropertyValuesPredicateEvaluator.VALUES;
  }

  @Override
  public boolean hasOperation() {
    return StringUtils.isNotBlank(getOperation());
  }

  @Override
  public String getOperation() {
    return operation;
  }

  @Override
  public boolean hasAnd() {
    return and;
  }

  @Override
  public Boolean getAnd() {
    return and;
  }

  @Override
  public String getName() {
    return PropertyValuesPredicateEvaluator.PREDICATE_NAME;
  }

  @Override
  public boolean isReady() {
    return !coreOptions.getItems().isEmpty();
  }

  @Override
  public String getInitialValue() {
    if (valueFromRequest == null) {
      valueFromRequest =
          PredicateUtil.getInitialValue(request, this, PropertyValuesPredicateEvaluator.VALUES);
    }

    return valueFromRequest;
  }

  @Override
  public ValueMap getInitialValues() {
    if (valuesFromRequest == null) {
      valuesFromRequest =
          PredicateUtil.getInitialValues(request, this, PropertyValuesPredicateEvaluator.VALUES);
    }

    return valuesFromRequest;
  }

  @Override
  public String getExportedType() {
    return RESOURCE_TYPE;
  }
}
