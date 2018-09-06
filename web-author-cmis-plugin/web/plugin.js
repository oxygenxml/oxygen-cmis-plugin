sync.util.loadCSSFile("../plugin-resources/cmis/style.css");

(function () {
  var initialUrl = decodeURIComponent(sync.util.getURLParameter('url'));
  var prefix = 'cmis://';

  var limit = initialUrl.substring(prefix.length).indexOf('/') + prefix.length;
  var rootUrl = initialUrl.substring(0, limit);

  var urlFromOptions = sync.options.PluginsOptions.getClientOption("cmis.enforced_url");
  var rootUrl = null;

  if (urlFromOptions) {
    rootUrl = 'cmis://' + encodeURIComponent(urlFromOptions);
    if (urlFromOptions.lastIndexOf('/') !== urlFromOptions.length) {
      rootUrl += '/';
    }
  }

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

      cmisNameInput = cD('input', { id: 'cmis-name', type: 'text' });
      cmisNameInput.setAttribute('autocorrect', 'off');
      cmisNameInput.setAttribute('autocapitalize', 'none');
      cmisNameInput.setAttribute('autofocus', '');

      cmisPasswordInput = cD('input', { id: 'cmis-passwd', type: 'password' });

      goog.dom.appendChild(loginDialog_.getElement(),
        cD('div', 'cmis-login-dialog',
          cD('label', '',
            tr(msgs.NAME_) + ': ',
            cmisNameInput
          ),
          cD('label', '',
            tr(msgs.PASSWORD_) + ': ',
            cmisPasswordInput
          )
        )
      );

      loginDialog_.setTitle(tr(msgs.AUTHENTICATION_REQUIRED_));
      loginDialog_.setPreferredSize(300, null);
    }

    loginDialog_.onSelect(function (key) {
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
    if (lastUser) {
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
      if (!rootUrlParam) {
        if (!this.rootUrlSet) {
          this.rootUrlSet = true;
          setTimeout(function () {
            rootURLChangedCallback(rootUrl, rootUrl) // TODO: bug in web author.
          }, 0)
        }
      }
      div.textContent = sync.options.PluginsOptions.getClientOption("cmis.enforced_name");
    } else {
      div.textContent = tr(msgs.SET_CMIS_API_URL_); // TODO link to documentation.
    }
    return div;
  };

  cmisFileRepo.getUrlInfo = function (url, urlInfoCallback, showErrorMessageCallback) {
    urlInfoCallback(rootUrl, url);
  };

  // -------- Initialize the file browser information ------------
  var cmisFileRepositoryDescriptor = {
    'id': 'cmis',
    'name': sync.options.PluginsOptions.getClientOption('cmis.enforced_name'),
    'icon': sync.util.computeHdpiIcon(sync.options.PluginsOptions.getClientOption('cmis.enforced_icon')),
    'matches': function matches(url) {
      return url.match('cmis'); // Check if the provided URL points to version file or folder from Cmis file repository
    },
    'repository': cmisFileRepo
  };

  workspace.getFileRepositoriesManager().registerRepository(cmisFileRepositoryDescriptor);
})();

//------------------- Cmis check-out action. -----------------------

//Button's state variable.
var checkedOut = false;

var CmisCheckOutAction = function (editor) {
  sync.actions.AbstractAction.call(this, '');
  this.editor = editor;
};


CmisCheckOutAction.prototype = Object.create(sync.actions.AbstractAction.prototype);
CmisCheckOutAction.prototype.constructor = CmisCheckOutAction;
CmisCheckOutAction.prototype.getDisplayName = function () {
  return tr(msgs.CHECK_OUT_);
};

CmisCheckOutAction.prototype.actionPerformed = function (callback) {
  this.editor.getActionsManager().invokeOperation(
    'com.oxygenxml.cmis.web.action.CmisActions', {
      action: 'cmisCheckout'
    }, callback);

    checkedOut = true;
};

//------------------- Cmis cancel check-out action. -----------------------
var cancelCmisCheckOutAction = function (editor) {
  sync.actions.AbstractAction.call(this, '');
  this.editor = editor;
};

cancelCmisCheckOutAction.prototype = Object.create(sync.actions.AbstractAction.prototype);
cancelCmisCheckOutAction.prototype.constructor = cancelCmisCheckOutAction;
cancelCmisCheckOutAction.prototype.getDisplayName = function () {
  return tr(msgs.CANCEL_CHECK_OUT_);
};

cancelCmisCheckOutAction.prototype.actionPerformed = function (callback) {
  this.editor.getActionsManager().invokeOperation(
    'com.oxygenxml.cmis.web.action.CmisActions', {
      action: 'cancelCmisCheckout'
    }, callback);

    checkedOut = false;
};

//------------------- Cmis check-in action. -----------------------
var CmisCheckInAction = function (editor) {
  sync.actions.AbstractAction.call(this, '');
  this.editor = editor;
};
CmisCheckInAction.prototype = Object.create(sync.actions.AbstractAction.prototype);
CmisCheckInAction.prototype.constructor = CmisCheckInAction;
CmisCheckInAction.prototype.getDisplayName = function () {
  return tr(msgs.CHECK_IN_);
};

