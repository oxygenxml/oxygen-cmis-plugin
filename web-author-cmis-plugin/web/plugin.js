(function() {
  sync.util.loadCSSFile("../plugin-resources/cmis/style.css");

  var PROTOCOL_PREFIX = 'cmis://';

var rootUrl;
var urlQuery = sync.util.getURLParameter('url');
var urlFromOptions = sync.options.PluginsOptions.getClientOption("cmis.enforced_url");

if (urlQuery) {
  var initialUrl = decodeURIComponent(urlQuery);
  var limit = initialUrl.substring(PROTOCOL_PREFIX.length).indexOf('/') + PROTOCOL_PREFIX.length;
  rootUrl = initialUrl.substring(0, limit);
} else if (urlFromOptions) {
  rootUrl = PROTOCOL_PREFIX + encodeURIComponent(urlFromOptions);
  if (urlFromOptions.lastIndexOf('/') !== urlFromOptions.length) {
    rootUrl += '/';
  }
}

  var cmisFileServer = function() {};

  /**
   * Login the user and call this callback at the end.
   *
   * @param {function} authenticated The callback when the user was authenticated - successfully or not.
   */
  cmisFileServer.prototype.login = function(serverUrl, authenticated) {
    var loginDialog = this.getLoginDialog();

    loginDialog.onSelect(function(key) {
      if (key === 'ok') {
        // Send the user and password to the login servlet which runs in the webapp.
        var userField = document.getElementById('cmis-name');
        var user = userField.value.trim();
        var passwdField = document.getElementById('cmis-passwd');
        var passwd = passwdField.value;

        userField.value = '';
        passwdField.value = '';

        goog.net.XhrIo.send(
          '../plugins-dispatcher/cmis-login',
          function() {
            localStorage.setItem('cmis.user', user);

            authenticated && authenticated();
          },
          'POST',
          // form params
          goog.Uri.QueryData.createFromMap(new goog.structs.Map({
            user: user,
            passwd: passwd
          })).toString()
        );
      }
    });

    loginDialog.show();

    // autocomplete the last username.
    var userInput = loginDialog.getElement().querySelector('#cmis-name');
    userInput.value = this.getUserName() || '';
    userInput.select();
  };

  /**
   * Create the CMIS login dialog.
   */
  cmisFileServer.prototype.getLoginDialog = function() {
    if (! this.loginDialog_) {
      this.loginDialog_ = workspace.createDialog();
      var cD = goog.dom.createDom;

      var cmisNameInput = cD('input', {
        id: 'cmis-name',
        type: 'text'
      });
      cmisNameInput.setAttribute('autocorrect', 'off');
      cmisNameInput.setAttribute('autocapitalize', 'none');
      cmisNameInput.setAttribute('autofocus', '');
      cmisNameInput.setAttribute('autocomplete', 'username');

      var cmisPasswordInput = cD('input', {
        id: 'cmis-passwd',
        type: 'password'
      });
      cmisPasswordInput.setAttribute('autocomplete', 'current-password');

      goog.dom.appendChild(this.loginDialog_.getElement(),
        cD('form', 'cmis-login-dialog',
          cD('label', '',
            tr(msgs.NAME_) + ': ',
            cmisNameInput
          ),
          cD('label', {style: 'margin-top: 10px;'},
            tr(msgs.PASSWORD_) + ': ',
            cmisPasswordInput
          )
        )
      );
      this.loginDialog_.setTitle(tr(msgs.AUTHENTICATION_REQUIRED_));
      this.loginDialog_.setPreferredSize(300, null);
    }

    return this.loginDialog_;
  };

  cmisFileServer.prototype.getUserName = function() {
    return localStorage.getItem('cmis.user');
  };

  cmisFileServer.prototype.getDefaultRootUrl = function() {
    return rootUrl;
  };

  cmisFileServer.prototype.logout = function(logoutCallback) {
    goog.net.XhrIo.send(
      '../plugins-dispatcher/cmis-login?action=logout',
      goog.bind(function() {
        try {
          localStorage.removeItem('cmis.user');
        } catch (e) {
          console.warn(e);
        }
        logoutCallback();
      }, this),
      'POST');
  };

  cmisFileServer.prototype.createRootUrlComponent = function(rootUrlParam, rootURLChangedCallback, readOnly) {
    var div = document.createElement('div');

    if (rootUrl) {
      if (! rootUrlParam) {
        if (! this.rootUrlSet) {
          this.rootUrlSet = true;
          setTimeout(function() {
            rootURLChangedCallback(rootUrl, rootUrl); // TODO: bug in web author.
          }, 0);
        }
      }
      div.textContent = sync.options.PluginsOptions.getClientOption("cmis.enforced_name");
    } else {
      div.textContent = tr(msgs.SET_CMIS_API_URL_); // TODO link to documentation.
    }
    return div;
  };

  cmisFileServer.prototype.getUrlInfo = function(url, urlInfoCallback, showErrorMessageCallback) {
    urlInfoCallback(rootUrl, url);
  };

// -------- Initialize the file browser information ------------
  var cmisFileRepositoryDescriptor = {
    'id': 'cmis',
    'name': sync.options.PluginsOptions.getClientOption('cmis.enforced_name'),
    'icon': sync.util.computeHdpiIcon(sync.options.PluginsOptions.getClientOption('cmis.enforced_icon')),
    'matches': function matches(url) {
      return url.match(PROTOCOL_PREFIX); // Check if the provided URL points to version file or folder from Cmis file repository
    },
    'fileServer': new cmisFileServer()
  };

  workspace.getFileServersManager().registerFileServerConnector(cmisFileRepositoryDescriptor);

  goog.events.listen(workspace, sync.api.Workspace.EventType.EDITOR_LOADED, function(e) {
    var editor = e.editor;
    if (editor.getUrl().indexOf(PROTOCOL_PREFIX) === 0) {
      var root = document.querySelector('[data-root="true"]');
      var nonversionable = root.getAttribute('data-pseudoclass-nonversionable');
      var checkedout = root.getAttribute('data-pseudoclass-checkedout') === 'true';
      var oldversion = root.getAttribute('data-pseudoclass-oldversion');
      var locked = root.getAttribute('data-pseudoclass-locked') === 'true';

      var status = new CmisStatus(checkedout, locked);

      // Register the newly created action.
      if (nonversionable !== 'true') {
        addCustomActions(editor, status);
      }
    }
  });

  /**
   * Add custom actions to the builin toolbar.
   *
   * @param editor the editor.
   * @param {CmisStatus} status the document status.
   */
  function addCustomActions(editor, status) {
    var actionsManager = editor.getActionsManager();
    actionsManager.registerAction('cmisCheckOut.link', new CmisCheckOutAction(editor, status));
    actionsManager.registerAction('cancelCmisCheckOut.link', new cancelCmisCheckOutAction(editor, status));
    actionsManager.registerAction('cmisCheckIn.link', new CmisCheckInAction(editor, status));
    actionsManager.registerAction('listOldVersion.link', new listOldVersionsAction(editor));

    goog.events.listen(editor, sync.api.Editor.EventTypes.ACTIONS_LOADED, function(e) {
      var toolbars = e.actionsConfiguration.toolbars;

      if (!toolbars) {
        return;
      }
      for (var i = 0; i < toolbars.length; i ++) {
        var toolbar = toolbars[i];
        if (toolbar.name === "Builtin") {
          toolbar.children.push({
            displayName: cmisFileRepositoryDescriptor.name,
            type: 'list',
            name: 'cmis-actions',
            children: [{
              id: 'listOldVersion.link',
              type: 'action'
            }, {
              id: 'cmisCheckOut.link',
              type: 'action'
            }, {
              id: 'cmisCheckIn.link',
              type: 'action'
            }, {
              id: 'cancelCmisCheckOut.link',
              type: 'action'
            }]
          });
        }
      }
    });
  }
})();