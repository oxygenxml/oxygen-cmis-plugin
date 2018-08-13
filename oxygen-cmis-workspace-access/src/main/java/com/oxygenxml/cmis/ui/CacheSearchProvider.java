package com.oxygenxml.cmis.ui;

import java.awt.Rectangle;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.SwingUtilities;

import com.oxygenxml.cmis.core.model.IResource;

public class CacheSearchProvider implements ContentSearchProvider {

  private ContentSearchProvider searchProvider;
  private JList<IResource> list;

  private HashMap<String, String> cache;

  CacheSearchProvider(ContentSearchProvider searchProvider, JList<IResource> list) {
    cache = new HashMap<>();
    this.searchProvider = searchProvider;
    this.list = list;

  }

  @Override
  public String getLineDoc(IResource doc, String matchPattern) {

    String lineDoc = cache.get(doc.getId());

    if (lineDoc == null) {

      new Thread(new Runnable() {
        
        public void run() {

          String line = searchProvider.getLineDoc(doc, matchPattern);

          cache.put(doc.getId(), line != null ? line : "Empty");
          
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              int index = ((DefaultListModel<IResource>) list.getModel()).indexOf(doc);
              Rectangle cellBounds = list.getCellBounds(index , index);
              
              list.repaint(cellBounds);
            }
          });
          

        }
      }).start();
    }

    return lineDoc;
  }

}
