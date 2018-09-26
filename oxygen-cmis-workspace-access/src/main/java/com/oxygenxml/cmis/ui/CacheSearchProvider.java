package com.oxygenxml.cmis.ui;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.SwingUtilities;

import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.plugin.TranslationResourceController;

/**
 * Class thats gets the line asynchronously and then repainting the component of
 * the JLlist
 * 
 * @author bluecc
 *
 */
public class CacheSearchProvider implements ContentSearcher {

  private  final String timeCreated;
  private final String emptyResult;
  private final ContentSearcher searchProvider;
  private final JList<IResource> list;

  private final HashMap<String, String> cacheLine;
  private final HashMap<String, String> cachePath;
  private final HashMap<String, String> cacheProperties;
  private final HashMap<String, String> cacheName;

  private final Timer timer = new Timer(false);

  /**
   * Initialize the data
   * 
   * @param searchProvider
   * @param list
   */
  CacheSearchProvider(ContentSearcher searchProvider, JList<IResource> list) {
    timeCreated = TranslationResourceController.getMessage("TIME_CREATED");
    emptyResult = TranslationResourceController.getMessage("EMPTY_RESULT");
    
    cacheLine = new HashMap<>();
    cachePath = new HashMap<>();
    cacheProperties = new HashMap<>();
    cacheName = new HashMap<>();
    this.searchProvider = searchProvider;
    this.list = list;

  }

  @Override
  public String getLineDoc(IResource doc, String matchPattern) {
    // Get the line found by ID
    String lineDoc = cacheLine.get(doc.getId());

    if (lineDoc == null && doc.getId() != null) {

      TimerTask task = new TimerTask() {
        @Override
        public void run() {

          if (doc != null && doc.getId() != null) {
            // Get the line
            String line = searchProvider.getLineDoc(doc, matchPattern);

            // Put it in the hashmap or put 'Empty'
            cacheLine.put(doc.getId(), line != null ? line : null);

            // Repaint later the component
            SwingUtilities.invokeLater(() -> {

              int index = ((DefaultListModel<IResource>) list.getModel()).indexOf(doc);
              Rectangle cellBounds = list.getCellBounds(index, index);

              if (cellBounds != null) {
                list.repaint(cellBounds);
              }

            });
          }
        }
      };

      timer.schedule(task, 0);
    }

    return lineDoc;
  }

  @Override
  public String getPath(IResource resource, ResourceController ctrl) {
    // Get the line found by ID
    String pathDoc = cachePath.get(resource.getId());

    if (pathDoc == null) {

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
          cachePath.put(resource.getId(), path != null ? path : emptyResult);

          // Repaint later the component
          SwingUtilities.invokeLater(() -> {
            int index = ((DefaultListModel<IResource>) list.getModel()).indexOf(resource);
            Rectangle cellBounds = list.getCellBounds(index, index);
            if (cellBounds != null) {
              list.repaint(cellBounds);
            }
          });

        }
      };

      timer.schedule(task, 0);

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

  @Override
  public String getProperties(IResource resource) {
    // Get the line found by ID
    String propertiesToShow = cacheProperties.get(resource.getId());

    if (propertiesToShow == null) {

      TimerTask task = new TimerTask() {
        @Override
        public void run() {

          // Get the line
          String properties = null;
          if (resource instanceof DocumentImpl) {
            DocumentImpl doc = (DocumentImpl) resource;
            properties = doc.getModifiedBy();

          } else if (resource instanceof FolderImpl) {
            FolderImpl folder = (FolderImpl) resource;
            properties = timeCreated + folder.getTimeCreated();

          }

          // Put it in the hashmap or put 'Empty'
          cacheProperties.put(resource.getId(), properties != null ? properties : emptyResult);

          // Repaint later the component
          SwingUtilities.invokeLater(() -> {

            int index = ((DefaultListModel<IResource>) list.getModel()).indexOf(resource);
            Rectangle cellBounds = list.getCellBounds(index, index);

            if (cellBounds != null) {
              list.repaint(cellBounds);
            }
          });

        }
      };

      timer.schedule(task, 0);
    }
    return propertiesToShow;
  }

  @Override
  public String getName(IResource resource) {
    // Get the line found by ID

    String id = resource.getId();

    String name = cacheName.get(id);

    if (name == null) {

      TimerTask task = new TimerTask() {
        @Override
        public void run() {
          // Get the name
          String name = null;
          name = resource.getDisplayName().replace(" (Working Copy)", "").trim();

          // Put it in the hashmap or put 'Empty'
          cacheName.put(resource.getId(), name != null ? name : emptyResult);

          // Repaint later the component
          SwingUtilities.invokeLater(() -> {

            int index = ((DefaultListModel<IResource>) list.getModel()).indexOf(resource);
            Rectangle cellBounds = list.getCellBounds(index, index);

            if (cellBounds != null) {
              list.repaint(cellBounds);
            }
          });

        }
      };

      timer.schedule(task, 0);
    }
    return name;
  }
}