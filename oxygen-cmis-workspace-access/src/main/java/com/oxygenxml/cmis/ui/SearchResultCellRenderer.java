package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

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

import ro.sync.exml.view.graphics.Dimension;
import sun.swing.DefaultLookup;

public class SearchResultCellRenderer extends JPanel implements ListCellRenderer<IResource> {
  private JPanel iconPanel;
  private JLabel iconLabel;

  private JPanel descriptionPanel;
  private JLabel nameResource;
  private JLabel pathResource;
  private JLabel lineResource;

  private JPanel notifierPanel;
  private JLabel notification;

  private ContentSearchProvider contentProv;

  // Graphics configurations
  private boolean isSelected;
  private String matchPattern;

  public SearchResultCellRenderer(ContentSearchProvider contentProvider, String matchPattern) {
    contentProv = contentProvider;
    this.matchPattern = matchPattern;

    setLayout(new BorderLayout());

    // Drawing will occur in paintComponent
    iconPanel = new JPanel(new BorderLayout());
    iconLabel = new JLabel();
    iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/tip.png")));
    iconPanel.add(iconLabel);
    iconPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Description panel
    descriptionPanel = new JPanel();
    descriptionPanel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(5, 10, 5, 10);
    c.anchor = GridBagConstraints.BASELINE_LEADING;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    nameResource = new JLabel();
    descriptionPanel.add(nameResource, c);

    c.gridx = 0;
    c.gridy = 1;
    c.insets = new Insets(5, 10, 5, 10);
    c.anchor = GridBagConstraints.BASELINE_LEADING;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    pathResource = new JLabel();
    descriptionPanel.add(pathResource, c);

    c.gridx = 0;
    c.gridy = 2;
    c.insets = new Insets(5, 10, 5, 10);
    c.anchor = GridBagConstraints.CENTER;
    c.weightx = 1;
    c.weighty = 1;
    c.gridheight = 2;
    c.fill = GridBagConstraints.BOTH;
    lineResource = new JLabel();
    descriptionPanel.add(lineResource, c);

    // Notification panel
    notifierPanel = new JPanel(new BorderLayout());
    notifierPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Will be drawn in paintComponent
    notification = new JLabel() {
      // @Override
      // protected void paintComponent(Graphics g) {
      // // TODO Auto-generated method stub
      //
      // Graphics2D g2d = (Graphics2D) g;
      // g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      // RenderingHints.VALUE_ANTIALIAS_ON);
      // super.paintComponent(g);
      // g2d.setPaint(Color.BLUE);
      // g2d.fill(new Ellipse2D.Double(getWidth() - 18 - 5, getHeight() / 2 - 9,
      // 18, 18));
      //
      // final String text = "" + notification.getText();
      // final Font oldFont = g2d.getFont();
      //
      // g2d.setFont(oldFont.deriveFont(oldFont.getSize() - 1f));
      //
      // final FontMetrics fm = g2d.getFontMetrics();
      // g2d.setPaint(Color.WHITE);
      // g2d.drawString(text, getWidth() - 9 - 5 - fm.stringWidth(text) / 2,
      // getHeight() / 2 + (fm.getAscent() - fm.getLeading() - fm.getDescent())
      // / 2);
      // g2d.setFont(oldFont);
      // }
    };
    notifierPanel.add(notification);

    add(iconPanel, BorderLayout.WEST);
    add(descriptionPanel, BorderLayout.CENTER);
    add(notifierPanel, BorderLayout.EAST);
    setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
  }

