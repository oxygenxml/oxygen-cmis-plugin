package com.oxygenxml.cmis.storage;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
 * SERVERS CREDENTIALS Handles the serialization with marshal and unmarshal
 * 
 * @author bluecc
 *
 */
public class SessionStorage {

  // Tag of the option
  private static final String OPTION_TAG = "cmisPlugin";

  // Singleton instance
  private static SessionStorage instance;

  // Get the plugin workspace
  private PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();

  // Get the insgleton instance
  public static SessionStorage getInstance() {
    if (instance == null) {
      instance = new SessionStorage();
    }
    return instance;
  }

  /*
   * 
   */
  private SessionStorage() {

    // Get the options stored
    String option = pluginWorkspace.getOptionsStorage().getOption(OPTION_TAG, null);

    // If there is no data entered in LoginDialog check the storage
    try {
      if (option != null) {
        option = pluginWorkspace.getXMLUtilAccess().unescapeAttributeValue(option);
      }
      // Unwrap the storage
      options = unmarshal(option);

    } catch (Exception e1) {
      JOptionPane.showMessageDialog(null, "Exception " + e1.getMessage());
    }

    // Initialize new options
    if (options == null) {
      options = new Options();
    }
  }

  public LinkedHashSet<String> getSevers() {
    return options.getServers();
  }

  public void setServers(LinkedHashSet<String> serversList) {
    options.setServers(serversList);
    SessionStorage.getInstance().store();
  }

  /*
   * Send the new credentials and store them
   * 
   * @param serverURL the url
   * 
   * @param uc usercredentials
   */
  public void addUserCredentials(URL serverURL, UserCredentials uc) {
    options.addUserCredentials(serverURL.toExternalForm(), uc);
    SessionStorage.getInstance().store();
  }

  /*
   * Get the credentials only if they exist
   */
  public UserCredentials getUserCredentials(URL serverURL) {
    Map<String, UserCredentials> credentials = options.getCredentials();
    if (credentials != null) {
      return credentials.get(serverURL.toExternalForm());
    }

    return null;
  }

  private Options options;

  /*
   * Serialize the options with JAXB
   * 
   * @param options
   */
  private static String marshal(Options options) throws Exception {

    // Create the instance using the model class Options
    JAXBContext context = JAXBContext.newInstance(Options.class);
    Marshaller m = context.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

    // Write to userCredentials
    StringWriter strWriter = new StringWriter();
    m.marshal(options, strWriter);

    return strWriter.toString();
  }

  /*
   * Deserialize using the context from the storage
   * 
   * @return Options
   */
  private static Options unmarshal(String content) throws Exception {

    JAXBContext context = JAXBContext.newInstance(Options.class);
    Unmarshaller m = context.createUnmarshaller();

    // Write to userCredentials
    return (Options) m.unmarshal(new StringReader(content));
  }

  /*
   * Store the data after serialization in the storage
   */
  public void store() {
    try {
      String marshal = marshal(options);

      marshal = pluginWorkspace.getXMLUtilAccess().escapeAttributeValue(marshal);

      pluginWorkspace.getOptionsStorage().setOption(OPTION_TAG, marshal);
    } catch (Exception e1) {
      JOptionPane.showMessageDialog(null, "Exception " + e1.getMessage());
    }

  }

}
