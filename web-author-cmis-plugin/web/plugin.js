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
      console.log(rootUrl, rootUrlParam, currentBrowseUrl);
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
      div.textContent = 'Please set the CMIS API URL in the Admin Page Configuration.'; // TODO link to documentation.
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
      return url.match('cmis'); // Check if the provided URL points to td file or folder from Cmis file repository
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
  return 'Check Out';
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
cancelCmisCheckOutAction.prototype.constructor = CmisCheckOutAction;
cancelCmisCheckOutAction.prototype.getDisplayName = function () {
  return 'Cancel Check Out';
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
CmisCheckInAction.prototype.constructor = CmisCheckOutAction;
CmisCheckInAction.prototype.getDisplayName = function () {
  return 'Check In';
};

CmisCheckInAction.prototype.actionPerformed = function (callback) {
  if (!this.dialog) {
    this.dialog = workspace.createDialog();
    this.dialog.setTitle('Check In');
    this.dialog.getElement().innerHTML = " Enter the commit message: <br>";
    
    var form = document.createElement('form');
    var input = document.createElement('input');

    var text1 = document.createElement('label');
    var text2 = document.createElement('label');

    input.setAttribute('type', 'text');
    input.setAttribute('id', 'commit');
    
    this.dialog.getElement().appendChild(input);
    
    form.setAttribute('action', '');
    radioButtonsCreator(form, text1, text2);

    this.dialog.getElement().appendChild(form);
  }

  this.dialog.show();

  var editor = this.editor;
  this.dialog.onSelect(goog.bind(function (key, e) {
    if(key === 'ok'){

    var commitMessage = this.dialog.getElement().querySelector('#commit').value;
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
  return 'All versions';
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
      this.dialog.setTitle('All versions');
      this.dialog.setButtonConfiguration(sync.api.Dialog.ButtonConfiguration.CANCEL);
      this.dialog.setPreferredSize(500, 400);

      let style = document.createElement('style');
      style.innerHTML = "table,td,th{border:1px solid #ddd;text-align:left;}" + 
            "table{border-collapse:collapse;width:96%;}th,td{padding:15px;}";

      let table = document.createElement('table');

      let tr = document.createElement('tr');
      let th = document.createElement('th');
      let th1 = document.createElement('th');

      th.innerHTML = "Version";
      tr.appendChild(th);

      th1.innerHTML = "Commit";
      tr.appendChild(th1);

      table.appendChild(tr);

      for(key in jsonFile){
        let tr = document.createElement('tr');
        let td = document.createElement('td');
        let a = document.createElement('a');
        let value = jsonFile[key];

        let commit = document.createElement('td');
        commit.setAttribute('style', 'margin-left:50px;');

        if(value[1] !== "" || value[1] !== null){
          commit.innerHTML = value[1];
        } 

        let hrefUrl = window.location.origin + window.location.pathname + value[0];

        a.setAttribute('href', hrefUrl);
        a.setAttribute('target','_blank');
        a.setAttribute('style', 'margin-right:25px;color:#323233;text-decoration:none;');
        a.innerHTML = key;

        td.appendChild(a);
        tr.appendChild(td);
        tr.appendChild(commit);
        table.appendChild(tr);
      }

      this.dialog.getElement().appendChild(style);
      this.dialog.getElement().appendChild(table);
      this.dialog.show();
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

  text1.innerHTML = 'Major Version';
  text1.appendChild(document.createElement('br'));
  form.appendChild(text1);

  var radio2 = document.createElement('input');
  radio2.setAttribute('type', 'radio');
  radio2.setAttribute('name', 'state');
  radio2.setAttribute('id', 'radio2');
  radio2.setAttribute('value', 'minor');
  form.appendChild(radio2);

  text2.innerHTML = 'Minor Version';
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
 
  console.log(doccheckedout);

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