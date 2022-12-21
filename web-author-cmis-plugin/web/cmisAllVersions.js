/**
 * Action that displays the document's older versions in a dialog.
 *
 * @param editor the current editor.
 * @param {CmisStatus} status the document status.
 */
var listOldVersionsAction = function(editor, status) {
  sync.actions.AbstractAction.call(this, '');
  this.editor_ = editor;
  this.status_ = status;
  this.dialog_ = null;
};
goog.inherits(listOldVersionsAction, sync.actions.AbstractAction);

/** @override */
listOldVersionsAction.prototype.getDisplayName = function() {
  return tr(msgs.ALL_VERSIONS_);
};

/** @override */
listOldVersionsAction.prototype.getSmallIcon = function() {
  return sync.util.computeHdpiIcon('../plugin-resources/cmis/icons/ShowVersionHistory16.png');
};

/** @override */
listOldVersionsAction.prototype.actionPerformed = function(callback) {
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
listOldVersionsAction.prototype.getDialog_ = function(supportsCommitMessage) {
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
listOldVersionsAction.prototype.handleOperationResult_ = function(container, supportsCommitMessage, err, data) {
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
listOldVersionsAction.prototype.createTable_ = function(versions, supportsCommitMessage) {
  var table = goog.dom.createDom('table', { id: 'cmis-all-versions-table'});
  var isLatestVersionOpenedNow = location.href.indexOf('oldversion') === -1;

  let headerRow = goog.dom.createDom('tr', {class: 'table-header-row'},
      [goog.dom.createDom('td', {class: 'td-header'}, "Version"),
      goog.dom.createDom('td', {class: 'td-header'}, "Creator")]);
  if(supportsCommitMessage) {
    let headerCommitMessage = goog.dom.createDom('td', {class: 'td-header'}, 'Check-in Message');
    headerRow.appendChild(headerCommitMessage);
  }
  let headerTitles = goog.dom.createDom('thead', {class: 'table-header'}, headerRow);
  table.appendChild(headerTitles);

  for(var i = 0; i < versions.length; i++) {
    var version = versions[i];
    if (version.version === 'filename') {
      continue;
    }
    var versionUrl = version.url;

    var isThisVersionOpenedNow = window.location.search.indexOf(versionUrl) !== -1;
    var isThisVersionOld = versionUrl.indexOf('oldversion') !== -1;
    var isThisCurrentVersion = (isThisVersionOpenedNow && isThisVersionOld) || (isLatestVersionOpenedNow && !isThisVersionOld);

    var href = window.location.origin + window.location.pathname + versionUrl;
    var versionLink = goog.dom.createDom('a', {
        className: 'oldlink',
        href: isThisCurrentVersion ? '#' : href,
        target: '_blank'
      }, version.version);

    
    
    var versionTd = this.createTableCell_('version', versionLink);
    var userTd = this.createTableCell_('user', version.author);
    
    // Make tooltip multi-line
    var processedCommitMessage = version.commitMessage.replace("/\n", "&#10;");
    

    // If file is not versionable, do not create the commit cell.
    var commitTd = supportsCommitMessage ? this.createTableCell_('commit') : '';
    var divCommitMessage = goog.dom.createDom('span', {title: processedCommitMessage}, version.commitMessage);
    commitTd.appendChild(divCommitMessage);
    table.appendChild(goog.dom.createDom('tr', {className: isThisCurrentVersion ? 'current-version' : '', title: isThisCurrentVersion ? 'Opened document version' : ''},
      versionTd,
      userTd,
      commitTd
    ));
  }

  
  return table;
};

/**
 * Create cell element.
 *
 * @param customAttribute custom attribute.
 * @param textContent the cells text content.
 * @return {*} the cell.
 * @private
 */
listOldVersionsAction.prototype.createTableCell_ = function(customAttribute, textContent) {
  var cell = goog.dom.createDom('td', 'td', textContent ? textContent : '');
  // Set some data attributes to set the column header widths later.
  goog.dom.dataset.set(cell, customAttribute, customAttribute);
  return cell;
};
