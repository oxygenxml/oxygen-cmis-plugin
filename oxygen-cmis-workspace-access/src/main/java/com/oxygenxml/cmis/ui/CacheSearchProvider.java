package com.oxygenxml.cmis.ui;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

import ro.sync.exml.editor.re;

/**
 * Class thats gets the line asynchronously and then repainting the component of
 * the JLlist
 * 
 * @author bluecc
 *
 */
public class CacheSearchProvider implements ContentSearchProvider {

  private ContentSearchProvider searchProvider;
  private JList<IResource> list;

  private HashMap<String, String> cacheLine;
  private HashMap<String, String> cachePath;

  private Timer timer = new Timer(false);

  /**
   * Initialize the data
   * 
   * @param searchProvider
   * @param list
   */
  CacheSearchProvider(ContentSearchProvider searchProvider, JList<IResource> list) {
    cacheLine = new HashMap<>();
    cachePath = new HashMap<>();
    this.searchProvider = searchProvider;
    this.list = list;

  }

  @Override
  public String getLineDoc(IResource doc, String matchPattern) {
    // Get the line found by ID
    String lineDoc = cacheLine.get(doc.getId());

    if (lineDoc == null) {

      TimerTask task = new TimerTask() {
        @Override
        public void run() {
          // Get the line
          String line = searchProvider.getLineDoc(doc, matchPattern);

          // Put it in the hashmap or put 'Empty'
          cacheLine.put(doc.getId(), line != null ? line : "");

          // Repaint later the component
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              int index = ((DefaultListModel<IResource>) list.getModel()).indexOf(doc);
              Rectangle cellBounds = list.getCellBounds(index, index);

              list.repaint(cellBounds);
            }
          });
        }
      };

      timer.schedule(task, 60);
    }

    return lineDoc;
  }

  @Override
  public String getPath(IResource resource, ResourceController ctrl) {
    // Get the line found by ID
    String pathDoc = cachePath.get(resource.getId());

    if (pathDoc == null) {

      // Initialize a new thread
      // new Thread(new Runnable() {
      //
      // public void run() {
      // // Get the line
      // String path = ((DocumentImpl) doc).getDocumentPath(ctrl);
      //
      // // Put it in the hashmap or put 'Empty'
      // cachePath.put(doc.getId(), path != null ? path : "");
      //
      // // Repaint later the component
      // SwingUtilities.invokeLater(new Runnable() {
      // @Override
      // public void run() {
      // int index = ((DefaultListModel<IResource>)
      // list.getModel()).indexOf(doc);
      // Rectangle cellBounds = list.getCellBounds(index, index);
      //
      // list.repaint(cellBounds);
      // }
      // });
      //
      // }
      // // Start the async thread
      // }).start();

      TimerTask task = new TimerTask() {
        @Override
        public void run() {
          // Get the line
          String path = null;
          if (resource instanceof DocumentImpl) {
            path = ((DocumentImpl) resource).getDocumentPath(ctrl);

          } else if (resource instanceof FolderImpl) {
            path = ((FolderImpl) resource).getFolderPath();

          }

          // Put it in the hashmap or put 'Empty'
          cachePath.put(resource.getId(), path != null ? path : "");

          // Repaint later the component
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              int index = ((DefaultListModel<IResource>) list.getModel()).indexOf(resource);
              Rectangle cellBounds = list.getCellBounds(index, index);

              list.repaint(cellBounds);
            }
          });

        }
      };

      timer.schedule(task, 60);

    }

    return pathDoc;
  }

  @Override
  public void addSearchListener(SearchListener searchListener) {
    searchProvider.addSearchListener(searchListener);
  }

  @Override
  public void doSearch(String searchText) {
    searchProvider.doSearch(searchText);
  }

}
