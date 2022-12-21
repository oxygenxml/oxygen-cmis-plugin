/**
 * Action that displays the document's older versions in a dialog.
 *
 * @param editor the current editor.
 * @param {CmisStatus} status the document status.
 */
var ListOldVersionsAction = function(editor, status) {
  sync.actions.AbstractAction.call(this, '');
  this.editor_ = editor;
  this.status_ = status;
  this.dialog_ = null;
};
goog.inherits(ListOldVersionsAction, sync.actions.AbstractAction);

/** @override */
ListOldVersionsAction.prototype.getDisplayName = function() {
  return tr(msgs.ALL_VERSIONS_);
};

/** @override */
ListOldVersionsAction.prototype.getSmallIcon = function() {
  return sync.util.computeHdpiIcon('../plugin-resources/cmis/icons/ShowVersionHistory16.png');
};

/** @override */
ListOldVersionsAction.prototype.actionPerformed = function(callback) {
  // Check if the server supports Commit Message.
  let supportsCommitMessage = this.status_.supportsCommitMessage();
 
  var allVerDialog = this.getDialog_(supportsCommitMessage);
  allVerDialog.show();

  allVerDialog.onSelect(function(e) {
    callback();
  });

  this.editor_.getActionsManager().invokeOperation(
    'com.oxygenxml.cmis.web.action.CmisOldVersions', {
      action: 'listOldVersions'
    }, goog.bind(this.handleOperationResult_, this, allVerDialog.getElement(), supportsCommitMessage));
};

/**
 * Creates the versions display dialog.
 *
 * @param supportsCommitMessage if the server supports Private Working Copy and commit messages.
 * @return the versions display dialog.
 * @private
 */
ListOldVersionsAction.prototype.getDialog_ = function(supportsCommitMessage) {
  var allVerDialog = this.dialog_;
  if(!allVerDialog) {
    allVerDialog = workspace.createDialog();
    allVerDialog.setTitle(tr(msgs.VERSION_HISTORY));
    allVerDialog.setButtonConfiguration([{key: 'close', caption: tr(msgs.CLOSE_)}]);
    this.dialog_ = allVerDialog;
  } else {
    // Clear the dialog element to render the new versions table.
    goog.dom.removeChildren(allVerDialog.getElement());
  }

  if (supportsCommitMessage) {
    allVerDialog.setPreferredSize(750, 550);
    allVerDialog.setResizable(true);
  } else {
    allVerDialog.setPreferredSize(430, 500);
  }
  var loader = document.createElement('div');
  loader.setAttribute('id', 'cmis-loader');
  allVerDialog.getElement().appendChild(loader);

  return allVerDialog;
};

/**
 * Handles the version information received from the operation.
 *
 * @param container the container in which to display the versions.
 * @param supportsCommitMessage whether the server supports private working copies.
 * @param err errors that appeared.
 * @param data the data.
 * @private
 */
ListOldVersionsAction.prototype.handleOperationResult_ = function(container, supportsCommitMessage, err, data) {
  // remove selection from document.
  document.activeElement.blur();
  goog.dom.removeNode(container.querySelector("#cmis-loader"));

  var versions = JSON.parse(data);
  goog.dom.append(container, this.createTable_(versions, supportsCommitMessage));

    // In case of older version, scroll it into view.
  var oldVersionSelected = document.querySelector('.current-version:not(:first-child)');
  if (oldVersionSelected) {
    oldVersionSelected.scrollIntoView(false);
  }
};

/**
 * Creates the versions table.
 *
 * @param versions the versions list.
 * @param supportsCommitMessage whether the server supports commit messages.
 *
 * @return {*} the HTML table.
 * @private
 */
ListOldVersionsAction.prototype.createTable_ = function(versions, supportsCommitMessage) {
  var table = goog.dom.createDom('table', 'cmis-history-table');
  var isLatestVersionOpenedNow = location.href.indexOf('oldversion') === -1;

  let headerRow = goog.dom.createDom('tr', 'table-header-row',
      [goog.dom.createDom('th', null, "Version"),
      goog.dom.createDom('th', null, "Creator")]);
  if(supportsCommitMessage) {
    let headerCommitMessage = goog.dom.createDom('th', null, 'Check-in Message');
    headerRow.appendChild(headerCommitMessage);
  }
  table.appendChild(goog.dom.createDom('thead', null, headerRow));

  for(let version of versions.length) {
    if (version.version === 'filename') {
      continue;
    }
    var versionUrl = version.url;

    var isThisVersionOpenedNow = window.location.search.indexOf(versionUrl) !== -1;
    var isThisVersionOld = versionUrl.indexOf('oldversion') !== -1;
    var isThisCurrentVersion = (isThisVersionOpenedNow && isThisVersionOld) || (isLatestVersionOpenedNow && !isThisVersionOld);

    var href = window.location.origin + window.location.pathname + versionUrl;
    var versionLink = goog.dom.createDom('a', {
        className: 'cmis-old-version-link',
        href: isThisCurrentVersion ? '#' : href,
        target: '_blank'
      }, version.version);

    var versionTd = goog.dom.createDom('td', 'cmis-version', versionLink);
    var userTd = goog.dom.createDom('td', {class: 'cmis-user', title: version.author}, version.author);

    var tr = goog.dom.createDom('tr', {
          className: isThisCurrentVersion ? 'current-version' : '',
          title: isThisCurrentVersion ? 'Opened document version' : ''
        },
        versionTd, userTd)
    if (supportsCommitMessage) {
      var processedCommitMessage = version.commitMessage.replace("/\n", "&#10;");
      var checkinMessageTd = goog.dom.createDom('td', 'cmis-checkin-message',
          goog.dom.createDom('span', {title: processedCommitMessage}, version.commitMessage));
      tr.appendChild(checkinMessageTd);
    }

    table.appendChild(tr);
  }

  
  return table;
};