// -------- Initialize the file browser information ------------
  var hasServiceAccount = 'true' === sync.options.PluginsOptions.getClientOption('cmis.has_service_account');

  var cmisFileRepositoryDescriptor = {
    'id': 'cmis',
    'name': sync.options.PluginsOptions.getClientOption('cmis.enforced_name'),
    'icon': sync.util.computeHdpiIcon(sync.options.PluginsOptions.getClientOption('cmis.enforced_icon')),
    'matches': function matches(url) {
      return url.match(CmisFileServer.PROTOCOL_PREFIX); // Check if the provided URL points to version file or folder from Cmis file repository
    },
    'fileServer': new CmisFileServer()
  };

  workspace.getFileServersManager().registerFileServerConnector(cmisFileRepositoryDescriptor);

  goog.events.listen(workspace, sync.api.Workspace.EventType.EDITOR_LOADED, function(e) {
    var editor = e.editor;
    if (editor.getUrl().indexOf(CmisFileServer.PROTOCOL_PREFIX) === 0) {
      var root = document.querySelector('[data-root="true"]');
      var nonversionable = root.getAttribute('data-pseudoclass-nonversionable');
      // Register the newly created action.
      if (nonversionable !== 'true') {
        addCustomActions(editor, new CmisStatus(root));
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
    actionsManager.registerAction('listOldVersion.link', new ListOldVersionsAction(editor, status));

    goog.events.listen(editor, sync.api.Editor.EventTypes.ACTIONS_LOADED, function(e) {
      var toolbars = e.actionsConfiguration.toolbars;
      
      if (hasServiceAccount) {
        // If a service account is configured, logout in the editor is useless.
        e.actionsConfiguration.removeAction('cmis/Logout');
      }

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
