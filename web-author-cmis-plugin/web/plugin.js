var cmisTimeOut = 5000;


(function () {
  var initialUrl = decodeURIComponent(sync.util.getURLParameter('url'));
  var prefix = 'cmis://';

  var limit = initialUrl.substring(prefix.length).indexOf('/') + prefix.length;
  var rootUrl = initialUrl.substring(0, limit);
  
  // goog.events.listen(workspace, sync.api.Workspace.EventType.BEFORE_EDITOR_LOADED, function(e) {
  //   console.log('Plugin loaded successfully');
  
  //   var url = e.options.url;
  //   // If the URL has 'rest' protocol we use the rest protocol handler.
  //   if (url.match('cmis')) {
      
    

  //     // initialUrl = initialUrl.replace('/' + repoId);

  //     // console.log('RepoID ' + repoId);

  //     console.log('Initial URL ' + initialUrl);
  //     console.log('Root URL ' + rootUrl);
  //     // set the workspace UrlChooser
  //     workspace.setUrlChooser(new sync.api.FileBrowsingDialog({
  //       initialUrl: initialUrl,
  //       rootUrl: rootUrl
  //     }));
  //   }
  // });
  
  var urlFromOptions = sync.options.PluginsOptions.getClientOption("cmis.enforced_url");
  var rootUrl = null;
  if (urlFromOptions) {
    rootUrl = 'cmis://' + encodeURIComponent(urlFromOptions);
    if (urlFromOptions.lastIndexOf('/') !== urlFromOptions.length) {
      rootUrl += '/';
    } 
  }
   // 'cmis://http%3A%2F%2F127.0.0.1%3A8098%2Falfresco%2Fapi%2F-default-%2Fpublic%2Fcmis%2Fversions%2F1.1%2Fatom/'; 
  var cmisFileRepo = {};
  
  /**
   * Login the user and call this callback at the end.
   *
   * @param {function} authenticated The callback when the user was authenticated - successfully or not.
   */
  var loginDialog_ = null;
  function login(serverUrl, authenticated) {
    var cmisNameInput,
      cmisPasswordInput;
    // pop-up an authentication window,
    if (!loginDialog_) {
      loginDialog_ = workspace.createDialog();

      var cD = goog.dom.createDom;

      cmisNameInput = cD('input', {id: 'cmis-name', type: 'text'});
      cmisNameInput.setAttribute('autocorrect', 'off');
      cmisNameInput.setAttribute('autocapitalize', 'none');
      cmisNameInput.setAttribute('autofocus', '');

      cmisPasswordInput = cD('input', {id: 'cmis-passwd', type: 'password'});

      goog.dom.appendChild(loginDialog_.getElement(),
        cD('div', 'cmis-login-dialog',
          cD('label', '',
            tr(msgs.NAME_) + ': ',
            cmisNameInput
          ),
          cD('label', '',
            tr(msgs.PASSWORD_)+ ': ',
            cmisPasswordInput
          )
        )
      );

      loginDialog_.setTitle(tr(msgs.AUTHENTICATION_REQUIRED_));
      loginDialog_.setPreferredSize(300, null);
    }
    loginDialog_.onSelect(function(key) {
      if (key === 'ok') {
        // Send the user and password to the login servlet which runs in the webapp.
        var userField = cmisNameInput || document.getElementById('cmis-name');
        var user = userField.value.trim();
        var passwdField = cmisPasswordInput || document.getElementById('cmis-passwd');
        var passwd = passwdField.value;

        userField.value = '';
        passwdField.value = '';

        goog.net.XhrIo.send(
          '../plugins-dispatcher/cmis-login',
          function () {
            localStorage.setItem('cmis.user', user);

            authenticated && authenticated();
          },
          'POST',
          // form params
          goog.Uri.QueryData.createFromMap(new goog.structs.Map({
            user: user,
            passwd: passwd,
          })).toString()
        );
      }
    });

    loginDialog_.show();
    var lastUser = localStorage.getItem('cmis.user');
    if(lastUser) {
      var userInput = cmisNameInput || loginDialog_.getElement().querySelector('#cmis-name');
      userInput.value = lastUser;
      userInput.select();
    }

    
}

	window.login = login;

  cmisFileRepo.login = login;
  cmisFileRepo.logout = function (logoutCallback) {
    goog.net.XhrIo.send(
      '../plugins-dispatcher/cmis-login?action=logout',
      goog.bind(function () {
        try {
          localStorage.removeItem('cmis.user');
        } catch (e) {
          console.warn(e);
        }
        logoutCallback();
      }, this),
      'POST');
};



  cmisFileRepo.createRepositoryAddressComponent = function (rootUrlParam, currentBrowseUrl, rootURLChangedCallback) {
    var div = document.createElement('div');

    if (rootUrl) {
      console.log(rootUrl, rootUrlParam, currentBrowseUrl);
      if (!rootUrlParam) {
      if (!this.rootUrlSet) {
        this.rootUrlSet = true;
        setTimeout(function() {
          rootURLChangedCallback(rootUrl, rootUrl) // TODO: bug in web author.
        }, 0)
      // rootURLChangedCallback(rootUrl, rootUrl); // TODO: bug in web author.
      }
      }
      div.textContent = 'CMIS'; // TODO: use the CMIS server host name
  
    } else {
      div.textContent = 'Please set the CMIS API URL in the Admin Page Configuration.'; // TODO link to documentation.
    }
    return div;
  };

  

cmisFileRepo.getUrlInfo = function (url, urlInfoCallback, showErrorMessageCallback) {
  urlInfoCallback(rootUrl, url);
};


  // -------- Initialize the file browser information ------------
var cmisFileRepositoryDescriptor = {
    'id' : 'cmis',
    'name' : 'CMIS',
    'icon' : sync.util.computeHdpiIcon('../plugin-resources/cmis/cmis.png'), // the large icon url, hidpi enabled.
    'matches' : function matches(url) {
      return url.match('cmis'); // Check if the provided URL points to a file or folder from Cmis file repository
    },
    'repository' : cmisFileRepo
};


 
workspace.getFileRepositoriesManager().registerRepository(cmisFileRepositoryDescriptor);


  })();


  