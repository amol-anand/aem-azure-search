package com.adobe.aem.poc.azuresearch.components.predicates;

import java.util.Arrays;
import java.util.Comparator;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.wcm.core.components.models.form.Field;
import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.day.cq.wcm.commons.WCMUtils;

public abstract class AbstractPredicate implements Predicate {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractPredicate.class);

  private static final String REQUEST_ATTR_PREDICATE_GROUP_TRACKER =
      "azure-search__predicate-group";
  private static final String REQUEST_ATTR_LEGACY_PREDICATE_GROUP_TRACKER =
      "azure-search__legacy_predicate-group";

  private static final String REQUEST_ATTR_FORM_ID_TRACKER = "azure-search__form-id";
  private static final String PN_GENERATE_PREDICATE_GROUP_ID = "generatePredicateGroupId";

  private static final Integer INITIAL_GROUP_ID = 0;
  private static final Integer INITIAL_LEGACY_GROUP_ID = 10000 - 1;

  @Self
  @Required
  private SlingHttpServletRequest request;

  @ValueMapValue
  @Default(booleanValues = false)
  private boolean expanded;

  @ValueMapValue
  @Default(booleanValues = false)
  private boolean autoSearch;

  @ValueMapValue
  @Named("updateMethod")
  @Default(values = "")
  private String componentUpdateMethod;

  private int group = 1;

  private Field coreField;

  @Override
  public boolean isExpanded() {
    if (!expanded) {
      // Handling incoming request query params
      return StringUtils.isNotBlank(getInitialValue()) || !getInitialValues().isEmpty();
    }

    return expanded;
  }

  @Override
  public boolean isAutoSearch() {
    return autoSearch;
  }

  @Override
  public String getGroup() {
    return group + "_group";
  }

  @Override
  public String getInitialValue() {
    return null;
  }

  @Override
  public ValueMap getInitialValues() {
    return ValueMap.EMPTY;
  }

  @Override
  public String getId() {
    if (request.getResource() != null
        && !ResourceUtil.isNonExistingResource(request.getResource())) {
      return "cmp-" + getName() + "_" + String.valueOf(request.getResource().getPath().hashCode());
    } else {
      return "cmp-" + coreField.getId();
    }
  }

  @Override
  public String getComponentUpdateMethod() {
    return componentUpdateMethod;
  }

  /**
   * Core Field Component Delegates
   **/

  @Override
  public String getTitle() {
    return coreField.getTitle();
  }

  @Override
  public String getValue() {
    return coreField.getValue();
  }

  @Override
  public String getHelpMessage() {
    return coreField.getHelpMessage();
  }

  @Override
  public String getFormId() {
    if (request.getAttribute(REQUEST_ATTR_FORM_ID_TRACKER) == null) {
      request.setAttribute(REQUEST_ATTR_FORM_ID_TRACKER, 1);
    }

    return REQUEST_ATTR_FORM_ID_TRACKER + "__"
        + String.valueOf(request.getAttribute(REQUEST_ATTR_FORM_ID_TRACKER));
  }

  /**
   * @return true if the request appears to be a request that has search parameters.
   */
  public boolean isParameterizedSearchRequest() {
    return Arrays.stream(new String[] {"p.", "", "_group."})
        .anyMatch(needle -> StringUtils.contains(request.getQueryString(), needle));
  }

  /**
   * Initializer Methods.
   **/

  /**
   * Initializes the abstract predicate; This is used to: - Initialize the predicate group number
   * for the Model. - Initialize the Core Components Field Sling Model which the Azure search
   * Predicates Components delegate to.
   *
   * @param request the current SlingHttpServletRequest object
   * @param coreField the Core Components Field component (if the request can be adapted to one by
   *        the concrete implementing class).
   */
  protected final void initPredicate(final SlingHttpServletRequest request, final Field coreField) {
    this.coreField = coreField;
    initGroup(request);
  }

  /**
   * Initializes the predicate group number from the tracking request attribute, and increments for
   * the next Sling Model calling this method.
   *
   * @param request the current SlingHttpServletRequest object.
   */
  @SuppressWarnings("AEM Rules:AEM-15")
  protected synchronized final void initGroup(final SlingHttpServletRequest request) {
    /* Track Predicate Groups across Request */

    if (!isGroupIdGeneratingComponent(request) || !isReady() || !generateGroupId(request)) {
      generateLegacyGroupId(request);
    }
  }

  /**
   * @param request the Sling Http Request object.
   * @return true if the component is marked as generating a predicate group Id.
   */
  private boolean isGroupIdGeneratingComponent(final SlingHttpServletRequest request) {
    final com.day.cq.wcm.api.components.Component component =
        WCMUtils.getComponent(request.getResource());
    return component != null
        && component.getProperties().get(PN_GENERATE_PREDICATE_GROUP_ID, false);
  }

  /**
   * Set the groupId and set the request attribute.
   *
   * @param request the Sling Http Request object.
   * @return true if a group id was generated.
   */
  private boolean generateGroupId(final SlingHttpServletRequest request) {
    Object groupTracker = request.getAttribute(REQUEST_ATTR_PREDICATE_GROUP_TRACKER);

    if (groupTracker == null) {
      groupTracker = INITIAL_GROUP_ID;
    }

    if (groupTracker instanceof Integer) {
      group = (Integer) groupTracker + 1;
      request.setAttribute(REQUEST_ATTR_PREDICATE_GROUP_TRACKER, group);
      return true;
    }

    return false;
  }

  /**
   * Set the legacy groupId and set the request attribute.
   *
   * @param request the Sling Http Request object.
   */
  private void generateLegacyGroupId(final SlingHttpServletRequest request) {
    Object legacyGroupTracker = request.getAttribute(REQUEST_ATTR_LEGACY_PREDICATE_GROUP_TRACKER);

    if (legacyGroupTracker == null) {
      legacyGroupTracker = INITIAL_LEGACY_GROUP_ID;
    }

    if (legacyGroupTracker instanceof Integer) {
      group = (Integer) legacyGroupTracker + 1;
      request.setAttribute(REQUEST_ATTR_LEGACY_PREDICATE_GROUP_TRACKER, group);
    } else {
      group = -1;
    }
  }

  public class AlphabeticalOptionItems implements Comparator<OptionItem> {
    @Override
    public int compare(final OptionItem a, final OptionItem b) {
      return a.getText().compareTo(b.getText());
    }
  }
}