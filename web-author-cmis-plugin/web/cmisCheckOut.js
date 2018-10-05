var CmisCheckOutAction = function(editor) {
  sync.actions.AbstractAction.call(this, '');
  this.editor = editor;
};

CmisCheckOutAction.prototype = Object.create(sync.actions.AbstractAction.prototype);
CmisCheckOutAction.prototype.constructor = CmisCheckOutAction;
CmisCheckOutAction.prototype.getDisplayName = function() {
  return tr(msgs.CHECK_OUT_);
};

CmisCheckOutAction.prototype.getSmallIcon = function(devicePixelRation) {
  return 'https://static.thenounproject.com/png/978469-200.png';
}

CmisCheckOutAction.prototype.isEnabled = function() {
  var isEnabled = true;
  if (cmisStatus || cmisStatus === 'checkedout') {
      isEnabled = false;
  }
  return isEnabled;
}
CmisCheckOutAction.prototype.actionPerformed = function(callback) {
  cmisStatus = true;
  this.editor.getActionsManager().invokeOperation(
      'com.oxygenxml.cmis.web.action.CmisActions', {
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
                      warningDiv.innerHTML = tr(msgs.ERROR_WARN_);

                      var messageDiv = document.createElement('div');
                      messageDiv.setAttribute('id', 'messdiv');

                      var errorMessage = cause.message;

                      if (err) {
                          errorMessage = err.message;
                      }

                      messageDiv.innerHTML = errorMessage;

                      var warnHr = document.createElement('hr');
                      warnHr.setAttribute('id', 'warnhr');

                      this.dialog.getElement().appendChild(warningDiv);
                      this.dialog.getElement().appendChild(warnHr);
                      this.dialog.getElement().appendChild(messageDiv);
                  }
                  this.dialog.show();
              } else {
                  cmisStatus = true;
              }
          }
          callback();
      });
};

//------------------- Cmis cancel check-out action. -----------------------
var cancelCmisCheckOutAction = function(editor) {
  sync.actions.AbstractAction.call(this, '');
  this.editor = editor;
};

cancelCmisCheckOutAction.prototype = Object.create(sync.actions.AbstractAction.prototype);
cancelCmisCheckOutAction.prototype.constructor = cancelCmisCheckOutAction;
cancelCmisCheckOutAction.prototype.getDisplayName = function() {
  return tr(msgs.CANCEL_CHECK_OUT_);
};

// PROTOTYPE!!!!
cancelCmisCheckOutAction.prototype.getSmallIcon = function(devicePixelRation) {
  return 'http://icons.iconarchive.com/icons/icons8/ios7/256/Very-Basic-Cancel-icon.png';
}

cancelCmisCheckOutAction.prototype.isEnabled = function() {
  var isEnabled = false;
  if (cmisStatus && cmisStatus !== 'checkedout') {
      isEnabled = true;
  }
  return isEnabled;
}

cancelCmisCheckOutAction.prototype.actionPerformed = function(callback) {
  if (!this.dialog) {
      this.dialog = workspace.createDialog();
      this.dialog.setTitle(tr(msgs.CANCEL_CHECK_OUT_));
      this.dialog.setButtonConfiguration(sync.api.Dialog.ButtonConfiguration.YES_NO);
      this.dialog.setPreferredSize(250, 180);

      var warningDiv = document.createElement('div');
      warningDiv.setAttribute('class', 'warningdiv');
      warningDiv.innerHTML = tr(msgs.CANCEL_WARN_);

      this.dialog.getElement().appendChild(warningDiv);
  }

  this.dialog.show();

  this.dialog.onSelect(goog.bind(function(key, e) {
      if (key === 'yes') {
          this.editor.getActionsManager().invokeOperation(
              'com.oxygenxml.cmis.web.action.CmisActions', {
                  action: 'cancelCmisCheckout'
              }, callback);

          cmisStatus = false;
      }
  }, this));
}