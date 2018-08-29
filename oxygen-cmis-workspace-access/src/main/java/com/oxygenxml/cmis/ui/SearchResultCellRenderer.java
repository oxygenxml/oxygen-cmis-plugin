package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

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
  private JPanel lineResourcePanel;

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
    c.insets = new Insets(5, 10, 0, 10);
    c.anchor = GridBagConstraints.BASELINE_LEADING;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    pathResource = new JLabel();
    descriptionPanel.add(pathResource, c);

    lineResourcePanel = new JPanel(new BorderLayout());
    lineResourcePanel.setPreferredSize(new java.awt.Dimension(100, 70));
    c.gridx = 0;
    c.gridy = 2;
    c.insets = new Insets(0, 10, 5, 10);
    c.anchor = GridBagConstraints.CENTER;
    c.weightx = 1;
    c.weighty = 1;
    c.gridheight = 2;
    c.fill = GridBagConstraints.BOTH;
    lineResource = new JLabel();

    // lineResource.setContentType("text/html");
    lineResourcePanel.add(lineResource, BorderLayout.CENTER);
    lineResource.addComponentListener(new ComponentListener() {

      @Override
      public void componentShown(ComponentEvent e) {
        // TODO Auto-generated method stub
        // lineResource.setFont(new Font("Serif", style, size));
      }

      @Override
      public void componentResized(ComponentEvent e) {
        // TODO Auto-generated method stub

      }

      @Override
      public void componentMoved(ComponentEvent e) {
        // TODO Auto-generated method stub

      }

      @Override
      public void componentHidden(ComponentEvent e) {
        // TODO Auto-generated method stub

      }
    });
    descriptionPanel.add(lineResourcePanel, c);

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
    String resourceText = styleString(value.getDisplayName());

    nameResource.setText("<html><div style=' overflow-wrap: break-word; word-wrap: break-word; background-color:red;'>"
        + (resourceText != null ? resourceText : "No data") + "</div></html>");

    if (value instanceof DocumentImpl && value != null) {

      DocumentImpl doc = ((DocumentImpl) value);

      if (doc.isPrivateWorkingCopy() && doc.isCheckedOut()) {

        iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/workingcopy.png")));
        // System.out.println("DocPWC:" + doc.getDisplayName());

      } else if (doc.isCheckedOut()) {

        iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/checkedout.png")));
        // System.out.println("Doc:" + doc.getDisplayName());

      } else {

        iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/file.png")));

      }

      pathValue = contentProv.getPath(doc, ctrl);

      notifyValue = "By:" + doc.getCreatedBy();
      // TODO: use breadcrumb view for the path

      // System.out.println("Line=" + contentProv.getLineDoc(doc,
      // matchPattern));

      // Get the results from the server
      String resultContext = contentProv.getLineDoc(doc, matchPattern);

      resultContext = styleString(resultContext);

      lineResource.setText(
          "<html><code style=' overflow-wrap: break-word; word-wrap: break-word; margin: 5px; padding: 5px; background-color:red;text-align: center;vertical-align: middle;'>"
              + (resultContext != null ? resultContext : "No data") + "</code></html>");

    } else if (value instanceof FolderImpl && value != null) {

      FolderImpl folder = ((FolderImpl) value);
      iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/folder.png")));

      notifyValue = "By:" + folder.getCreatedBy();
      pathValue = contentProv.getPath(folder, ctrl);

      lineResource.setText(
          "<html><code style=' overflow-wrap: break-word; word-wrap: break-word; margin: 5px; padding: 5px; background-color:red;text-align: center;vertical-align: middle;'>"
              + "No data" + "</code></html>");

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

  private String styleString(String resultContext) {
    // Check if there is some data
    if (resultContext != null) {

      // Escape the HTML
      resultContext = escapeHTML(resultContext);

      System.out.println("Before split = " + resultContext);
      // Check if there is something in searchbar
      if (matchPattern != null) {
        // Split the words entered as keys
        String[] searchKeys = matchPattern.trim().split("\\s+");

        // Get the styled HTML splitted
        resultContext = getReadyHTMLSplit(resultContext, searchKeys);
      }

      System.out.println("After split = " + resultContext);
    }
    return resultContext;
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
  public static void printMatches(String text, String regex) {
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(text);
    // Check all occurrences
    while (matcher.find()) {
      System.out.print("Start index: " + matcher.start());
      System.out.print(" End index: " + matcher.end());
      System.out.println(" Found: " + matcher.group());
    }
  }

  /**
   * Check the occurrences of a string in a string by checking the index and
   * then move next up to the length of the findStr
   * 
   * @param str
   * @param findStr
   * @return
   */
  static ArrayList<String> keysOrder = new ArrayList<String>();

  // static Stack<Integer> getOccurencesIndexes(String str, String[] findStr) {
  // String regex = "";
  // for (String string : findStr) {
  // regex += string + "|";
  // }
  //
  // keysOrder.clear();
  //
  // Stack<Integer> indexes = new Stack<Integer>();
  //
  // // Matters to preserve the order of the keys
  // Pattern pattern = Pattern.compile(regex);
  // Matcher matcher = pattern.matcher(str);
  // // Check all occurrences
  // while (matcher.find()) {
  // String found = matcher.group();
  //
  // if (!found.equals("")) {
  // System.out.print("Start index: " + matcher.start());
  // System.out.print(" End index: " + matcher.end());
  // System.out.println(" Found: " + found.trim());
  // }
  // }
  //
  // return indexes;
  // }

  /**
   * Count the occurrences of each key, splits the string for that specific key
   * and sets a style in that string with HTML 4. As long as there are keys left
   * the string will get updated values.
   * 
   * @param context
   * @param searchKeys
   * @return The styled string to be showed
   */
  static String getReadyHTMLSplit(String context, String[] searchKeys) {
    // String toReturn = "";
    String contextToSplit = context;
    StringBuffer stBuffer = new StringBuffer(contextToSplit);
    String styledMatch = "";

    Stack<Integer> occurencesIndexes = new Stack<Integer>();
    System.out.println("COntext=" + stBuffer.toString() + " Size =" + (stBuffer.length() - 1));

    // All the indexes of the keys
    // occurencesIndexes = getOccurencesIndexes(stBuffer.toString(),
    // searchKeys);
    // // Collections.sort(occurencesIndexes);

    String regex = "";
    for (String string : searchKeys) {
      regex += string + "|";
    }

    Stack<ObjectFound> foundObjects = new Stack<ObjectFound>();

    // Matters to preserve the order of the keys
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(stBuffer.toString());
    // Check all occurrences
    while (matcher.find()) {
      String found = matcher.group();

      if (!found.equals("")) {
        int startIndex = matcher.start();
        int endIndex = matcher.end();

        foundObjects.push(new ObjectFound(startIndex, endIndex, found.trim()));
        System.out.print("Start index: " + startIndex);
        System.out.print(" End index: " + endIndex);
        System.out.println(" Found: " + found.trim());
      }
    }

    System.out.println("Occurences=" + occurencesIndexes.size());

    while (!foundObjects.isEmpty()) {
      ObjectFound element = foundObjects.peek();
      styledMatch = "<nobr style=' background-color:yellow; color:gray'>" + element.getContent() + "</nobr>";
      System.out.println("Index from list=" + element.getStartIndex());
      System.out.println("Till = " + element.getEndIndex() + " The key =" + element.getContent());

      stBuffer.replace(element.getStartIndex(), element.getEndIndex(), styledMatch);
      foundObjects.pop();
    }

    // for (int index = 0; index < occurencesIndexes.size(); index++) {
    //
    // styledMatch = "<nobr style=' background-color:yellow; color:gray'>" +
    // keysOrder.get(index) + "</nobr>";
    // System.out.println("Index from list=" + occurencesIndexes.get(index));
    // System.out.println("Till = " + (occurencesIndexes.get(index) +
    // keysOrder.get(index).length() - 1) + " The key ="
    // + keysOrder.get(index));
    //
    // stBuffer.replace(occurencesIndexes.get(index),
    // occurencesIndexes.get(index) + keysOrder.get(index).length(),
    // styledMatch);
    //
    // }
    System.out.println(" FinalCOntext=" + stBuffer.toString());

    return stBuffer.toString();
  }

  /**
   * Escapes the HTML by replacing the signs with their codename
   * 
   * @param s
   * @return
   */
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

class ObjectFound {
  private int startIndex = 0;
  private int endIndex = 0;
  private String content = "";

  public ObjectFound(int startIndex, int endIndex, String content) {
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    this.content = content;
  }

  public int getStartIndex() {
    return this.startIndex;

  }

  public int getEndIndex() {
    return this.endIndex;

  }

  public String getContent() {
    return this.content;

  }
}