package com.oxygenxml.cmis.ui;

import static org.junit.Assert.*;

import org.junit.Test;

import com.oxygenxml.cmis.ui.SearchResultCellRenderer;

public class RenderingTest {

  @Test
  public void testHTML() throws Exception {
    String content = "flowers (Working Copy).ditamap";
    String[] searchKeys = { "dita" };
    String styleHTML = SearchResultCellRenderer.getReadyHTMLSplit(content, searchKeys);

    assertEquals("flowers (Working Copy).<nobr style=' background-color:yellow; color:gray'>dita</nobr>map",
        styleHTML);

  }

}
