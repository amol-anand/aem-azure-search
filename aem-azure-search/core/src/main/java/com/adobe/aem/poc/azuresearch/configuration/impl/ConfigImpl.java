package com.adobe.aem.poc.azuresearch.configuration.impl;

import java.util.Locale;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.featureflags.Features;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.factory.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.poc.azuresearch.configuration.Config;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.commons.WCMUtils;

@Model(adaptables = {SlingHttpServletRequest.class}, adapters = {Config.class})
public class ConfigImpl implements Config {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigImpl.class);

  // public static final String NODE_NAME = "config";

  private static final String[] rootResourceTypes =
      new String[] {"azuresearch/components/search/search-bar"};

  @Self
  @Required
  private SlingHttpServletRequest request;

  @OSGiService
  @Required
  private ModelFactory modelFactory;

  @SlingObject
  @Required
  private Resource requestResource;

  @OSGiService
  @Required
  private Features features;

  private Page currentPage;

  private ValueMap properties;


  @PostConstruct
  protected void init() {
    final ComponentContext componentContext = WCMUtils.getComponentContext(request);

    Resource pageResource = requestResource;
    if (componentContext != null && componentContext.getPage() != null) {
      pageResource = componentContext.getPage().getContentResource();
    }

    final PageManager pageManager = pageResource.getResourceResolver().adaptTo(PageManager.class);
    currentPage = pageManager.getContainingPage(pageResource);

    properties = currentPage.getContentResource().getValueMap();

  }

  @Override
  public SlingHttpServletRequest getRequest() {
    return request;
  }

  @Override
  public ResourceResolver getResourceResolver() {
    return request.getResourceResolver();
  }

  @Override
  public ValueMap getProperties() {
    return properties;
  }

  @Override
  public Locale getLocale() {
    if (currentPage != null) {
      return currentPage.getLanguage(false);
    } else {
      return Locale.getDefault();
    }
  }



  @Override
  public String getRootPath() {
    return getRootPath(currentPage);
  }

  /**
   * Finds the first root page (Search Page) for this Azure search page tree.
   *
   * @param currentPage
   * @return the path to the root page or / if none can be found
   */
  private String getRootPath(final Page currentPage) {
    final ResourceResolver resourceResolver = request.getResourceResolver();
    Page page = currentPage;

    do {
      if (page != null) {
        for (final String resourceType : rootResourceTypes) {
          if (page.getContentResource() != null
              && resourceResolver.isResourceType(page.getContentResource(), resourceType)) {
            return page.getPath();
          }

          page = page.getParent();
        }
      }
    } while (page != null);

    if (currentPage != null) {
      LOG.warn(
          "Could not find a valid Azure search root page for [ {} ]. Check to ensure a parent page sling:resourceSuperTypes one of [ {} ]",
          currentPage.getPath(), StringUtils.join(rootResourceTypes, ","));
    } else {
      LOG.warn(
          "Could not find a valid Azure search root page for because the current page could not be resolved.");
    }

    return "/";
  }
}