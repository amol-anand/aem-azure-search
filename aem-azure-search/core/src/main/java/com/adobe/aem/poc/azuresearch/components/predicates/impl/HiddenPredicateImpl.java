package com.adobe.aem.poc.azuresearch.components.predicates.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.factory.ModelFactory;

import com.adobe.aem.poc.azuresearch.components.predicates.AbstractPredicate;
import com.adobe.aem.poc.azuresearch.components.predicates.HiddenPredicate;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.day.cq.search.PredicateConverter;
import com.day.cq.search.PredicateGroup;

@Model(adaptables = {SlingHttpServletRequest.class},
    adapters = {HiddenPredicate.class, ComponentExporter.class},
    resourceType = {HiddenPredicateImpl.RESOURCE_TYPE})

@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME,
    extensions = ExporterConstants.SLING_MODEL_EXTENSION)

public class HiddenPredicateImpl extends AbstractPredicate implements HiddenPredicate {
  protected static final String RESOURCE_TYPE = "azuresearch/components/search/hidden";

  private static final String NN_PREDICATES = "predicates";
  private static final String PN_PREDICATE = "predicate";
  private static final String PN_VALUE = "value";

  @Self
  @Required
  private SlingHttpServletRequest request;

  @SlingObject
  @Required
  private Resource resource;

  @OSGiService
  private ModelFactory modelFactory;

  @Override
  public boolean isReady() {
    // Hidden properties should never display to the end-user
    return false;
  }

  @Override
  public PredicateGroup getPredicateGroup() {

    final PredicateGroup hiddenPredicateGroup = new PredicateGroup("hiddenPredicate");

    final Map<String, String> params = new HashMap<>();

    if (resource == null) {
      return hiddenPredicateGroup;
    }

    final Resource predicates = resource.getChild(NN_PREDICATES);

    if (predicates == null) {
      return hiddenPredicateGroup;
    }

    final Iterator<Resource> iterator = predicates.listChildren();

    while (iterator.hasNext()) {
      final Resource predicateResource = iterator.next();
      final ValueMap predicateProperties = predicateResource.getValueMap();

      final String predicate = predicateProperties.get(PN_PREDICATE, String.class);
      final String value = predicateProperties.get(PN_VALUE, "");

      if (StringUtils.isNotBlank(predicate)) {
        params.put(predicate, value);
      }
    }

    hiddenPredicateGroup.addAll(PredicateConverter.createPredicates(params));

    return hiddenPredicateGroup;
  }

  @Override
  public String getGroup() {
    throw new UnsupportedOperationException(
        "Hidden predicate groupIds are managed in the PagePredicateImpl automatically");
  }

  @Override
  public String getName() {
    throw new UnsupportedOperationException("Hidden predicates have no name");
  }

  @Override
  public Map<String, String> getParams() {
    final Map<String, String> params = new HashMap<>();

    if (resource == null) {
      return params;
    }

    final Resource predicates = resource.getChild(NN_PREDICATES);

    if (predicates == null) {
      return params;
    }

    final Iterator<Resource> iterator = predicates.listChildren();

    while (iterator.hasNext()) {
      final Resource predicateResource = iterator.next();
      final ValueMap predicateProperties = predicateResource.getValueMap();

      final String predicate = predicateProperties.get(PN_PREDICATE, String.class);
      final String value = predicateProperties.get(PN_VALUE, "");

      if (StringUtils.isNotBlank(predicate)) {
        params.put(predicate, value);
      }
    }

    return params;
    }

  @Override
  public String getExportedType() {
    return RESOURCE_TYPE;
  }
}