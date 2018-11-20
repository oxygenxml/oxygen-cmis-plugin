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


CmisCheckInAction.prototype.getDisplayName = function() {
    return tr(msgs.CMIS_CHECK_IN);
};

CmisCheckInAction.prototype.getSmallIcon = function(devicePixelRation) {
    return 'https://static.thenounproject.com/png/796161-200.png';
};

CmisCheckInAction.prototype.isEnabled = function() {
    return this.status.isCheckedout();
};

CmisCheckInAction.prototype.actionPerformedInternal_ = function (callback) {
  if (this.editor.isDirty()) {
    // Should only happen if save action failed.
    this.editor.problemReporter.showWarning(tr(msgs.SAVE_CHANGES_BEFORE_CHECK_IN_));
    return;
  }
  var noSupport = document.querySelector('[data-root="true"]').getAttribute('data-pseudoclass-nosupportfor');

  this.dialog = workspace.createDialog();
  this.dialog.setTitle(tr(msgs.CMIS_CHECK_IN));
  var dialogElement = this.dialog.getElement();
  dialogElement.innerHTML = '';

  if (noSupport !== 'true') {
    goog.dom.append(dialogElement, createCommitMessageElements());
    this.dialog.setPreferredSize(300, 350);
  } else {
    this.dialog.setPreferredSize(250, 180);
  }

  dialogElement.appendChild(createVersionForm());

  this.dialog.show();

  var editor = this.editor;
  this.dialog.onSelect(goog.bind(function(key, e) {
      if (key === 'ok') {
        // If commit message is not supported, the textarea is not present.
        var commitMessageTextarea = dialogElement.querySelector('#cmis-commit-message');
        var commitMessage = commitMessageTextarea ? commitMessageTextarea.value.replace(/["']/g, "") : null;

        // Get the selected version type.
        var selectedVersionRadio = document.querySelector('.cmis-version-label input[type="radio"]:checked');
        editor.getActionsManager().invokeOperation(
          'com.oxygenxml.cmis.web.action.CmisCheckIn', {
            action: 'cmisCheckin',
            commit: commitMessage,
            state: selectedVersionRadio ? selectedVersionRadio.value : ''
          }, callback);
        this.status.setCheckedout(false);
      } else {
        goog.isFunction(callback) && callback()
      }
    },
    this));
};

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
function createVersionForm() {
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
}

/**
 * Create the commit message textarea.
 * @returns Array<Element> A list of commit message related elements to be appended to the dialog element.
 */
function createCommitMessageElements() {
  return [
    goog.dom.createDom('div', '', tr(msgs.CHECK_IN_MESSAGE_)),
    goog.dom.createDom('textarea', {
      id: 'cmis-commit-message',
      style: 'margin:0px; width:255px; height:125px; resize:none;',
      type: 'text'
    })
  ]
}
