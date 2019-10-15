/**
 * Action that displays the document's older versions in a dialog.
 *
 * @param editor the current editor.
 * @param {CmisStatus} status the document status.
 */
var CompareBetweenVersionsAction = function(editor, status) {
  sync.actions.AbstractAction.call(this, '');
  this.editor_ = editor;
  this.status_ = status;
  this.dialog_ = null;


  this.leftVersionSelectedIndex_ = null;
  this.rightVersionSelectedIndex_ = null;
};
goog.inherits(CompareBetweenVersionsAction, sync.actions.AbstractAction);

/** @override */
CompareBetweenVersionsAction.prototype.getDisplayName = function() {
  return "Compare between versions...";
};

/** @override */
CompareBetweenVersionsAction.prototype.getSmallIcon = function() {
  return sync.util.computeHdpiIcon('../plugin-resources/cmis/icons/ShowVersionHistory16.png');
};

CompareBetweenVersionsAction.prototype.isEnabled = function () {
  return sync.diff && sync.diff.CompareWithDialog;
};

/** @override */
CompareBetweenVersionsAction.prototype.actionPerformed = function(callback) {
  // Check if the server supports Commit Message.
  // todo: (WA-2472/WA-2709) disabled the commit message column until the table is reworked.
  var supportsCommitMessage = false/*this.status_.supportsCommitMessage()*/;

  var allVerDialog = this.getDialog_(supportsCommitMessage);
  allVerDialog.show();
  allVerDialog.onSelect(function(e) {
    callback();
  });

  this.editor_.getActionsManager().invokeOperation(
    'com.oxygenxml.cmis.web.action.CmisOldVersions', {
      action: 'listOldVersions'
    }, goog.bind(this.onHistoryReceived_, this, allVerDialog.getElement(), supportsCommitMessage));
};

/**
 * Creates the versions display dialog.
 *
 * @param supportsCommitMessage if the server supports Private Working Copy and commit messages.
 * @return the versions display dialog.
 * @private
 */
CompareBetweenVersionsAction.prototype.getDialog_ = function(supportsCommitMessage) {
  this.diffShowing_ = false;

  var allVerDialog = this.dialog_;
  if(!allVerDialog) {
    allVerDialog = workspace.createDialog();
    allVerDialog.setTitle("Choose versions");
    allVerDialog.setButtonConfiguration([
      {key: 'diff', caption: "Compare"},
      {key: 'cancel', caption: "Cancel"}]);
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

  allVerDialog.onSelect(goog.bind(this.onButtonSelected_, this));

  return allVerDialog;
};

CompareBetweenVersionsAction.prototype.onButtonSelected_ = function(buttonKey) {
  this.dialog_.hide();
  console.log("onButtonSelected_ ", this.leftVersionSelectedIndex_, this.rightVersionSelectedIndex_);

  if (!this.diffShowing_ && buttonKey === "diff") {
    var leftVersion = this.versions_[this.leftVersionSelectedIndex_];
    var rightVersion = this.versions_[this.rightVersionSelectedIndex_];
    var leftLabel = leftVersion.version;
    var leftUrl = decodeURIComponent(leftVersion.url.substring("?url=".length));
    var rightLabel = rightVersion.version;
    var rightUrl = decodeURIComponent(rightVersion.url.substring("?url=".length));
    this.showDiff_(leftLabel, leftUrl, rightLabel, rightUrl);
  }
};

CompareBetweenVersionsAction.prototype.showDiff_ = function(leftLabel, leftUrl, rightLabel, rightUrl) {
  this.diffShowing_ = true;
  var compareWithDialog = new sync.diff.CompareWithDialog(workspace.currentEditor, leftLabel, rightLabel);
  compareWithDialog.show(leftUrl, rightUrl);
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
CompareBetweenVersionsAction.prototype.onHistoryReceived_ = function(container, supportsCommitMessage, err, data) {
  document.activeElement.blur();

  var leftdocHeader = this.createHeaderCell_("Left");
  var rightdocHeader = this.createHeaderCell_("Right");
  var versionHeader = this.createHeaderCell_(tr(msgs.VERSION_));
  var userHeader = this.createHeaderCell_(tr(msgs.MODIFIED_BY_));
  var commitHeader = supportsCommitMessage ? this.createHeaderCell_(tr(msgs.COMMIT_MESS_)) : '';

  goog.dom.removeNode(container.querySelector("#cmis-loader"));

  this.versions_ = JSON.parse(data);
  goog.dom.append(container,
    goog.dom.createDom('div', { id: 'cmis-head' },
      leftdocHeader,
      rightdocHeader,
      versionHeader,
      userHeader,
      commitHeader
    ),
    this.createTable_(this.versions_, supportsCommitMessage)
  );


  this.resizeHeaderWidth_(leftdocHeader, 'leftdoc');
  this.resizeHeaderWidth_(rightdocHeader, 'rightdoc');
  this.resizeHeaderWidth_(versionHeader, 'version');
  this.resizeHeaderWidth_(userHeader, 'user');
  this.resizeHeaderWidth_(commitHeader, 'commit');

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
 * @return {{commitMessage: String, version: String, url: String}} the HTML table.
 * @private
 */
CompareBetweenVersionsAction.prototype.createTable_ = function(versions, supportsCommitMessage) {
  var table = goog.dom.createDom('table', { id: 'cmis-all-versions-table'});
  var isLatestVersionOpenedNow = location.href.indexOf('oldversion') === -1;

  console.log("versions: ", versions);

  var secondDocSelected = false;
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
      href: href,
      target: '_blank'
    }, version.version);

    var cD = goog.dom.createDom;
    var selectFirstDoc = isThisCurrentVersion;
    var firstDocRadio = cD('input', {
      className: 'first-doc',
      type: 'radio',
      checked: selectFirstDoc
    });
    if (selectFirstDoc) {
      this.leftVersionSelectedIndex_ = i;
    }

    var selectSecondDoc = (!secondDocSelected && !isThisCurrentVersion) || versions.length === 1;
    secondDocSelected = secondDocSelected || selectSecondDoc;
    var secondDocRadio = cD('input', {
      className: 'second-doc',
      type: 'radio',
      checked: selectSecondDoc
    });
    if (selectSecondDoc) {
      this.rightVersionSelectedIndex_ = i;
    }

    var firstDocRadioTd = this.createTableCell_('leftdoc', firstDocRadio);
    var secondDocRadioTd = this.createTableCell_('rightdoc', secondDocRadio);

    goog.events.listen(
      firstDocRadioTd,
      goog.events.EventType.CLICK,
      goog.bind(this.onSelectDocRadioSelected_, this, true, i)
    );
    goog.events.listen(
      secondDocRadioTd,
      goog.events.EventType.CLICK,
      goog.bind(this.onSelectDocRadioSelected_, this, false, i)
    );

    var versionTd = this.createTableCell_('version', versionLink);
    if (isThisCurrentVersion) {
      versionTd.appendChild(cD("span", "opened-label", "(opened)"));
    }

    var userTd = this.createTableCell_('user', version.author);

    // If file is not versionable, do not create the commit cell.
    var commitTd = supportsCommitMessage ? this.createTableCell_('commit', version.commitMessage) : '';


    // Fill the dialog with only version/user columns.
    if (!supportsCommitMessage) {
      versionTd.style.width = '150px';
      userTd.style.width = '60%';
    }

    table.appendChild(goog.dom.createDom('tr', null,
      firstDocRadioTd,
      secondDocRadioTd,
      versionTd,
      userTd,
      commitTd
    ));
  }
  return table;
};

