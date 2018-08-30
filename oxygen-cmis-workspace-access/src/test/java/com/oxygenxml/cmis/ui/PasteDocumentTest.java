package com.oxygenxml.cmis.ui;

import static org.junit.Assert.*;

import org.junit.Test;

import com.oxygenxml.cmis.actions.PasteDocumentAction;

public class PasteDocumentTest {

  @Test
  public void String() throws Exception {

    String getClipboard = PasteDocumentAction.getSysClipboardText();

    //assertEquals("11", getClipboard);

  }

}
