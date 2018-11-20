/**
 * The CMIS Checkin Action.
 *
 * @param editor the editor.
 * @param {CmisStatus} status the document status.
 *
 * @constructor
 */
CmisCheckInAction = function(editor, status) {
  sync.actions.AbstractAction.call(this, '');
  this.editor = editor;
  this.status = status;
};
goog.inherits(CmisCheckInAction, sync.actions.AbstractAction);

/** @override */
CmisCheckInAction.prototype.getDisplayName = function() {
    return tr(msgs.CMIS_CHECK_IN);
};

/** @override */
CmisCheckInAction.prototype.getSmallIcon = function(devicePixelRation) {
    return 'https://static.thenounproject.com/png/796161-200.png';
};

/** @override */
CmisCheckInAction.prototype.isEnabled = function() {
    return this.status.isCheckedout();
};

/**
 * Check in the document.
 *
 * @param callback callback function.
 *
 * @private
 */
CmisCheckInAction.prototype.actionPerformedInternal_ = function (callback) {
  if (this.editor.isDirty()) {
    // Should only happen if save action failed.
    this.editor.problemReporter.showWarning(tr(msgs.SAVE_CHANGES_BEFORE_CHECK_IN_));
    return;
  }
  // supports Private Working Copy and Commit Message
  var supportsPWC = document.querySelector('[data-root="true"]').getAttribute('data-pseudoclass-nosupportfor') !== 'true';

  var dialog = this.getDialog(supportsPWC);
  dialog.show();
  dialog.onSelect(goog.bind(this.handleDialogSelect, this, callback));
};

/**
 * Create the checkin dialog.
 *
 * @param supportsPWC if the server supports Private Working Copy and commit messages.
 *
 * @return {*} the checkin dialog.
 */
CmisCheckInAction.prototype.getDialog = function(supportsPWC) {
  var dialog = this.dialog;
  if(!dialog) {
    dialog = workspace.createDialog();
    dialog.setTitle(tr(msgs.CMIS_CHECK_IN));
    this.dialog = dialog;
  }
  var dialogElement = dialog.getElement();
  dialogElement.innerHTML = '';

  if (supportsPWC) {
    goog.dom.append(dialogElement, this.createCommitMessageElements());
    dialog.setPreferredSize(300, 350);
  } else {
    dialog.setPreferredSize(250, 180);
  }
  dialogElement.appendChild(this.createVersionForm());

  return dialog;
};

/**
 * Handles the dialog select.
 *
 * @param callback callback function.
 * @param key the button key.
 * @param e the event.
 */
CmisCheckInAction.prototype.handleDialogSelect = function(callback, key, e) {
  if (key === 'ok') {
    var dialogElement = this.dialog.getElement();
    // If commit message is not supported, the textarea is not present.
    var commitMessageTextarea = dialogElement.querySelector('#cmis-commit-message');
    var commitMessage = commitMessageTextarea ? commitMessageTextarea.value.replace(/["']/g, "") : null;

    // Get the selected version type.
    var selectedVersionRadio = dialogElement.querySelector('.cmis-version-label input[type="radio"]:checked');
    this.editor.getActionsManager().invokeOperation(
      'com.oxygenxml.cmis.web.action.CmisCheckIn', {
        action: 'cmisCheckin',
        commit: commitMessage,
        state: selectedVersionRadio ? selectedVersionRadio.value : ''
      }, callback);
    this.status.setCheckedout(false);
  } else {
    goog.isFunction(callback) && callback();
  }
};

/** @override */
CmisCheckInAction.prototype.actionPerformed = function(callback) {
  // Save document immediately if it is dirty.
  if (this.editor.isDirty()) {
    var saveAction = this.editor.getActionsManager().getActionById('Author/Save');
    saveAction.actionPerformed(goog.bind(this.actionPerformedInternal_, this, callback));
  } else {
    this.actionPerformedInternal_(callback);
  }
};

/**
 * Create the major/minor version radio button form for the check-in dialog.
 * @returns {Element} The radio button form.
 */
CmisCheckInAction.prototype.createVersionForm = function() {
  var createDom = goog.dom.createDom;
  var majorVersionRadio = createDom('input', { type: 'radio', name: 'state', value: 'major' });
  majorVersionRadio.setAttribute('checked', '');
  var minorVersionRadio = createDom('input', { type: 'radio', name: 'state', value: 'minor' });

  return createDom('form', '',
    createDom('label', 'cmis-version-label',
      majorVersionRadio,
      tr(msgs.MAJOR_VERSION_)
    ),
    createDom('label', 'cmis-version-label',
      minorVersionRadio,
      tr(msgs.MINOR_VERSION_)
    )
  );
};

/**
 * Create the commit message textarea.
 * @returns Array<Element> A list of commit message related elements to be appended to the dialog element.
 */
CmisCheckInAction.prototype.createCommitMessageElements = function() {
  return [
    goog.dom.createDom('div', '', tr(msgs.CHECK_IN_MESSAGE_)),
    goog.dom.createDom('textarea', {
      id: 'cmis-commit-message',
      style: 'margin:0px; width:255px; height:125px; resize:none;',
      type: 'text'
    })
  ]
};