CmisCheckInAction.prototype.actionPerformed = function (callback) {
  if (!this.dialog) {
    this.dialog = workspace.createDialog();
    this.dialog.setTitle(tr(msgs.CHECK_IN_MESSAGE_));
    this.dialog.getElement().innerHTML = tr(msgs.CHECK_IN_MESSAGE_) +"<br>";
    this.dialog.setPreferredSize(300, 350);

    var form = document.createElement('form');
    var input = document.createElement('textarea');
    input.setAttribute('style', 'margin:0px;width:250px;height:125px;resize:none;');

    var text1 = document.createElement('label');
    var text2 = document.createElement('label');

    input.setAttribute('type', 'text');
    input.setAttribute('id', 'input');
    
    this.dialog.getElement().appendChild(input);
    
    form.setAttribute('action', '');
    radioButtonsCreator(form, text1, text2);

    this.dialog.getElement().appendChild(form);
  }

  this.dialog.show();

  var editor = this.editor;
  this.dialog.onSelect(goog.bind(function (key, e) {
    if(key === 'ok'){

    var commitMessage = this.dialog.getElement().querySelector('#input').value;
    var verstate;

    if(this.dialog.getElement().querySelector('#radio1').checked) {
      verstate = this.dialog.getElement().querySelector('#radio1').value;
    } else if(this.dialog.getElement().querySelector('#radio2').checked) {
      verstate = this.dialog.getElement().querySelector('#radio2').value;
    }

    editor.getActionsManager().invokeOperation(
      'com.oxygenxml.cmis.web.action.CmisActions', {
        action: 'cmisCheckin',
        commit: commitMessage,
        state: verstate
      }, callback);
      checkedOut = false;
    }
  }, 
  this));
};

//------------------- List old version of document action. -----------------------
var listOldVersionsAction = function (editor) {
  sync.actions.AbstractAction.call(this, '');
  this.editor = editor;
};

listOldVersionsAction.prototype = Object.create(sync.actions.AbstractAction.prototype);
listOldVersionsAction.prototype.constructor = CmisCheckOutAction;
listOldVersionsAction.prototype.getDisplayName = function () {
  return tr(msgs.ALL_VERSIONS_);
};

listOldVersionsAction.prototype.actionPerformed = function (callback) {
  this.editor.getActionsManager().invokeOperation(
    'com.oxygenxml.cmis.web.action.CmisActions', {
      action: 'listOldVersions'
    }, goog.bind(this.afterList_, this, callback));
};

listOldVersionsAction.prototype.afterList_ = function (callback, err, data) {
  this.editor.getActionsManager().invokeOperation(
    'com.oxygenxml.cmis.web.action.CmisActions', {
      action: ''
    }, function (err, data) {
  
      var jsonFile = JSON.parse(data);
      
      


      this.dialog = workspace.createDialog();
      this.dialog.setTitle(this.tr(msgs.ALL_VERSIONS_));
      this.dialog.setButtonConfiguration(sync.api.Dialog.ButtonConfiguration.CANCEL);
      this.dialog.setPreferredSize(750, 550);
      this.dialog.setResizable(true);

      this.dialog.show();

      let div = document.createElement('div');
      div.setAttribute('id', 'head');

      let childDiv = document.createElement('div');
      childDiv.setAttribute('id', 'versionDiv');
      childDiv.setAttribute('class', 'headtitle');
      childDiv.innerHTML = tr(msgs.VERSION_);
      
      let childDiv1 = document.createElement('div');
      childDiv1.setAttribute('id', 'userDiv');
      childDiv1.setAttribute('class', 'headtitle');
      childDiv1.innerHTML = tr(msgs.MODIFIED_BY_);

      let childDiv2 = document.createElement('div');
      childDiv2.setAttribute('id', 'commitDiv');
      childDiv2.setAttribute('class', 'headtitle');
      childDiv2.innerHTML = tr(msgs.COMMIT_MESS_);

      div.appendChild(childDiv);
      div.appendChild(childDiv1);
      div.appendChild(childDiv2);

      let table = document.createElement('table');
      table.setAttribute('id', 'table');

      for(key in jsonFile){
        let tr = document.createElement('tr');
      
        let versionTd = document.createElement('td');
        versionTd.setAttribute('id', 'version');
        versionTd.setAttribute('class', 'td');

        let versionLink = document.createElement('a');
        let value = jsonFile[key];

        let commitTd = document.createElement('td');
        commitTd.setAttribute('id', 'commit');
        commitTd.setAttribute('class', 'td');

        if(value[1] !== "" || value[1] !== null){
          commitTd.innerHTML = value[1];
        } 

        let href = window.location.origin + window.location.pathname + value[0];

        let oldVer = value[0];
        oldVer = oldVer.substring(oldVer.lastIndexOf('='), oldVer.length);

        versionLink.setAttribute('href', href);
        versionLink.setAttribute('target','_blank');
        versionLink.setAttribute('class', 'oldlink');
        versionLink.innerHTML = key;
        
        let userTd = document.createElement('td');
        userTd.setAttribute('id', 'user');
        userTd.setAttribute('class', 'td');
        userTd.innerHTML = value[2];
        
        if(window.location.search.includes(oldVer)) {
          versionLink.setAttribute('href', '#');
          versionLink.setAttribute('style', 'text-decoration:none;');

          versionTd.style.backgroundColor = '#ededed';
          commitTd.style.backgroundColor = '#ededed';
          userTd.style.backgroundColor = '#ededed';
        }
        
        versionTd.appendChild(versionLink);
        tr.appendChild(versionTd);
        tr.appendChild(userTd);
        tr.appendChild(commitTd);
        table.appendChild(tr);
      }

      this.dialog.getElement().appendChild(div);
      this.dialog.getElement().appendChild(table);
     
      var versionBarWidth = document.getElementById('version').offsetWidth;
      var userBarWidth = document.getElementById('user').offsetWidth;
      var commitBarWidth = document.getElementById('commit').offsetWidth;

      document.getElementById('versionDiv').style.width = versionBarWidth + 'px';
      document.getElementById('userDiv').style.width = userBarWidth + 'px';
      document.getElementById('commitDiv').style.width = commitBarWidth + 'px';
      
      this.dialog.onSelect(goog.bind(function(e){
        this.dialog.dispose();
      }, this));

    });

  callback();
}

