/**
 * The CMIS checkout action.
 *
 * @param editor the editor.
 * @param {CmisStatus} status the document status.
 *
 * @constructor
 */
var CmisCheckOutAction = function(editor, status) {
  sync.actions.AbstractAction.call(this, '');
  this.editor = editor;
  this.status = status;
};

CmisCheckOutAction.prototype = Object.create(sync.actions.AbstractAction.prototype);
CmisCheckOutAction.prototype.getDisplayName = function() {
  return tr(msgs.CMIS_CHECK_OUT);
};

CmisCheckOutAction.prototype.getSmallIcon = function(devicePixelRation) {
  return 'https://static.thenounproject.com/png/978469-200.png';
};

CmisCheckOutAction.prototype.isEnabled = function() {
  return !this.status.isCheckedout() && !this.status.isLocked();
};

CmisCheckOutAction.prototype.actionPerformed = function(callback) {
  this.editor.getActionsManager().invokeOperation(
      'com.oxygenxml.cmis.web.action.CmisCheckOut', {
          action: 'cmisCheckout'
      },
      goog.bind(function(err, data) {

          if (data !== null) {
              var cause = JSON.parse(data);

              if (cause.error === 'denied') {
                this.status.setCheckedout(false);

                  if (!this.dialog) {
                      this.dialog = workspace.createDialog();
                      this.dialog.setTitle(tr(msgs.ERROR_TITLE_));
                      this.dialog.setButtonConfiguration(sync.api.Dialog.ButtonConfiguration.CANCEL);
                      this.dialog.setPreferredSize(350, 300);

                      var warningDiv = document.createElement('div');
                      warningDiv.setAttribute('class', 'warningdiv');
                      warningDiv.textContent = tr(msgs.ERROR_WARN_);

                      var messageDiv = document.createElement('div');
                      messageDiv.setAttribute('id', 'messdiv');

                      var errorMessage = cause.message;

                      if (err) {
                          errorMessage = err.message;
                      }

                      messageDiv.textContent = errorMessage;

                      var warnHr = document.createElement('hr');
                      warnHr.setAttribute('id', 'warnhr');

                      goog.dom.append(this.dialog.getElement(),
                        warningDiv,
                        warnHr,
                        messageDiv
                      );
                  }
                  this.dialog.show();
              } else {
                this.status.setCheckedout(true);
              }
          }
          callback();
      }, this));
};
