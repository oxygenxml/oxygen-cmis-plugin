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

  // True if web-auhtor-diff-plugin is installed.
  let canPerformDiff = sync && sync.diff && typeof sync.diff.DiffApi === 'function';

  var allVerDialog = this.getDialog_(supportsCommitMessage, canPerformDiff);
  allVerDialog.show();
  allVerDialog.onSelect((option, e) => {
    if (option === "diff") {
      // keep the Version History dialog open when choosing "Diff".
      e.preventDefault();
      this.showDiff_(allVerDialog.getElement());
    } else {
      callback();
    }
  });

  this.editor_.getActionsManager().invokeOperation(
    'com.oxygenxml.cmis.web.action.CmisOldVersions', {
      action: 'listOldVersions'
    }, goog.bind(this.handleOperationResult_, this, allVerDialog.getElement(), supportsCommitMessage, canPerformDiff));
};

ListOldVersionsAction.prototype.showDiff_ = function(versionHistoryDialogElement) {
  let leftDocInput = versionHistoryDialogElement.querySelector("input[name='cmis-diff-left']:checked");
  let rightDocInput = versionHistoryDialogElement.querySelector("input[name='cmis-diff-right']:checked");
  let isLeftCurrentVersion = leftDocInput.getAttribute("data-currentversion") === "true";

  let rightDocUrl = rightDocInput.value;
  let rightDocVersion = rightDocInput.getAttribute("data-version");

  if (isLeftCurrentVersion) {
    workspace.serviceLoader.loadServices(sync.internal_api.DiffApi)
      .then(([DiffApi]) => {
        let diffApi = new DiffApi(this.editor_).withRightLabel(rightDocVersion);
        let promise = diffApi.canMerge() ? diffApi.mergeWith(rightDocUrl) : diffApi.compareWith(rightDocUrl);
        promise.catch(err => {
          window.workspace.getNotificationManager().showError("Cannot store changes. " + JSON.stringify(err));
        });
      });
  } else {
    let leftDocUrl = leftDocInput.value;
    let leftDocVersion = leftDocInput.getAttribute("data-version");
    workspace.serviceLoader.loadServices(sync.internal_api.DiffTool)
      .then(([DiffTool]) => {
        let dialog = workspace.createDialog(tr(msgs.Diff));
        dialog.show();
        dialog.setPreferredSize(9000, 9000);

        let diffParams = new sync.internal_api.DiffParams(sync.internal_api.DiffType.DIFF, "", leftDocUrl);
        diffParams.rightUrl = rightDocUrl;
        diffParams.leftEditorLabel = leftDocVersion;
        diffParams.rightEditorLabel = rightDocVersion;

        let diffTool = new DiffTool(dialog.getElement());
        diffTool.showDiffEditor(diffParams, {})
      });
  }
}

/**
 * Creates the versions display dialog.
 *
 * @param {boolean} supportsCommitMessage if the server supports Private Working Copy and commit messages.
 * @param {boolean} canPerformDiff True if can perform diff.
 * @return the versions display dialog.
 * @private
 */
