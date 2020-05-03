package com.adobe.aem.poc.azuresearch.components;

import com.adobe.cq.export.json.ComponentExporter;

public interface Component extends ComponentExporter {
  /**
   * @return true if the component is in a valid/configured/ready state and can be rendered, else
   *         return false to indicate to the component to display the placeholder text.
   */
  boolean isReady();
}