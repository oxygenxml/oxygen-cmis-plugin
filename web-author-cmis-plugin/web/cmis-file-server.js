
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
 * @param {function} onLoginSuccessful The callback when the user successfully logs in.
 */
CmisFileServer.prototype.login = function(serverUrl, onLoginSuccessful) {
  let loginDialog = this.getLoginDialog_();
  let userField = loginDialog.getElement().querySelector('#cmis-name');
  let passwdField = loginDialog.getElement().querySelector('#cmis-passwd');

  let urlParams = new URLSearchParams(window.location.search)
  let alfrescoTicket = urlParams.get("alfrescoTicket");
  if (alfrescoTicket) {
    this.isLoginInProgress_ = true;
    this.rootUrlComponent_?.classList.add("oxy-spinner");
    this.login_("ROLE_TICKET", alfrescoTicket)
      .then(() => {
        onLoginSuccessful && onLoginSuccessful();
      })
      .catch(e => {
        loginDialog.show();
      })
      .finally(() => {
        this.isLoginInProgress_ = false;
        this.rootUrlComponent_?.classList.remove("oxy-spinner");
      });
  } else {
    loginDialog.onSelect(function(key, event) {
      if (key === 'ok') {
        event.preventDefault();
        if (this.isLoginInProgress_) {
          return;
        }

        this.isLoginInProgress_ = true;
        userField.setAttribute("disabled", true);
        passwdField.setAttribute("disabled", true);
        loginDialog.getElement().classList.add("oxy-spinner");
        this.login_(userField.value.trim(), passwdField.value)
          .then(() => {
            passwdField.value = '';
            loginDialog.hide();
            onLoginSuccessful && onLoginSuccessful();
          })
          .finally(() => {
            this.isLoginInProgress_ = false;
            loginDialog.getElement().classList.remove("oxy-spinner");
            userField.removeAttribute("disabled");
            passwdField.removeAttribute("disabled");
          });
      }
    }.bind(this));

    loginDialog.show();
    userField.select();
  }
};

CmisFileServer.prototype.login_ = function(user, password) {
  return fetch('../plugins-dispatcher/cmis-login', {
    body: "user=" + encodeURIComponent(user)
      + "&passwd=" + encodeURIComponent(password)
      + "&serverUrl=" + encodeURIComponent(sync.options.PluginsOptions.getClientOption("cmis.enforced_url")),
    method: "POST",
    headers: {'X-Requested-With': 'g', 'Content-Type': 'application/x-www-form-urlencoded'}
  })
  .then(response => response.json())
  .then(responseJson => {
    localStorage.setItem('cmis.user', responseJson.userName);
  });
}

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

    // autocomplete the last username.
    cmisNameInput.value = this.getUserName() || '';

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
    loginDialog.setTitle(tr(msgs.CONNECT_));
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
    'POST',
    '',
    {'X-Requested-With': 'g'});
};

CmisFileServer.prototype.createRootUrlComponent = function(rootUrlParam, rootURLChangedCallback, readOnly) {
  var div = document.createElement('div');

  if (this.rootUrl_) {
    if (! rootUrlParam) {
      if (! this.rootUrlSet_) {
        this.rootUrlSet_ = true;
        setTimeout(goog.bind(function() {
          rootURLChangedCallback(this.rootUrl_, this.rootUrl_); // TODO: bug in web author.
        }, this), 0);
      }
    }
    div.textContent = sync.options.PluginsOptions.getClientOption("cmis.enforced_name");
  } else {
    div.textContent = tr(msgs.SET_CMIS_API_URL_); // TODO link to documentation.
  }

  this.rootUrlComponent_ = div;

  return div;
};

CmisFileServer.prototype.getUrlInfo = function(url, urlInfoCallback, showErrorMessageCallback) {
  urlInfoCallback(this.rootUrl_, url);
};
