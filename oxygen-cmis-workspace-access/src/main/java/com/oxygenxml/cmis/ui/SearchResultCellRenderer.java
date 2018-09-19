package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Icon;
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

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import sun.swing.DefaultLookup;

public class SearchResultCellRenderer extends JPanel implements ListCellRenderer<IResource> {
  private final JPanel iconPanel;
  private final JLabel iconLabel;

  private final JPanel descriptionPanel;
  private final JLabel nameResource;
  private final JLabel propertiesResource;
  private final JLabel lineResource;
  private final JPanel lineResourcePanel;

  private final JPanel notifierPanel;
  private final JLabel notification;

  private final ContentSearcher contentProv;

  // Graphics configurations
  private boolean isSelected;
  private final String matchPattern;

  public SearchResultCellRenderer(ContentSearcher contentProvider, String matchPattern) {
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
    final GridBagConstraints c = new GridBagConstraints();

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
    propertiesResource = new JLabel();
    descriptionPanel.add(propertiesResource, c);

    lineResourcePanel = new JPanel(new BorderLayout());
    lineResourcePanel.setPreferredSize(new java.awt.Dimension(100, 50));
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
    descriptionPanel.add(lineResourcePanel, c);

    // Notification panel
    notifierPanel = new JPanel(new BorderLayout());
    notifierPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Will be drawn in paintComponent
    notification = new JLabel();
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

    final ResourceController ctrl = CMISAccess.getInstance().createResourceController();

    String pathValue = null;
    String notifyValue = null;
    String propertiesValues = null;
    String resourceText = null;
    setComponentOrientation(list.getComponentOrientation());

    Color bg = null;
    Color fg = null;
    resourceText = contentProv.getName(value);

    if (resourceText != null) {
      resourceText = styleString(resourceText);
    }

    if (value instanceof DocumentImpl) {

      final DocumentImpl doc = ((DocumentImpl) value);
      if (doc.getId() != null) {

        if (!doc.isPrivateWorkingCopy() && doc.isCheckedOut()) {

          iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/padlock.png")));

        } else {

          try {
            iconLabel.setIcon((Icon) PluginWorkspaceProvider.getPluginWorkspace().getImageUtilities()
                .getIconDecoration(new URL("http://localhost/" + value.getDisplayName())));

          } catch (final MalformedURLException e) {

            iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/file.png")));
          }

        }

        pathValue = contentProv.getPath(doc, ctrl);
        propertiesValues = contentProv.getProperties(doc);
        notifyValue = "By:" + doc.getCreatedBy();

        // System.out.println("Line=" + contentProv.getLineDoc(doc,
        // matchPattern));

        // Get the results from the server
        String resultContext = contentProv.getLineDoc(doc, matchPattern);

        resultContext = styleString(resultContext);

        lineResource.setText(
            "<html><code style=' overflow-wrap: break-word; word-wrap: break-word; margin: 5px; padding: 5px; text-align: center;vertical-align: middle;'>"
                + (resultContext != null ? resultContext : "No data") + "</code></html>");

      }
    } else if (value instanceof FolderImpl && value != null) {

      final FolderImpl folder = ((FolderImpl) value);

      if (folder.getId() != null) {
        iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/folder.png")));

        pathValue = contentProv.getPath(folder, ctrl);
        propertiesValues = contentProv.getProperties(folder);
        notifyValue = "By:" + folder.getCreatedBy();

        lineResource.setText(
            "<html><code style=' overflow-wrap: break-word; word-wrap: break-word; margin: 5px; padding: 5px; text-align: center;vertical-align: middle;'>"
                + "No data" + "</code></html>");

      }
    }

    nameResource.setText("<html><div style=' overflow-wrap: break-word; word-wrap: break-word;'>"
        + (resourceText != null ? resourceText : "No data") + "</div></html>");

    propertiesResource.setOpaque(true);
    propertiesResource.setForeground(Color.GRAY);
    propertiesResource.setText((propertiesValues != null ? propertiesValues : "No data"));

    notification.setText(notifyValue);

    final JList.DropLocation dropLocation = list.getDropLocation();
    if (dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == index) {

      bg = DefaultLookup.getColor(this, ui, "List.dropCellBackground");
      fg = DefaultLookup.getColor(this, ui, "List.dropCellForeground");

      isSelected = true;
    }

    if (isSelected) {
      setToolTipText(pathValue);
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

      // System.out.println("Before split = " + resultContext);
      // Check if there is something in searchbar
      if (matchPattern != null) {
        // Split the words entered as keys
        final String[] searchKeys = matchPattern.trim().split("\\s+");

        // Get the styled HTML splitted
        resultContext = getReadyHTMLSplit(resultContext, searchKeys);
      }

      // System.out.println("After split = " + resultContext);
    }
    return resultContext;
  }

