package com.oxygenxml.cmis.storage;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.ui.LoginDialog;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Singleton
 * 
 * SERVERS CREDENTIALS
 * 
 * 
 * 
 * @author bluecc
 *
 */
public class SessionStorage {

  private static final String OPTION_TAG = "cmisPlugin";

  private static SessionStorage instance;

  private PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();

  public static SessionStorage getInstance() {
    if (instance == null) {
      instance = new SessionStorage();
    }
    return instance;
  }

  private SessionStorage() {
    // Create the loginDialog
    new LoginDialog((JFrame) pluginWorkspace.getParentFrame());

    // Get the options stored
    String option = pluginWorkspace.getOptionsStorage().getOption(OPTION_TAG, null);
    try {
      if (option != null) {
        option = pluginWorkspace.getXMLUtilAccess().unescapeAttributeValue(option);
      }
      options = unmarshal(option);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    if (options == null) {
      options = new Options();
    }
  }

  public LinkedHashSet<String> getSevers() {

    // // Create the default servers
    // String[] serversList = new String[] {
    // "http://127.0.0.1:8098/alfresco/api/-default-/cmis/versions/1.1/atom",
    // "http://localhost:8088/alfresco/api/-default-/cmis/versions/1.1/atom" };

    return options.getServers();
  }

  public void setServers(LinkedHashSet<String> serversList) {
    options.setServers(serversList);
    SessionStorage.getInstance().store();
  }

  public void addUserCredentials(URL serverURL, UserCredentials uc) {
    options.addUserCredentials(serverURL.toExternalForm(), uc);
    SessionStorage.getInstance().store();
  }

  public UserCredentials getUserCredentials(URL serverURL) {
    Map<String, UserCredentials> credentials = options.getCredentials();
    if (credentials != null) {
      return credentials.get(serverURL.toExternalForm());
    }
    
    return null;
  }

  private Options options;

  private static String marshal(Options options) throws Exception {
    
    JAXBContext context = JAXBContext.newInstance(Options.class);
    Marshaller m = context.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

    // Write to userCredentials
    StringWriter strWriter = new StringWriter();
    m.marshal(options, strWriter);

    return strWriter.toString();
  }

  private static Options unmarshal(String content) throws Exception {
    
    JAXBContext context = JAXBContext.newInstance(Options.class);
    Unmarshaller m = context.createUnmarshaller();

    // Write to userCredentials
    return (Options) m.unmarshal(new StringReader(content));
  }

  public void store() {
    try {
      String marshal = marshal(options);

      marshal = pluginWorkspace.getXMLUtilAccess().escapeAttributeValue(marshal);

      pluginWorkspace.getOptionsStorage().setOption(OPTION_TAG, marshal);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

//  public static void main(String[] args) throws Exception {
//    Options op = new Options();
//    op.setServers(Arrays.asList(new String[] { "server1", "server2" }));
//
//    op.addUserCredentials("server1", new UserCredentials("user1", "pass1"));
//    op.addUserCredentials("server2", new UserCredentials("user2", "pass2"));
//
//    String s = marshal(op);
//
//    System.out.println(s);
//
//    Options options2 = unmarshal(s);
//
//    System.out.println(options2.getServers());
//    System.out.println(options2.getCredentials());
//  }
}
