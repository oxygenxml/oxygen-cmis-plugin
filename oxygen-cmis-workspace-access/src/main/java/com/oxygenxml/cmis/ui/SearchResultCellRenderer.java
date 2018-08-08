package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

import sun.swing.DefaultLookup;

public class SearchResultCellRenderer extends JPanel implements ListCellRenderer<IResource> {

  private JPanel iconPanel;
  private JLabel iconLabel;

  private JPanel descriptionPanel;
  private JLabel nameRsource;
  private JLabel pathResource;
  private JLabel textResource;

  private JPanel notifierPanel;
  private JLabel notification;

  public SearchResultCellRenderer() {
    setLayout(new BorderLayout());

    // Drawing will occur in paintComponent
    iconPanel = new JPanel(new BorderLayout());
    iconLabel = new JLabel();
    iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/tip.png")));

    // Description panel
    descriptionPanel = new JPanel();
    descriptionPanel.setLayout(new GridLayout(3, 1, 1, 0));

    nameRsource = new JLabel();
    pathResource = new JLabel();
    textResource = new JLabel();
    descriptionPanel.add(nameRsource);
    descriptionPanel.add(pathResource);
    descriptionPanel.add(textResource);

    // Notification panel
    notifierPanel = new JPanel(new BorderLayout());

    // Will be drawn in paintComponent
    notification = new JLabel();
    notifierPanel.add(notification);

    add(iconLabel, BorderLayout.WEST);
    add(descriptionPanel, BorderLayout.CENTER);
    add(notifierPanel, BorderLayout.EAST);

  }

  @Override
  public Component getListCellRendererComponent(JList<? extends IResource> list, IResource value, int index,
      boolean isSelected, boolean cellHasFocus) {

    ResourceController ctrl = CMISAccess.getInstance().createResourceController();
    String pathValue = null;

    setComponentOrientation(list.getComponentOrientation());

    Color bg = null;
    Color fg = null;

    nameRsource.setText(value.getDisplayName());

    if (value instanceof DocumentImpl) {
      
      DocumentImpl doc = ((DocumentImpl) value);
      pathValue = doc.getDocumentPath(ctrl);
      //TODO: use breadcrumb view for the path
      
//      textResource.setText(doc.getDoc().getContentStream().toString());

    } else if (value instanceof FolderImpl) {

      pathValue = ((FolderImpl) value).getFolderPath();
    }

    pathResource.setText(pathValue);
    
    
    JList.DropLocation dropLocation = list.getDropLocation();
    if (dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == index) {

      bg = DefaultLookup.getColor(this, ui, "List.dropCellBackground");
      fg = DefaultLookup.getColor(this, ui, "List.dropCellForeground");

      isSelected = true;
    }

    if (isSelected) {
      setBackgroundC(this, bg == null ? list.getSelectionBackground() : bg);
      setForegroundC(this, fg == null ? list.getSelectionForeground() : fg);
    } else {
      setBackgroundC(this, list.getBackground());
      setForegroundC(this, list.getForeground());
    }

    return this;
  }

  private void setBackgroundC(Component c, Color background) {
    c.setBackground(background);

    if (c instanceof Container) {
      Component[] components = ((Container) c).getComponents();
      for (int i = 0; i < components.length; i++) {
        Component child = components[i];

        setBackgroundC(child, background);
      }
    }
  }

  private void setForegroundC(Component c, Color foreground) {
    c.setForeground(foreground);

    if (c instanceof Container) {
      Component[] components = ((Container) c).getComponents();
      for (int i = 0; i < components.length; i++) {
        Component child = components[i];

        setForegroundC(child, foreground);
      }
    }
  }

}
