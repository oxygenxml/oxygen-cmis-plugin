package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.net.URL;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.chemistry.opencmis.client.api.Repository;

import com.oxygenxml.cmis.core.CMISAccess;

public class ListRepoView extends JPanel implements RepositoriesPresenter, ListSelectionListener {

  private JList<Repository> listRepo;
  private JList<String>listItems;
  private  DefaultListModel<Repository> model;
  private List<Repository> serverReposList;
  private ItemsPresenter itemsPresenter;
  private URL serverURL;
  
  ListRepoView(ItemsPresenter itemsPresenter) {

    this.itemsPresenter = itemsPresenter;
    
    listRepo = new JList();
    listRepo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listRepo.setSelectedIndex(0);
    listRepo.addListSelectionListener(this);
    // Scroller for the listRepo
    JScrollPane listRepoScrollPane = new JScrollPane(listRepo);

    // Setting the minum size for the SideList and MainList
    Dimension minimumSizeSideList = new Dimension(200, 100);

    listRepoScrollPane.setMinimumSize(minimumSizeSideList);

    setLayout(new BorderLayout());
    add(listRepoScrollPane, BorderLayout.CENTER);
  }

  @Override
  public void presentRepositories(URL serverURL) {
    this.serverURL = serverURL;
    // TODO Auto-generated method stub
    // Create the listRepo of repos.
    serverReposList = CMISAccess.getInstance().getRepositories(serverURL);

    DefaultListModel<Repository> model = new DefaultListModel<>();

    for (Repository element : serverReposList) {
      model.addElement(element);
    }
    listRepo.setModel(model);
    listRepo.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
          boolean cellHasFocus) {
        String renderTex = "";
        if (value != null) {
          renderTex = ((Repository) value).getName();
        }

        return super.getListCellRendererComponent(list, renderTex, index, isSelected, cellHasFocus);
      }
    });

  }

  public void valueChanged(ListSelectionEvent e) {
    
    if (!e.getValueIsAdjusting() && listRepo.getSelectedValue() != null) {
      itemsPresenter.presentItems(serverURL, listRepo.getSelectedValue().getId());
    }
   
  }
}
