package com.oxygenxml.cmis.storage;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.ui.AuthenticatorUtil;
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
 * @see com.oxygenxnl.cmis.storage.Options
 */
public class SessionStorage {
  private Options options;

  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(SessionStorage.class);

  // Tag of the option
  private static final String OPTION_TAG = "cmisPlugin";

  // Singleton instance
  private static SessionStorage instance;

  // Get the plugin workspace
  private PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();

  // Get the singleton instance
  public static SessionStorage getInstance() {
    if (instance == null) {
      instance = new SessionStorage();
    }
    return instance;
  }

  /**
   * Constructor that tries to unwrap the storage
   */
  private SessionStorage() {

    // Get the options stored
    String option = pluginWorkspace.getOptionsStorage().getOption(OPTION_TAG, null);

    // If there is no data entered in LoginDialog check the storage
    try {

      // Escape the xml tag
      if (option != null) {
        option = pluginWorkspace.getXMLUtilAccess().unescapeAttributeValue(option);

        // Unwrap the storage
        options = unmarshal(option);
      }

    } catch (Exception e1) {

      logger.error(e1, e1);

      JOptionPane.showMessageDialog(null, "Exception " + e1.getMessage());
    }

    // Initialize new options
    if (options == null) {

      options = new Options();
    }
  }

  /**
   * Get the servers
   * 
   * @return Set<String>
   */
  public Set<String> getSevers() {
    return options.getServers();
  }

  /**
   * Send the new credentials and store them
   * 
   * @param serverURL
   * @param uc
   * 
   * @see com.oxygenxnl.cmis.core.UserCredentials
   */
  public void addUserCredentials(URL serverURL, UserCredentials uc) {
    // Add the new credentials
    options.addUserCredentials(serverURL.toExternalForm(), uc);

    // Store the new credentials in the storage
    SessionStorage.getInstance().store();
  }

  /**
   * Get the user credentials for the required serverURL
   * 
   * @param serverURL
   * @return
   */
  public UserCredentials getUserCredentials(URL serverURL) {
    return options.getUserCredentials(serverURL);
  }

  /**
   * Serialize the options with JAXB
   * 
   * @param options
   * @exception Exception
   * 
   * @see com.oxygenxnl.cmis.storage.Options
   */
  private static String marshal(Options options) throws Exception {

    // Create the instance using the model class Options
    JAXBContext context = JAXBContext.newInstance(Options.class);

    // Create the serializer
    Marshaller m = context.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

    // Write to userCredentials
    StringWriter strWriter = new StringWriter();

    // Commit serialization
    m.marshal(options, strWriter);

    return strWriter.toString();
  }

  /**
   * Deserialize using the context from the storage
   * 
   * @exception Exception
   * @return Options
   */
  private static Options unmarshal(String content) throws Exception {
    // Create the deserializer object
    JAXBContext context = JAXBContext.newInstance(Options.class);

    // Call the deserializer
    Unmarshaller m = context.createUnmarshaller();

    // Write to userCredentials
    System.err.println("content |" + content + "|");
    return (Options) m.unmarshal(new StringReader(content));
  }

  /**
   * Store the data after serialization in the storage
   * 
   * @exception Exception
   * 
   * 
   */
  public void store() {

    try {
      // Serialize the options
      String marshal = marshal(options);

      // Escape the XML tags
      marshal = pluginWorkspace.getXMLUtilAccess().escapeAttributeValue(marshal);

      // Save in the memory of the workspace the option
      pluginWorkspace.getOptionsStorage().setOption(OPTION_TAG, marshal);
    } catch (Exception e1) {

      logger.error(e1, e1);
      // Show the exception if there is one
      JOptionPane.showMessageDialog(null, "Exception " + e1.getMessage());
    }

  }

  /**
   * Add the server to the options
   * 
   * @param currentServerURL
   * 
   * @see com.oxygenxnl.cmis.storage.Options
   */
  public void addServer(String currentServerURL) {

    // Add server URL to the options
    options.addServer(currentServerURL);

    // Store the new options in the memory
    SessionStorage.getInstance().store();
  }
}
