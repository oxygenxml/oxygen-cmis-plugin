package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IDocument;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

public class ListItemView extends JPanel implements ItemsPresenter, ListSelectionListener {
  private JList<IResource> listItem;
  private DefaultListModel<Repository> model;
  private List<Repository> repoList;
  private TabsPresenter tabsPresenter;

  
  
  
  ListItemView(TabsPresenter tabsPresenter) {
    this.tabsPresenter = tabsPresenter;

    listItem = new JList();
    listItem.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listItem.setSelectedIndex(0);
    listItem.addListSelectionListener(this);
    
    
    
    // Scroller for the listRepo
    JScrollPane listItemScrollPane = new JScrollPane(listItem);

    // Setting the minum size for the SideList and MainList
    Dimension minimumSizeSideList = new Dimension(200, 100);
    listItemScrollPane.setMinimumSize(minimumSizeSideList);
    

    listItem.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
          boolean cellHasFocus) {
        String renderTex = "";
        if (value != null) {
          renderTex = ((IResource) value).getDisplayName();
        }

        return super.getListCellRendererComponent(list, renderTex, index, isSelected, cellHasFocus);
      }
    });

    listItem.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub;
        if (e.getClickCount() == 2) {
          int itemIndex = listItem.locationToIndex(e.getPoint());

          if (itemIndex != -1) {
            IResource currentItem = listItem.getModel().getElementAt(itemIndex);
            Iterator<IResource> childrenIterator = currentItem.iterator();

            if (childrenIterator != null && childrenIterator.hasNext()) {
              DefaultListModel<IResource> model = new DefaultListModel<>();

              while (childrenIterator.hasNext()) {
                IResource iResource = (IResource) childrenIterator.next();
                model.addElement(iResource);

              }
              listItem.setModel(model);
              
            }

            if (currentItem instanceof DocumentImpl) {
              tabsPresenter.presentItem(((DocumentImpl)currentItem).getDoc());
            }
            
          }
        }
      }
    });

    setLayout(new BorderLayout());
    add(listItemScrollPane, BorderLayout.CENTER);
  }

  @Override
  public void presentItems(URL connectionInfo, String repositoryID) {

    CMISAccess instance = CMISAccess.getInstance();
    instance.connect(connectionInfo, repositoryID);
    Folder rootFolder = instance.createResourceController().getRootFolder();
    FolderImpl resource = new FolderImpl(rootFolder);

    DefaultListModel<IResource> model = new DefaultListModel<>();
    model.addElement(resource);
    listItem.setModel(model);

  }

  @Override
  public void valueChanged(ListSelectionEvent e) {
    // TODO Auto-generated method stub

  }

}
