(function () {
  
    var regExpOption = sync.options.PluginsOptions.getClientOption('restRootRegExp');
    var ROOT_REGEXP = regExpOption ? new RegExp(regExpOption) : null;
  
    goog.events.listen(workspace, sync.api.Workspace.EventType.BEFORE_EDITOR_LOADED, function(e) {
      console.log('Plugin loaded successfully');
	  
      var url = e.options.url;
      // If the URL has 'rest' protocol we use the rest protocol handler.
      if (url.match('cmis')) {
        var initialUrl = decodeURIComponent(sync.util.getURLParameter('url'));
        var prefix = 'cmis://';

        var limit = initialUrl.substring(prefix.length).indexOf('/') + prefix.length;
        var rootUrl = initialUrl.substring(0, limit);
      

        // initialUrl = initialUrl.replace('/' + repoId);

        // console.log('RepoID ' + repoId);

        console.log('Initial URL ' + initialUrl);
        console.log('Root URL ' + rootUrl);
        // set the workspace UrlChooser
        workspace.setUrlChooser(new sync.api.FileBrowsingDialog({
          initialUrl: initialUrl,
          rootUrl: rootUrl
        }));
      }
    });
  
  
  /**
   * Login the user and call this callback at the end.
   *
   * @param {function} authenticated The callback when the user was authenticated - successfully or not.
   */
  var loginDialog_ = null;
  function login(authenticated) {
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
  sync.options.PluginsOptions.getClientOption("cmis.enforced_url")
  })();


  