ListOldVersionsAction.prototype.getDialog_ = function(supportsCommitMessage, canPerformDiff) {
  var allVerDialog = this.dialog_;
  if(!allVerDialog) {
    allVerDialog = workspace.createDialog();
    allVerDialog.setTitle(tr(msgs.VERSION_HISTORY));
    if (canPerformDiff) {
      allVerDialog.setButtonConfiguration([{key: 'diff', caption: tr(msgs.Diff)}, {key: 'close', caption: tr(msgs.CLOSE_)}]);
    } else {
      allVerDialog.setButtonConfiguration([{key: 'close', caption: tr(msgs.CLOSE_)}]);
    }
    allVerDialog.setResizable(true);
    this.dialog_ = allVerDialog;
  } else {
    // Clear the dialog element to render the new versions table.
    goog.dom.removeChildren(allVerDialog.getElement());
  }

  if (supportsCommitMessage) {
    allVerDialog.setPreferredSize(750, 550);
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
 * @param {HTMLElement} container the container in which to display the versions.
 * @param {boolean} supportsCommitMessage whether the server supports private working copies.
 * @param {boolean} canPerformDiff True if can perform diff.
 * @param err errors that appeared.
 * @param data the data.
 * @private
 */
ListOldVersionsAction.prototype.handleOperationResult_ = function(container, supportsCommitMessage, canPerformDiff, err, data) {
  // remove selection from document.
  document.activeElement.blur();
  goog.dom.removeNode(container.querySelector("#cmis-loader"));

  /**
   * @type {[{version: string, isCurrentVersion: boolean, url: string, commitMessage: string}]}
   */
  var versions = JSON.parse(data);
  goog.dom.append(container, this.createTable_(versions, supportsCommitMessage, canPerformDiff));

    // In case of older version, scroll it into view.
  var oldVersionSelected = document.querySelector('.current-version');
  if (oldVersionSelected) {
    oldVersionSelected.scrollIntoView(false);
  }
};

/**
 * Creates the versions table.
 *
 * @param {[{version: string, isCurrentVersion: boolean, url: string, commitMessage: string}]} versions The versions list.
 * @param {boolean} supportsCommitMessage whether the server supports commit messages.
 * @param {boolean} canPerformDiff True if can perform diff.
 *
 * @return {HTMLTableElement} the HTML table.
 * @private
 */
ListOldVersionsAction.prototype.createTable_ = function(versions, supportsCommitMessage, canPerformDiff) {
  var table = goog.dom.createDom('table', 'cmis-history-table');

  let headerRow = goog.dom.createDom('tr', 'table-header-row',
      [
        goog.dom.createDom('th', null, "Version"),
        goog.dom.createDom('th', null, "Creator")
      ]);
  if (canPerformDiff) {
    headerRow.insertBefore(goog.dom.createDom('th', {colspan: 2, style: "width:1%"}, "Diff"), headerRow.firstChild);
  }
  if(supportsCommitMessage) {
    let headerCommitMessage = goog.dom.createDom('th', null, 'Check-in Message');
    headerRow.appendChild(headerCommitMessage);
  }
  table.appendChild(goog.dom.createDom('thead', null, headerRow));

  let currentVersionIndex = versions.findIndex(v => v.isCurrentVersion === "true");
  let leftDiffIndex = Math.max(currentVersionIndex, 0);
  let rightDiffIndex = Math.min(leftDiffIndex + 1, versions.length - 1);
  for(let i = 0; i < versions.length; i++) {
    let version = versions[i];
    var versionUrl = version.url;

    var isThisCurrentVersion= version.isCurrentVersion === "true";

    var href = window.location.origin + window.location.pathname + "?url=" + encodeURIComponent(versionUrl);
    var versionLink = goog.dom.createDom('a', {
        className: 'cmis-old-version-link',
        href: isThisCurrentVersion ? '#' : href,
        target: '_blank'
      }, version.version);

    var versionTd = goog.dom.createDom('td', 'cmis-version', versionLink);
    var userTd = goog.dom.createDom('td', {class: 'cmis-user', title: version.author}, version.author);

    var trEl = goog.dom.createDom('tr', {
          className: isThisCurrentVersion ? 'current-version' : '',
          title: isThisCurrentVersion ? 'Opened document version' : ''
        }, versionTd, userTd);

    if (canPerformDiff) {
      var diffLeftTd = goog.dom.createDom('td', 'cmis-diff-left', goog.dom.createDom('input',
        {type: "radio", name: "cmis-diff-left", value: versionUrl, title: tr(msgs.LEFT_DIFF_DOC), checked: leftDiffIndex === i, "data-version": version.version, "data-currentversion": version.isCurrentVersion}));
      var diffRightTd = goog.dom.createDom('td', 'cmis-diff-right', goog.dom.createDom('input',
        {type: "radio", name: "cmis-diff-right", value: versionUrl, title: tr(msgs.RIGHT_DIFF_DOC), checked: rightDiffIndex === i, "data-version": version.version, "data-currentversion": version.isCurrentVersion}));
      trEl.insertBefore(diffRightTd, trEl.firstChild);
      trEl.insertBefore(diffLeftTd, diffRightTd);
    }

    if (supportsCommitMessage) {
      var processedCommitMessage = version.commitMessage.replace("/\n", "&#10;");
      var checkinMessageTd = goog.dom.createDom('td', 'cmis-checkin-message',
          goog.dom.createDom('span', {title: processedCommitMessage}, version.commitMessage));
      trEl.appendChild(checkinMessageTd);
    }

    table.appendChild(trEl);
  }

  return table;
};
