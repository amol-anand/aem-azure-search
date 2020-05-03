package com.adobe.aem.poc.azuresearch.components.predicates.impl.options;

import com.adobe.cq.wcm.core.components.models.form.OptionItem;

public class UnselectedOptionItem implements OptionItem {

  private OptionItem wrappedOptionItem = null;

  public UnselectedOptionItem(final OptionItem wrappedOptionItem) {
    this.wrappedOptionItem = wrappedOptionItem;
  }

  @Override
  public boolean isSelected() {
    return false;
  }

  @Override
  public boolean isDisabled() {
    return wrappedOptionItem.isDisabled();
  }

  @Override
  public String getValue() {
    return wrappedOptionItem.getValue();
  }

  @Override
  public String getText() {
    return wrappedOptionItem.getText();
  }
}
