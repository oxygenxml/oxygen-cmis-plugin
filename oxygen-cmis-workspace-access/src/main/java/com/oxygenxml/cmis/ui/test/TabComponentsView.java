package com.oxygenxml.cmis.ui.test;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.CmisAccessSingleton;
import com.oxygenxml.cmis.ui.TabsPresenter;

/**
 * Component used for show the documents in tabs
 * 
 * @author bluecc
 *
 */
public class TabComponentsView extends JPanel implements TabsPresenter {
  private static final Logger logger = Logger.getLogger(TabComponentsView.class);
  protected static int itemsCounter = 0;
  private final JTabbedPane pane = new JTabbedPane();

  /*
   * Initialize the components
   */
  public TabComponentsView() {

    // Set layout
    setLayout(new GridBagLayout());

    // constraints
    add(pane);
    // Set one only a column
    setLayout(new GridLayout(0, 1));
    setMinimumSize(new Dimension(200, 100));
    setVisible(true);
  }

  /**
   * Initialize each component using ButtonTabComponent class
   * 
   * @param i
   */
  private void initTabComponent(int i) {

    pane.setTabComponentAt(i, new ButtonTabComponentView(pane));
  }

  /**
   * @exception UnsupportedEncodingException
   * @exception IOException
   * @exception IOException
   * 
   * @param doc
   */
  @Override
  public void presentItem(Document doc) {

    // The title
    String title = doc.getName();

    // The textarea
    JTextArea area = new JTextArea();

    /*
     * A reader to reader 1024 characters using a ResourceController and
     * document ID
     */
    Reader documentContent = null;
    try {
      documentContent = CmisAccessSingleton.getInstance().createResourceController().getDocumentContent(doc.getId());
      char[] ch = new char[1024];
      int l = -1;

      // While there is text to read
      while ((l = documentContent.read(ch)) != -1) {

        // Append the text to the textArea
        area.append(String.valueOf(ch, 0, l));
      }
    } catch (Exception e) {
      // Show the exception if there is one
      logger.debug("Exception ", e);

    } finally {
      // No matter what happens close at the end the doc

      // Even if it's not empty
      if (documentContent != null) {

        try {
          documentContent.close();

        } catch (IOException e) {

          // Show the exception if there is one
          JOptionPane.showMessageDialog(null, "Exception " + e.getMessage());
        }
      }
    }

    // Add a scroll pane for the textArea
    JScrollPane scrollerTabs = new JScrollPane(area);

    pane.add(title, scrollerTabs);

    // Initialize the button for the tab
    initTabComponent(itemsCounter);
    itemsCounter++;

    pane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);

  }

}