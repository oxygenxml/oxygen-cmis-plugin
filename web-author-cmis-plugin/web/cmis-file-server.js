
var CmisFileServer = function() {
  var rootUrl;
  var urlQuery = sync.util.getURLParameter('url');
  var urlFromOptions = sync.options.PluginsOptions.getClientOption("cmis.enforced_url");

  if (urlQuery) {
    var initialUrl = decodeURIComponent(urlQuery);
    var limit = initialUrl.substring(CmisFileServer.PROTOCOL_PREFIX.length).indexOf('/') + CmisFileServer.PROTOCOL_PREFIX.length;
    rootUrl = initialUrl.substring(0, limit);
  } else if (urlFromOptions) {
    rootUrl = CmisFileServer.PROTOCOL_PREFIX + encodeURIComponent(urlFromOptions);
    if (urlFromOptions.lastIndexOf('/') !== urlFromOptions.length) {
      rootUrl += '/';
    }
  }
  this.rootUrl_ = rootUrl;
  this.rootUrlSet_ = false;

  this.loginDialog_ = null;
};

/**
 * The CMIS protocol.
 *
 * @type {string}
 */
CmisFileServer.PROTOCOL_PREFIX = 'cmis://';

/**
 * Login the user and call this callback at the end.
 *
 * @param {string} serverUrl The server URL.
 * @param {function} authenticated The callback when the user was authenticated - successfully or not.
 */
CmisFileServer.prototype.login = function(serverUrl, authenticated) {
  var loginDialog = this.getLoginDialog_();

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
 * @private
 */
CmisFileServer.prototype.getLoginDialog_ = function() {
  if (! this.loginDialog_) {
    var loginDialog = workspace.createDialog();
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

    goog.dom.appendChild(loginDialog.getElement(),
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
    loginDialog.setTitle(tr(msgs.AUTHENTICATION_REQUIRED_));
    loginDialog.setPreferredSize(300, null);
    this.loginDialog_ = loginDialog;
  }

  return this.loginDialog_;
};

CmisFileServer.prototype.getUserName = function() {
  return localStorage.getItem('cmis.user');
};

CmisFileServer.prototype.getDefaultRootUrl = function() {
  return this.rootUrl_;
};

CmisFileServer.prototype.logout = function(logoutCallback) {
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

CmisFileServer.prototype.createRootUrlComponent = function(rootUrlParam, rootURLChangedCallback, readOnly) {
  var div = document.createElement('div');

  if (this.rootUrl_) {
    if (! rootUrlParam) {
      if (! this.rootUrlSet_) {
        this.rootUrlSet_ = true;
        setTimeout(function() {
          rootURLChangedCallback(this.rootUrl_, this.rootUrl_); // TODO: bug in web author.
        }, 0);
      }
    }
    div.textContent = sync.options.PluginsOptions.getClientOption("cmis.enforced_name");
  } else {
    div.textContent = tr(msgs.SET_CMIS_API_URL_); // TODO link to documentation.
  }
  return div;
};

CmisFileServer.prototype.getUrlInfo = function(url, urlInfoCallback, showErrorMessageCallback) {
  urlInfoCallback(this.rootUrl_, url);
};
