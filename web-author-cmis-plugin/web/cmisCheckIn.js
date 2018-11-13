var CmisCheckInAction = function(editor) {
    sync.actions.AbstractAction.call(this, '');
    this.editor = editor;
};

CmisCheckInAction.prototype = Object.create(sync.actions.AbstractAction.prototype);
CmisCheckInAction.prototype.constructor = CmisCheckInAction;
CmisCheckInAction.prototype.getDisplayName = function() {
    return tr(msgs.CMIS_CHECK_IN);
};

CmisCheckInAction.prototype.getSmallIcon = function(devicePixelRation) {
    return 'https://static.thenounproject.com/png/796161-200.png';
};

CmisCheckInAction.prototype.isEnabled = function() {
    var isEnabled = false;
    if (cmisStatus && cmisStatus !== 'checkedout') {
        isEnabled = true;
    }
    return isEnabled;
};

CmisCheckInAction.prototype.actionPerformed = function(callback) {
    // Do not allow check in while the document is dirty.
    if (this.editor.isDirty()) {
      this.editor.problemReporter.showWarning(msgs.SAVE_CHANGES_BEFORE_CHECK_IN_);
      return;
    }
    var root = document.querySelector('[data-root="true"]');
    var noSupport = root.getAttribute('data-pseudoclass-nosupportfor');

    this.dialog = workspace.createDialog();
    this.dialog.setTitle(tr(msgs.CMIS_CHECK_IN));
    var dialogElement = this.dialog.getElement();

    if (noSupport !== 'true') {
        dialogElement.innerHTML = '';
        var checkInMessageSpan = document.createElement('span');
        checkInMessageSpan.textContent = tr(msgs.CHECK_IN_MESSAGE_);
        dialogElement.appendChild(checkInMessageSpan);
        dialogElement.appendChild(document.createElement('br'));
        this.dialog.setPreferredSize(300, 350);

        var input = document.createElement('textarea');

        input.setAttribute('style', 'margin:0px;width:255px;height:125px;resize:none;');
        input.setAttribute('type', 'text');
        input.setAttribute('id', 'input');

        dialogElement.appendChild(input);

    } else {
        this.dialog.setPreferredSize(250, 180);
    }

    dialogElement.appendChild(createVersionForm());

    this.dialog.show();

    var editor = this.editor;
    this.dialog.onSelect(goog.bind(function(key, e) {
            if (key === 'ok') {
                var root = document.querySelector('[data-root="true"]');
                var noSupport = root.getAttribute('data-pseudoclass-nosupportfor');

                var commitMessage;
                var verstate;

                if (noSupport !== 'true') {
                    commitMessage = dialogElement.querySelector('#input').value.replace(/["']/g, "");
                }

                if (dialogElement.querySelector('#radio1').checked) {
                    verstate = dialogElement.querySelector('#radio1').value;
                } else if (dialogElement.querySelector('#radio2').checked) {
                    verstate = dialogElement.querySelector('#radio2').value;
                }

                editor.getActionsManager().invokeOperation(
                    'com.oxygenxml.cmis.web.action.CmisCheckIn', {
                        action: 'cmisCheckin',
                        commit: commitMessage,
                        state: verstate
                    }, callback);

                cmisStatus = false;
            }

            this.dialog.dispose();
        },
        this));
};

/**
 * Create the major/minor version radio button form for the check-in dialog.
 * @returns {Element} The radio button form.
 */
function createVersionForm() {
    var createDom = goog.dom.createDom;

    var radio1 = createDom('input', {
      id: 'radio1',
      type: 'radio',
      name: 'state',
      value: 'major'
    });
    radio1.setAttribute('checked', '');

    var radio2 = createDom('input', {
      id: 'radio2',
      type: 'radio',
      name: 'state',
      value: 'minor'
    });

    return createDom('form', '',
      createDom('label', 'cmis-version-label',
        radio1,
        tr(msgs.MAJOR_VERSION_)
      ),
      createDom('label', 'cmis-version-label',
        radio2,
        tr(msgs.MINOR_VERSION_)
      )
    );
}
