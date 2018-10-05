sync.util.loadCSSFile("../plugin-resources/cmis/style.css");

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

        cmisNameInput = cD('input', {
            id: 'cmis-name',
            type: 'text'
        });
        cmisNameInput.setAttribute('autocorrect', 'off');
        cmisNameInput.setAttribute('autocapitalize', 'none');
        cmisNameInput.setAttribute('autofocus', '');

        cmisPasswordInput = cD('input', {
            id: 'cmis-passwd',
            type: 'password'
        });

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
                function() {
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
cmisFileRepo.logout = function(logoutCallback) {
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

cmisFileRepo.createRepositoryAddressComponent = function(rootUrlParam, currentBrowseUrl, rootURLChangedCallback) {
    var div = document.createElement('div');

    if (rootUrl) {
        if (!rootUrlParam) {
            if (!this.rootUrlSet) {
                this.rootUrlSet = true;
                setTimeout(function() {
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

cmisFileRepo.getUrlInfo = function(url, urlInfoCallback, showErrorMessageCallback) {
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


var cmisStatus = false;

goog.events.listen(workspace, sync.api.Workspace.EventType.EDITOR_LOADED, function(e) {
    var editor = e.editor;

    var root = document.querySelector('[data-root="true"]');
    var nonversionable = root.getAttribute('data-pseudoclass-nonversionable');
    var doccheckedout = root.getAttribute('data-pseudoclass-checkedout');
    var block = root.getAttribute('data-pseudoclass-block');

    if (doccheckedout === 'true') {
        cmisStatus = true;
    }

    if (block === 'true') {
        cmisStatus = 'checkedout';
    }

    // Register the newly created action.
    if (nonversionable !== 'true') {
        editor.getActionsManager().registerAction('cmisCheckOut.link', new CmisCheckOutAction(editor));
        editor.getActionsManager().registerAction('cancelCmisCheckOut.link', new cancelCmisCheckOutAction(editor));
        editor.getActionsManager().registerAction('cmisCheckIn.link', new CmisCheckInAction(editor));
        editor.getActionsManager().registerAction('listOldVersion.link', new listOldVersionsAction(editor));

        addToDitaToolbar(editor, 'cmisCheckOut.link', 'cmisCheckIn.link', 'cancelCmisCheckOut.link', 'listOldVersion.link');
    }
});

function addToDitaToolbar(editor, checkOutId, checkInId, cancelCheckOutId, listOldVersionsId) {
    goog.events.listen(editor, sync.api.Editor.EventTypes.ACTIONS_LOADED, function(e) {
        var actionsConfig = e.actionsConfiguration;

        var builtinToolbar = null;
        if (actionsConfig.toolbars) {
            for (var i = 0; i < actionsConfig.toolbars.length; i++) {
                var toolbar = actionsConfig.toolbars[i];
                if (toolbar.name === "Builtin") {
                    builtinToolbar = toolbar;
                }
            }
        }

        if (builtinToolbar) {
            builtinToolbar.children.push({
                displayName: tr(msgs.CMIS_ACTIONS_),
                type: 'list',
                name: 'cmis-actions',
                children: [{
                    id: listOldVersionsId,
                    type: 'action'
                }, {
                    id: checkOutId,
                    type: 'action'
                }, {
                    id: checkInId,
                    type: 'action'
                }, {
                    id: cancelCheckOutId,
                    type: 'action'
                }]
            });
        }
    });
}