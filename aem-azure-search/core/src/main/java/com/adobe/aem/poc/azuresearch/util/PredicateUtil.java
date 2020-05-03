package com.adobe.aem.poc.azuresearch.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import com.adobe.aem.poc.azuresearch.components.predicates.Predicate;
import com.adobe.cq.wcm.core.components.models.form.OptionItem;

/**
 * Utility class that helps with common patterns found in Predicate implementations.
 */
public final class PredicateUtil {
  private PredicateUtil() {}

  /**
   *
   * @param request the request object.
   * @param parameterName The request query parameter name.
   * @return a String representation of the query parameter parameterName. If no request parameter
   *         can be found with that name, the empty string is returned.
   */
  public static String getParamFromQueryParams(final SlingHttpServletRequest request,
      final String parameterName) {
    final RequestParameter requestParameter = request.getRequestParameter(parameterName);

    if (requestParameter != null) {
      return requestParameter.getString();
    } else {
      return "";
    }
  }

  /**
   * Determines if the optionItem's value exists as a value in the initialValues map.
   *
   * @param optionItem the option item.
   * @param initialValues the initial values.
   * @return true if optionItem's value is in initialValues.
   */
  public static boolean isOptionInInitialValues(final OptionItem optionItem,
      final ValueMap initialValues) {
    return isOptionInInitialValues(optionItem.getValue(), initialValues);
  }

  /**
   * Determines if the value exists as a value in the initialValues map.
   *
   * Only String and String[] intialValues values are supported.
   *
   * @param value the value to check.
   * @param initialValues the initial values.
   * @return true if value is in initialValues.
   */
  public static boolean isOptionInInitialValues(final String value, final ValueMap initialValues) {
    for (final String key : initialValues.keySet()) {
      if (isValueInValueMap(value, initialValues.get(key))) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param needle the value to check for.
   * @param haystack they values to check if the needle is in (must be String or String[]).
   * @return true is the needle exists in the haystack.
   */
  private static boolean isValueInValueMap(final String needle, final Object haystack) {
    boolean found = false;

    if (haystack instanceof String) {
      found = StringUtils.equals((String) haystack, needle);
    } else if (haystack instanceof String[]) {
      found = ArrayUtils.contains((String[]) haystack, needle);
    }

    return found;
  }

  /**
   * Finds initial predicate value from the request.
   *
   * @param request the request.
   * @param predicate the predicate.
   * @param predicateValueName the predicate value name.
   * @return a map of the initial values.
   */
  public static String getInitialValue(final SlingHttpServletRequest request,
      final Predicate predicate, final String predicateValueName) {
    RequestParameter requestParameter = request.getRequestParameter(
        predicate.getGroup() + "." + predicate.getName() + "." + predicateValueName);

    if (requestParameter == null && (StringUtils.equals(predicate.getName(), predicateValueName)
        || StringUtils.isBlank(predicateValueName))) {
      requestParameter =
          request.getRequestParameter(predicate.getGroup() + "." + predicate.getName());
    }

    if (requestParameter != null) {
      return requestParameter.getString();
    } else {
      return "";
    }
  }

  /**
   * Finds initial predicate values from the request.
   *
   * @param request the request.
   * @param predicate the predicate.
   * @param predicateValueName the predicate value name.
   * @return a map of the initial values.
   */
  public static ValueMap getInitialValues(final SlingHttpServletRequest request,
      final Predicate predicate, final String predicateValueName) {
    final ValueMap valuesFromRequest = new ValueMapDecorator(new HashMap<>());

    final Pattern p = Pattern.compile("^(" + predicate.getGroup() + "\\.)?(\\d+_)?"
        + predicate.getName() + "(\\.((\\d+_)?" + predicateValueName + "))?$");

    for (final Map.Entry<String, RequestParameter[]> entry : request.getRequestParameterMap()
        .entrySet()) {
      List<String> values = new ArrayList<>();
      final Matcher matcher = p.matcher(entry.getKey());

      if (matcher.matches()) {
        values = Arrays.stream(entry.getValue()).map(RequestParameter::getString)
            .filter(StringUtils::isNotBlank).collect(Collectors.toList());
      }

      if (!values.isEmpty()) {
        valuesFromRequest.put(entry.getKey(), values.toArray(new String[values.size()]));
      }
    }

    return valuesFromRequest;
    }

  /**
   * Finds the QueryBuilder map entries that pertain to the parameterized predicate.
   *
   * This is helpful to find out what/which parameters are already present in a parameter map.
   *
   * @param queryBuilderParams the params to search through.
   * @param predicateName the predicate name to find.
   * @param predicateValueName the predicateName's predicate value to find. If null, it is set to
   *        the predicateName.
   * @return a map that contains the QueryBuilder params (keys and values) that match the
   *         predicateName/predicateValueName param pair.
   */
  public static ValueMap findPredicate(final Map<String, String> queryBuilderParams,
      final String predicateName, String predicateValueName) {
    if (predicateValueName == null) {
      predicateValueName = predicateName;
    }

    final ValueMap foundPredicates = new ValueMapDecorator(new HashMap<>());

    final Pattern p = Pattern.compile("^((\\d+_)?group\\.)?(\\d+_)?" + predicateName
        + "(\\.((\\d+_)?" + predicateValueName + "))?$");

    queryBuilderParams.keySet().stream().filter(key -> p.matcher(key).matches())
        .forEach(key -> foundPredicates.put(key, queryBuilderParams.get(key)));

    return foundPredicates;
    }
}