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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.plugin.TranslationResourceController;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class SearchResultCellRenderer extends JPanel implements ListCellRenderer<IResource> {

  private  final String createdByLabel;
  private  final String noDataLabel;
  // Internal role
  private static final String HTML_ENCLOSING_TAG = "</code></html>";
  private static final String HTML_TAG = "<html><code style=' overflow-wrap: break-word; word-wrap: break-word; margin: 5px; padding: 5px; text-align: center;vertical-align: middle;'>";
  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(ItemListView.class);
  private final JPanel iconPanel;
  private final JLabel iconLabel;

  private final JPanel descriptionPanel;
  private final JLabel nameResource;
  private final JLabel propertiesResource;
  private final JLabel lineResource;
  private final JPanel lineResourcePanel;

  private final JPanel notifierPanel;
  private final JLabel notification;

  private final transient ContentSearcher contentProv;

  // Graphics configurations
  private final String matchPattern;

  public SearchResultCellRenderer(ContentSearcher contentProvider, String matchPattern) {
    createdByLabel = TranslationResourceController.getMessage("CREATED_BY");
    noDataLabel = TranslationResourceController.getMessage("NO_DATA");
    
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
    final ResourceController ctrl = CMISAccess.getInstance().createResourceController();

    String pathValue = null;
    String notifyValue = null;
    String propertiesValues = null;
    String resourceText = null;
    setComponentOrientation(list.getComponentOrientation());

    resourceText = contentProv.getName(value);

    if (resourceText != null) {
      resourceText = styleString(resourceText);
    }

    if (value instanceof DocumentImpl) {

      final DocumentImpl doc = ((DocumentImpl) value);
      if (doc.getId() != null) {

        renderDocIcon(value, doc);

        // Get the path
        pathValue = contentProv.getPath(doc, ctrl);
        // Get the properties
        propertiesValues = contentProv.getProperties(doc);
        // Get notification
        notifyValue = createdByLabel + doc.getCreatedBy();

        logger.debug("Line=" + contentProv.getLineDoc(doc, matchPattern));

        // Get the results from the server
        String resultContext = contentProv.getLineDoc(doc, matchPattern);

        resultContext = styleString(resultContext);

        lineResource.setText(HTML_TAG + (resultContext != null ? resultContext : noDataLabel) + HTML_ENCLOSING_TAG);

      }
    } else if (value instanceof FolderImpl) {

      final FolderImpl folder = ((FolderImpl) value);

      if (folder.getId() != null) {
        iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/folder.png")));

        pathValue = contentProv.getPath(folder, ctrl);
        propertiesValues = contentProv.getProperties(folder);
        notifyValue = createdByLabel + folder.getCreatedBy();

        lineResource.setText(HTML_TAG + noDataLabel + HTML_ENCLOSING_TAG);

      }
    }
    // Render name
    nameResource.setText("<html><div style=' overflow-wrap: break-word; word-wrap: break-word;'>"
        + (resourceText != null ? resourceText : noDataLabel) + "</div></html>");

    // Render properties
    propertiesResource.setOpaque(true);
    propertiesResource.setForeground(Color.GRAY);
    propertiesResource.setText((propertiesValues != null ? propertiesValues : noDataLabel));

    // Render notification
    notification.setText(notifyValue);

    setBackgroundComponent(list, index, isSelected, pathValue);

    return this;
  }

  /**
   * Set a background when a component is selected.
   * 
   * @param list
   * @param index
   * @param isSelected
   * @param pathValue
   */
  private void setBackgroundComponent(JList<? extends IResource> list, int index, boolean isSelected,
      String pathValue) {
    final JList.DropLocation dropLocation = list.getDropLocation();
    Color bg = null;
    Color fg = null;
    if (dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == index) {

      bg = javax.swing.UIManager.getColor("List.dropCellBackground");
      fg = javax.swing.UIManager.getColor("List.dropCellForeground");

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
  }

  private void renderDocIcon(IResource value, final DocumentImpl doc) {
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
  }

  /**
   * Initializes the escpaing of the HTML and
   * 
   * @param resultContext
   * @return
   */
  private String styleString(String resultContext) {
    // Check if there is some data
    if (resultContext != null) {

      // Escape the HTML
      resultContext = escapeHTML(resultContext);

      logger.debug("Before split = " + resultContext);
      // Check if there is something in searchbar
      if (matchPattern != null) {

        // Get the styled HTML splitted
        resultContext = getReadyHTMLSplit(resultContext, matchPattern);
      }

      logger.debug("After split = " + resultContext);
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
  static String getReadyHTMLSplit(String context, String matchPattern) {
    // Split the words entered as keys
    final String[] searchKeys = matchPattern.trim().split("\\s+");
    final String contextToSplit = context;
    final StringBuilder strBuilder = new StringBuilder(contextToSplit);
    final StringBuilder regexBuilder = new StringBuilder();
    String styledMatch = "";

    // Concatenate all the keys from the search
    for (final String string : searchKeys) {
      regexBuilder.append(string + "|");
    }

    // Use a stack to store data because we will show them from the back in
    // order to not destroy the original string
    final Deque<ObjectFound> foundObjects = new ArrayDeque<>();

    // Matters to preserve the order of the keys
    final Pattern pattern = Pattern.compile(regexBuilder.toString());
    final Matcher matcher = pattern.matcher(strBuilder.toString());

    // While some results are found
    while (matcher.find()) {
      final String found = matcher.group();

      // There is data
      if (!found.equals("")) {
        final int startIndex = matcher.start();
        final int endIndex = matcher.end();

        // Create a new object
        foundObjects.push(new ObjectFound(startIndex, endIndex, found.trim()));

        logger.debug("Start index: " + startIndex);
        logger.debug(" End index: " + endIndex);
        logger.debug(" Found: " + found.trim());
      }
    }

    // Iterate all the objects from the stack
    while (!foundObjects.isEmpty()) {

      final ObjectFound element = foundObjects.peek();
      styledMatch = "<nobr style=' background-color:yellow; color:gray'>" + element.getContent() + "</nobr>";

      strBuilder.replace(element.getStartIndex(), element.getEndIndex(), styledMatch);
      foundObjects.pop();

    }
    logger.debug(" FinalContext=" + strBuilder.toString());

    return strBuilder.toString();
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
 * Class use only for the sake of storing the start, end index and the data
 * found.
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