//------------------- Radio buttons. -----------------------
function radioButtonsCreator(form, text1, text2){
  var radio1 = document.createElement('input');
  radio1.setAttribute('type', 'radio');
  radio1.setAttribute('name', 'state');
  radio1.setAttribute('id', 'radio1');
  radio1.setAttribute('value', 'major');
  radio1.setAttribute('checked', '');
  form.appendChild(radio1);

  text1.innerHTML = tr(msgs.MAJOR_VERSION_);
  text1.appendChild(document.createElement('br'));
  form.appendChild(text1);

  var radio2 = document.createElement('input');
  radio2.setAttribute('type', 'radio');
  radio2.setAttribute('name', 'state');
  radio2.setAttribute('id', 'radio2');
  radio2.setAttribute('value', 'minor');
  form.appendChild(radio2);

  text2.innerHTML = tr(msgs.MINOR_VERSION_);
  form.appendChild(text2);
}

//------------------- Enable buttons with state dependence. -----------------------
CmisCheckOutAction.prototype.isEnabled = function () {
  if (checkedOut === 'checkedoutby') {
    return false;
  }
  if (checkedOut) {
    return false;
  }
  return true;
}

CmisCheckInAction.prototype.isEnabled = function () {
  if (checkedOut === 'checkedoutby') {
    return false;
  }
  if (checkedOut) {
    return true;
  }
  return false;
}

cancelCmisCheckOutAction.prototype.isEnabled = function () {
  if (checkedOut === 'checkedoutby') {
    return false;
  }
  if (checkedOut) {
    return true;
  }
  return false;
}

//------------------- Actions when editor is loaded. -----------------------
goog.events.listen(workspace, sync.api.Workspace.EventType.EDITOR_LOADED, function (e) {
  var editor = e.editor;

  var root = document.querySelector('[data-root="true"]');
  var nonversionable = root.getAttribute('data-pseudoclass-nonversionable');
  var doccheckedout = root.getAttribute('data-pseudoclass-checkedout');
  var block = root.getAttribute('data-pseudoclass-block');

  if(doccheckedout === 'true'){
    checkedOut = true;
  } 

  if(block === 'true'){
    checkedOut = 'checkedoutby';
  }

  // Register the newly created action.
  if(nonversionable !== 'true'){
    editor.getActionsManager().registerAction('cmisCheckOut.link', new CmisCheckOutAction(editor));
    editor.getActionsManager().registerAction('cancelCmisCheckOut.link', new cancelCmisCheckOutAction(editor));
    editor.getActionsManager().registerAction('cmisCheckIn.link', new CmisCheckInAction(editor));
    editor.getActionsManager().registerAction('listOldVersion.link', new listOldVersionsAction(editor));

    addToDitaToolbar(editor, 'cmisCheckOut.link', 'cmisCheckIn.link', 'cancelCmisCheckOut.link', 'listOldVersion.link');
  }
});

function addToDitaToolbar(editor, checkOutId, checkInId, cancelCheckOutId, listOldVersionsId) {
  goog.events.listen(editor, sync.api.Editor.EventTypes.ACTIONS_LOADED, function (e) {
    var actionsConfig = e.actionsConfiguration;

    var builtinToolbar = null;
    if (actionsConfig.toolbars) {
      for (var i = 0; i < actionsConfig.toolbars.length; i++) {
        var toolbar = actionsConfig.toolbars[i];
        if (toolbar.name == "Builtin") {
          builtinToolbar = toolbar;
        }
      }
    }

    if (builtinToolbar) {
      builtinToolbar.children.push({
        displayName: 'CMIS',
        type: 'list',
        children: [{
          id: checkOutId,
          type: 'action'
        }, {
          id: cancelCheckOutId,
          type: 'action'
        }, {
          id: checkInId,
          type: 'action'
        },{
          id: listOldVersionsId,
          type: 'action'
        }]
      });
    }
  });
}