  private void setBackgroundC(Component c, Color background) {
    c.setBackground(background);

    if (c instanceof Container) {
      final Component[] components = ((Container) c).getComponents();
      for (int i = 0; i < components.length; i++) {
        final Component child = components[i];

        setBackgroundC(child, background);
      }
    }
  }

  private void setForegroundC(Component c, Color foreground) {
    c.setForeground(foreground);

    if (c instanceof Container) {
      final Component[] components = ((Container) c).getComponents();
      for (int i = 0; i < components.length; i++) {
        final Component child = components[i];

        setForegroundC(child, foreground);
      }
    }
  }

  /**
   * Matcher used to get all the star and end indexes for replacing the original
   * data
   * 
   * 
   * @param context
   * @param searchKeys
   * @return The styled string to be showed
   */
  static String getReadyHTMLSplit(String context, String[] searchKeys) {
    final String contextToSplit = context;
    final StringBuffer stBuffer = new StringBuffer(contextToSplit);
    String styledMatch = "";

    // System.out.println("COntext=" + stBuffer.toString() + " Size =" +
    // (stBuffer.length() - 1));

    // Concatenate all the keys from the search
    String regex = "";
    for (final String string : searchKeys) {
      regex += string + "|";
    }

    // Use a stack to store data because we will show them from the back in
    // order to not destroy the original string
    final Stack<ObjectFound> foundObjects = new Stack<ObjectFound>();

    // Matters to preserve the order of the keys
    final Pattern pattern = Pattern.compile(regex);
    final Matcher matcher = pattern.matcher(stBuffer.toString());

    // While some results are found
    while (matcher.find()) {
      final String found = matcher.group();

      // There is data
      if (!found.equals("")) {
        final int startIndex = matcher.start();
        final int endIndex = matcher.end();

        // Create a new object
        foundObjects.push(new ObjectFound(startIndex, endIndex, found.trim()));

        // System.out.print("Start index: " + startIndex);
        // System.out.print(" End index: " + endIndex);
        // System.out.println(" Found: " + found.trim());
      }
    }

    // Iterate all the objects from the stack
    while (!foundObjects.isEmpty()) {

      final ObjectFound element = foundObjects.peek();
      styledMatch = "<nobr style=' background-color:yellow; color:gray'>" + element.getContent() + "</nobr>";
      // System.out.println("Index from list=" + element.getStartIndex());
      // System.out.println("Till = " + element.getEndIndex() + " The key =" +
      // element.getContent());

      stBuffer.replace(element.getStartIndex(), element.getEndIndex(), styledMatch);
      foundObjects.pop();

    }
    // System.out.println(" FinalCOntext=" + stBuffer.toString());

    return stBuffer.toString();
  }

  /**
   * Escapes the HTML by replacing the signs with their codename
   * 
   * @param s
   * @return
   */
  public static String escapeHTML(String s) {
    final StringBuilder out = new StringBuilder(Math.max(16, s.length()));
    for (int i = 0; i < s.length(); i++) {
      final char c = s.charAt(i);
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

/**
 * Class use only for the sake of storing the start,endindex and the data fround
 * 
 * @author bluecc
 *
 */
class ObjectFound {
  private int startIndex = 0;
  private int endIndex = 0;
  private String content = "";

  /**
   * Constructor
   * 
   * @param startIndex
   * @param endIndex
   * @param content
   */
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