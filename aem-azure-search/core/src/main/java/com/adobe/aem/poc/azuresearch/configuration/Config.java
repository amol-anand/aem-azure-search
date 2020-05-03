package com.adobe.aem.poc.azuresearch.configuration;

import java.util.Locale;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

/**
 * The model interface that represents the configuration for Azure search "site".
 *
 * Most of these configurations are derived via the configurations set on the Search Page's Page
 * Properties.
 */
public interface Config {

  /**
   * @return the SlingHttpServletRequest that resolves to this Config object.
   */
  SlingHttpServletRequest getRequest();

  /**
   * @return the ResourceResolver that resolved this Config object.
   */
  ResourceResolver getResourceResolver();

  ValueMap getProperties();

  /**
   * @return the Locale for the Page that resolves to this Config object.
   */
  Locale getLocale();

  /**
   * @return the absolute path Path to the resource resource (cq:Page) that resolves to this Config
   *         object.
   */
  String getRootPath();
}