/**
 * @private
 */
CompareBetweenVersionsAction.prototype.onSelectDocRadioSelected_ = function(isFirstRow, index, e) {
  console.log("onSelectDocRadioSelected_");
  if (isFirstRow) {
    this.leftVersionSelectedIndex_ = index;
  } else {
    this.rightVersionSelectedIndex_ = index;
  }

  var selectedInput;
  if (!e.target.checked) {
    selectedInput = e.target.firstElementChild;
    selectedInput.checked = true;
  } else {
    selectedInput = e.target;
  }

  var table = selectedInput.parentNode.parentNode.parentNode;
  var checkedInputs;
  if(isFirstRow) {
    checkedInputs = table.querySelectorAll("input.first-doc:checked");
  } else {
    checkedInputs = table.querySelectorAll("input.second-doc:checked");
  }

  for (var i = 0; i < checkedInputs.length; i++) {
    if (checkedInputs[i] !== selectedInput) {
      checkedInputs[i].checked = false;
    }
  }
};

/**
 * Creates a table header cell.
 *
 * @param text the cells text content.
 * @return {*} the header cell element.
 * @private
 */
CompareBetweenVersionsAction.prototype.createHeaderCell_ = function(text) {
  return goog.dom.createDom('div', 'headtitle', text);
};

/**
 * Create cell element.
 *
 * @param customAttribute custom attribute.
 * @param textContent the cells text content.
 * @return {*} the cell.
 * @private
 */
CompareBetweenVersionsAction.prototype.createTableCell_ = function(customAttribute, textContent) {
  var cell = goog.dom.createDom('td', 'td', textContent ? textContent : '');
  // Set some data attributes to set the column header widths later.
  goog.dom.dataset.set(cell, customAttribute, customAttribute);
  return cell;
};

/**
 * Resize the header cell accordingly to it's cells.
 *
 * @param header the header cell
 * @param attr the cell type.
 * @private
 */
CompareBetweenVersionsAction.prototype.resizeHeaderWidth_ = function(header, attr) {
  if (header) {
    var tableCell = document.querySelector('[data-' + attr + '="' + attr + '"]');
    var headerSectionWidth;
    if(tableCell) {
      headerSectionWidth = tableCell.offsetWidth;
    } else {
      // no entries in table.
      var headerContainer = header.parentElement;
      headerSectionWidth = headerContainer.offsetWidth / headerContainer.children.length;
    }
    header.style.width = headerSectionWidth + 'px';
  }
};
