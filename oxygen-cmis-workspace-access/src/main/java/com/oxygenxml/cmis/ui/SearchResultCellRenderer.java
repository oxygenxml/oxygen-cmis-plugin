package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
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

  // Graphics configurations
  private boolean isSelected;

  public SearchResultCellRenderer() {
    setLayout(new BorderLayout());

    // Drawing will occur in paintComponent
    iconPanel = new JPanel(new BorderLayout());
    iconLabel = new JLabel();
    iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/tip.png")));
    iconPanel.add(iconLabel);
    iconPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

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

    add(iconPanel, BorderLayout.WEST);
    add(descriptionPanel, BorderLayout.CENTER);
    add(notifierPanel, BorderLayout.EAST);

  }

  @Override
  public Component getListCellRendererComponent(JList<? extends IResource> list, IResource value, int index,
      boolean isSelected, boolean cellHasFocus) {
    // Initialize the graphics configurations for the cell
    this.isSelected = isSelected;

    ResourceController ctrl = CMISAccess.getInstance().createResourceController();
    String pathValue = null;

    setComponentOrientation(list.getComponentOrientation());

    Color bg = null;
    Color fg = null;

    nameRsource.setText(value.getDisplayName());

    if (value instanceof DocumentImpl && value != null) {
      DocumentImpl doc = ((DocumentImpl) value);

      if (doc.isCheckedOut()) {
        iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/checkedout.png")));

      } else if (doc.isPrivateWorkingCopy()) {
        iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/workingcopy.png")));
      } else {

        iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/file.png")));
      }

      pathValue = doc.getDocumentPath(ctrl);

      // TODO: use breadcrumb view for the path

      // textResource.setText(doc.getDoc().getContentStream().toString());

    } else if (value instanceof FolderImpl && value != null)

    {
      iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/folder.png")));

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

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // if (isSelected) {
    // Area area = new Area(new Ellipse2D.Double(0, 0, 36, 36));
    // area.add(new Area(new RoundRectangle2D.Double(18, 3, getWidth() - 18, 29,
    // 6, 6)));
    // g2d.setPaint(currentColor);
    // g2d.fill(area);
    //
    // g2d.setPaint(Color.WHITE);
    // g2d.fill(new Ellipse2D.Double(2, 2, 32, 32));
    // }

    // g2d.drawImage(icon.getImage(), 5 + 13 - icon.getIconWidth() / 2, 5 + 13 -
    // icon.getIconHeight() / 2, null);

    // g2d.setPaint(currentColor);
    // g2d.fill(new Ellipse2D.Double(getWidth() - 18 - 5, getHeight() / 2 - 9,
    // 18, 18));

    // final String text = "" + notification.getText();
    // final Font oldFont = g2d.getFont();
    // g2d.setFont(oldFont.deriveFont(oldFont.getSize() - 1f));
    // final FontMetrics fm = g2d.getFontMetrics();
    // g2d.setPaint(Color.WHITE);
    // g2d.drawString(text, getWidth() - 9 - 5 - fm.stringWidth(text) / 2,
    // getHeight() / 2 + (fm.getAscent() - fm.getLeading() - fm.getDescent()) /
    // 2);
    // g2d.setFont(oldFont);

  }
}
