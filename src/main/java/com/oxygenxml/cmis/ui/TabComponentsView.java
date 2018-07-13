 package com.oxygenxml.cmis.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;

import com.oxygenxml.cmis.core.CMISAccess;

public class TabComponentsView extends JPanel implements TabsPresenter {
  private int itemsCounter = 0;
  private final JTabbedPane pane = new JTabbedPane();

  /*
   * Initialize the components
   */
  public TabComponentsView() {
    add(pane);
    setLayout(new GridLayout(0, 1));
    setMinimumSize(new Dimension(200, 100));
    setVisible(true);
  }


  /*
   * Initialize each component using ButtonTabComponent class
   */
  private void initTabComponent(Document doc, int i) {
    
    pane.setTabComponentAt(i, new ButtonTabComponentView(pane));
  }

  @Override
  public void presentItem(Document doc) {
    // TODO Auto-generated method stub
    String title = doc.getName();
    
    JTextArea area = new JTextArea();
    Reader documentContent = null;
    try {
       documentContent = CMISAccess.getInstance().createResourceController().getDocumentContent(doc.getId());
      char[] ch = new char[1024];
      int l = -1;
      
      while ((l = documentContent.read(ch)) != -1) {
        area.append(String.valueOf(ch, 0, l));
      }
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      if (documentContent != null) {
        try {
          documentContent.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    
    JScrollPane sp = new JScrollPane(area);
    
    pane.add(title, sp);
    initTabComponent(doc, itemsCounter);
    itemsCounter++;

    pane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
    
  }



}