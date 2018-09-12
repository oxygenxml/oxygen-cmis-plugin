package com.oxygenxml.cmis.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.oxygenxml.cmis.core.UserCredentials;

import ro.sync.exml.workspace.api.standalone.ui.OKCancelDialog;

/*
 * The login dialog that extends the Oxygen OKCancelDialog
 * and adds the content for the login form
 * 
 * Sends the data when OK button (Inheritance from OKCancelDialog)
 * is pressed and cancels the when when cancel is pressed 
 */
public class LoginDialog extends OKCancelDialog {

  // Initialize the fields
  private JTextField userField;
  private JPasswordField passwordField;

  // Constructor
  public LoginDialog(JFrame frame) {

    super(frame, "Login", true);

    // Get the parent container
    Container cont = getContentPane();

    // Initialize fields
    userField = new JTextField();
    passwordField = new JPasswordField();

    // Set Layout
    cont.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // Username JLabel constraints
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.0;
    c.ipadx = 10;
    c.insets = new Insets(3, 5, 3, 5);
    c.fill = GridBagConstraints.NONE;
    JLabel userLabel = new JLabel("Username:");
    cont.add(userLabel, c);

    // Username JField constraints
    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 1.0;
    c.gridwidth = 1;
    c.insets = new Insets(3, 5, 3, 5);
    c.fill = GridBagConstraints.HORIZONTAL;
    cont.add(userField, c);

    // Password JLabel constraints
    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0.0;
    c.ipadx = 10;
    c.gridwidth = 1;
    c.insets = new Insets(3, 5, 3, 5);
    JLabel passwordLabel = new JLabel("Password:");
    cont.add(passwordLabel, c);

    // Password JField constraints
    c.gridx = 1;
    c.gridy = 1;
    c.weightx = 1.0;
    c.gridwidth = 1;
    c.insets = new Insets(3, 5, 3, 5);
    c.fill = GridBagConstraints.HORIZONTAL;
    cont.add(passwordField, c);

    // Show it in the center of the frame
    setLocationRelativeTo(frame);

    // This solves the problem where the dialog was not getting
    // focus the second time it was displayed
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        userField.requestFocusInWindow();
      }
    });

    pack();
    setModal(true);
    setVisible(true);
    setResizable(true);
  }

  /**
   * Get the use credentials and set the info from the inputs
   * 
   * @return UserCredentials
   */
  public UserCredentials getUserCredentials() {

    // Create the user and set it's credentials
    UserCredentials user = new UserCredentials();

    user.setUsername(this.userField.getText());
    user.setPassword(String.valueOf(this.passwordField.getPassword()));

    return user;

  }
}
