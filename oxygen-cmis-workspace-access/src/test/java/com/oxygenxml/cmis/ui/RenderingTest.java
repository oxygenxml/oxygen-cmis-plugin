package com.oxygenxml.cmis.ui;

import static org.junit.Assert.*;

import org.junit.Test;

import com.oxygenxml.cmis.ui.SearchResultCellRenderer;

public class RenderingTest {
  
  @Test
  public void testHTML() throws Exception {
    String contnet = "<xref>ceva si inca ceva</xref>";
    
    String escapeHTML = SearchResultCellRenderer.escapeHTML(contnet);
    
    assertEquals("&#60;xref&#62;ceva si inca ceva&#60;/xref&#62;", escapeHTML);
    
    String readyHTMLSplit = SearchResultCellRenderer.getReadyHTMLSplit(escapeHTML, "ceva");
//    
//    assertEquals("&#60;xref&#62;"
//        + "<nobr style='overflow:auto; padding: 5px; background-color:yellow; color:gray'>ceva</nobr>"
//        + " si inca <nobr style='overflow:auto; padding: 5px; background-color:yellow; color:gray'>ceva</nobr>"
//        + "&#60;/xref&#62;", readyHTMLSplit);
  }

}