  @Override
  public Component getListCellRendererComponent(JList<? extends IResource> list, IResource value, int index,
      boolean isSelected, boolean cellHasFocus) {
    // Initialize the graphics configurations for the cell
    this.isSelected = isSelected;

    ResourceController ctrl = CMISAccess.getInstance().createResourceController();

    String pathValue = null;
    String notifyValue = null;

    setComponentOrientation(list.getComponentOrientation());

    Color bg = null;
    Color fg = null;

    nameResource.setText(value.getDisplayName());

    if (value instanceof DocumentImpl && value != null) {

      DocumentImpl doc = ((DocumentImpl) value);

      if (doc.isPrivateWorkingCopy() && doc.isCheckedOut()) {

        iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/workingcopy.png")));
        System.out.println("DocPWC:" + doc.getDisplayName());

      } else if (doc.isCheckedOut()) {

        iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/checkedout.png")));
        System.out.println("Doc:" + doc.getDisplayName());

      } else {

        iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/file.png")));

      }
      System.out.println();

      pathValue = contentProv.getPath(doc, ctrl);

      notifyValue = "By:" + doc.getCreatedBy();
      // TODO: use breadcrumb view for the path

      System.out.println("Line=" + contentProv.getLineDoc(doc, matchPattern));
      String resultContext = contentProv.getLineDoc(doc, matchPattern);

      if (resultContext != null) {
        resultContext = escapeHTML(resultContext);

        if (matchPattern != null) {
          resultContext = getReadyHTMLSplit(resultContext, matchPattern);
        }
      }

      lineResource.setText(
          "<html><div style='word-wrap: break-word; padding: 5px; background-color:red;text-align: center;vertical-align: middle;'>"
              + (resultContext != null ? resultContext : "Loading...") + "</div></html>");

    } else if (value instanceof FolderImpl && value != null) {

      FolderImpl folder = ((FolderImpl) value);
      iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/folder.png")));

      notifyValue = "By:" + folder.getCreatedBy();
      pathValue = contentProv.getPath(folder, ctrl);

      lineResource.setText("");

    }

    pathResource.setText(pathValue);
    notification.setText(notifyValue);

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

  // public ApplicationListResizeSensitive(boolean forwardSelection) {
  // super(forwardSelection);
  //
  // // The HTML content of the renderers may wrap to the list viewport
  // bounds, leading to
  // // a different height for the same value
  //
  // // Receives resize events and invalidates the list.
  // final ComponentAdapter parentComponentListener = new
  // ComponentAdapter() {
  //
  // @Override
  // public void componentResized(ComponentEvent e) {
  // size = e.getComponent().getSize();
  // clearRendererAllocationCache();
  // }
  // };
  //
  // addHierarchyListener(new HierarchyListener() {
  // /**
  // * A reference to the previous linked caret. Used to avoid
  // clearing the cache too often.
  // */
  // private Container oldParent = null;
  // @Override
  // public void hierarchyChanged(HierarchyEvent e) {
  // // Such events can be fired quite often. For example when
  //// changing the selected tab in a tabbed pane.
  //
  // Container parent = getParent();
  // if (parent != null
  // // A new parent.
  // && (parent != oldParent
  // // Just a precaution. The size of the parent changed. Maybe
  //// it can happen if inside
  // // a tabbed pane when switching.
  // || !ro.sync.basic.util.Equaler.verifyEquals(size,
  // parent.getSize()))) {
  // // Avoid linking multiple times.
  // parent.removeComponentListener(parentComponentListener);
  // parent.addComponentListener(parentComponentListener);
  // clearRendererAllocationCache();
  //
  // oldParent = parent;
  // }
  // }
  // });
  // }

  /**
   * Invalidates the bounds of the cells.
   */
  // public void clearRendererAllocationCache() {
  //
  // ListCellRenderer cellRenderer = getOriginalCellRenderer();
  // setCellRenderer(null);
  // setCellRenderer(cellRenderer);
  // }
  // protected void update(String htmlContent, boolean selected, boolean
  // isHovered, int htmlWidth) {
  // this.isSelected = selected;
  // this.isHovered = isHovered;
  // // Update the colors.
  //
  // StringBuilder html = new StringBuilder();
  // html.append("<html><body>");
  //
  // if (htmlWidth == -1) {
  // htmlWidth = getVisibleListWidth(0);
  // }
  //
  // html.append(" <table cellspacing='" + getRendererTableCellSpacing() + "'
  // cellpadding='0' width='")
  // .append(htmlWidth).append("'>");
  // html.append(htmlContent);
  // html.append("</table></body></html>");
  //
  // // Update the dimensions of the text label.
  // this.setBackground(UIManager.getColor("TextArea.background"));
  // this.setText(html.toString());
  //
  // View view = (View) this.getClientProperty(BasicHTML.propertyKey);
  // if (view != null) {
  // double w = view.getPreferredSpan(View.X_AXIS);
  // double h = view.getPreferredSpan(View.Y_AXIS);
  //
  // // Do not force label size beyond minimum size.
  // double minimumWidth = this.getMinimumSize().getWidth();
  // double minimumHeight = this.getMinimumSize().getHeight();
  // if (w < minimumWidth) {
  // w = minimumWidth;
  // }
  // if (h < minimumHeight) {
  // h = minimumHeight;
  // }
  //
  // this.setPreferredSize(new Dimension((int) w, (int) h));
  // } else {
  // this.setPreferredSize(new Dimension(10, 10));
  // }
  // }

  static String getReadyHTMLSplit(String context, String matchPattern) {
    String toReturn = "";

    System.out.println("COntext=" + context);
    System.out.println("Pattern=" + matchPattern);
    String[] splits = context.split(matchPattern);

    for (int index = 0; index < splits.length; index += 1) {

      String styledContext = splits[index];
      String styledMatch = "";
      if (index != splits.length - 1) {
        styledMatch = "<nobr style=' background-color:yellow; color:gray'>" + matchPattern + "</nobr>";
      }
      toReturn += (styledContext + styledMatch);
    }

    return toReturn;
  }

  public static String escapeHTML(String s) {
    StringBuilder out = new StringBuilder(Math.max(16, s.length()));
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
        out.append("&#");
        out.append((int) c);
        out.append(';');
      } else {
        out.append(c);
      }
    }
    return out.toString();
  }
}
