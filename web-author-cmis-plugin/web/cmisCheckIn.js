var CmisCheckInAction = function(editor) {
    sync.actions.AbstractAction.call(this, '');
    this.editor = editor;
};

CmisCheckInAction.prototype = Object.create(sync.actions.AbstractAction.prototype);
CmisCheckInAction.prototype.constructor = CmisCheckInAction;
CmisCheckInAction.prototype.getDisplayName = function() {
    return tr(msgs.CHECK_IN_);
};

CmisCheckInAction.prototype.getSmallIcon = function(devicePixelRation) {
    return 'https://static.thenounproject.com/png/796161-200.png';
}

CmisCheckInAction.prototype.isEnabled = function() {
    var isEnabled = false;
    if (cmisStatus && cmisStatus !== 'checkedout') {
        isEnabled = true;
    }
    return isEnabled;
}

CmisCheckInAction.prototype.actionPerformed = function(callback) {
    if (!this.dialog) {
        var dialogElement = this.dialog.getElement();
        var root = document.querySelector('[data-root="true"]');
        var noSupport = root.getAttribute('data-pseudoclass-nosupportfor');

        this.dialog = workspace.createDialog();
        this.dialog.setTitle(tr(msgs.CHECK_IN_));

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
            this.dialog.setPreferredSize(200, 180);
        }

        var form = document.createElement('form');
        var text1 = document.createElement('label');
        var text2 = document.createElement('label');

        form.setAttribute('action', '');
        radioButtonsCreator(form, text1, text2);

        dialogElement.appendChild(form);
    }

    this.dialog.show();

    var editor = this.editor;
    this.dialog.onSelect(goog.bind(function(key, e) {
            if (key === 'ok') {
                var root = document.querySelector('[data-root="true"]');
                var noSupport = root.getAttribute('data-pseudoclass-nosupportfor');

                var commitMessage;
                var verstate;

                if (noSupport !== 'true') {
                    commitMessage = this.dialog.getElement().querySelector('#input').value.replace(/["']/g, "");
                }

                if (this.dialog.getElement().querySelector('#radio1').checked) {
                    verstate = this.dialog.getElement().querySelector('#radio1').value;
                } else if (this.dialog.getElement().querySelector('#radio2').checked) {
                    verstate = this.dialog.getElement().querySelector('#radio2').value;
                }

                editor.getActionsManager().invokeOperation(
                    'com.oxygenxml.cmis.web.action.CmisActions', {
                        action: 'cmisCheckin',
                        commit: commitMessage,
                        state: verstate
                    }, callback);

                cmisStatus = false;
            }
        },
        this));
};

function radioButtonsCreator(form, text1, text2) {
    var radio1 = document.createElement('input');

    radio1.setAttribute('type', 'radio');
    radio1.setAttribute('name', 'state');
    radio1.setAttribute('id', 'radio1');
    radio1.setAttribute('value', 'major');
    radio1.setAttribute('checked', '');
    form.appendChild(radio1);

    text1.textContent = tr(msgs.MAJOR_VERSION_);
    text1.appendChild(document.createElement('br'));
    form.appendChild(text1);

    var radio2 = document.createElement('input');

    radio2.setAttribute('type', 'radio');
    radio2.setAttribute('name', 'state');
    radio2.setAttribute('id', 'radio2');
    radio2.setAttribute('value', 'minor');
    form.appendChild(radio2);

    text2.textContent = tr(msgs.MINOR_VERSION_);
    form.appendChild(text2);
}