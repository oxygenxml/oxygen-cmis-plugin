package com.oxygenxml.cmis.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringStyleTest {
  @Test
  public void String() throws Exception {
    String content = "glossaryditaSepal.ditadita";
    String searchKeys = "glossary  dita";

    String getStyled = SearchResultCellRenderer.getReadyHTMLSplit(content, searchKeys);

    assertEquals(
        "<nobr style=' background-color:yellow; color:gray'>glossary</nobr><nobr style=' background-color:yellow; color:gray'>dita</nobr>Sepal.<nobr style=' background-color:yellow; color:gray'>dita</nobr><nobr style=' background-color:yellow; color:gray'>dita</nobr>",
        getStyled);

  }
}
