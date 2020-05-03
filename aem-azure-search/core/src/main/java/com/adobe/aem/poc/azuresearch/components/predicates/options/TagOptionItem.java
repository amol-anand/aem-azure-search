package com.adobe.aem.poc.azuresearch.components.predicates.options;

import java.util.Locale;

import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.day.cq.tagging.Tag;

public class TagOptionItem implements OptionItem {
  private final Tag tag;
  private final Locale locale;
  private final boolean selected;

  public TagOptionItem(final Tag tag, final Locale locale, final boolean selected) {
    this.tag = tag;
    this.locale = locale;
    this.selected = selected;
  }

  @Override
  public boolean isSelected() {
    return selected;
  }

  @Override
  public boolean isDisabled() {
    return false;
  }

  @Override
  public String getValue() {
    if (tag != null) {
      return tag.getTagID();
    }
    return "";
  }

  @Override
  public String getText() {
    if (tag != null) {
      return tag.getTitle(locale);
    }
    return "";
  }
}