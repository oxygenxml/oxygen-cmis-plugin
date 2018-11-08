var CmisCheckOutAction = function(editor) {
  sync.actions.AbstractAction.call(this, '');
  this.editor = editor;
};

CmisCheckOutAction.prototype = Object.create(sync.actions.AbstractAction.prototype);
CmisCheckOutAction.prototype.constructor = CmisCheckOutAction;
CmisCheckOutAction.prototype.getDisplayName = function() {
  return tr(msgs.CMIS_CHECK_OUT);
};

CmisCheckOutAction.prototype.getSmallIcon = function(devicePixelRation) {
  return 'https://static.thenounproject.com/png/978469-200.png';
};

CmisCheckOutAction.prototype.isEnabled = function() {
  var isEnabled = true;
  if (cmisStatus || cmisStatus === 'checkedout') {
      isEnabled = false;
  }
  return isEnabled;
};

CmisCheckOutAction.prototype.actionPerformed = function(callback) {
  cmisStatus = true;
  this.editor.getActionsManager().invokeOperation(
      'com.oxygenxml.cmis.web.action.CmisCheckOut', {
          action: 'cmisCheckout'
      },
      function(err, data) {

          if (data !== null) {
              var cause = JSON.parse(data);

              if (cause.error === 'denied') {
                  cmisStatus = false;

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
                  cmisStatus = true;
              }
          }
          callback();
      });
};
