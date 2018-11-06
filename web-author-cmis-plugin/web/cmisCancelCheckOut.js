var cancelCmisCheckOutAction = function(editor) {
  sync.actions.AbstractAction.call(this, '');
  this.editor = editor;
};

cancelCmisCheckOutAction.prototype = Object.create(sync.actions.AbstractAction.prototype);
cancelCmisCheckOutAction.prototype.constructor = cancelCmisCheckOutAction;
cancelCmisCheckOutAction.prototype.getDisplayName = function() {
  return tr(msgs.CANCEL_CHECK_OUT);
};

cancelCmisCheckOutAction.prototype.getSmallIcon = function(devicePixelRation) {
  return 'http://icons.iconarchive.com/icons/icons8/ios7/256/Very-Basic-Cancel-icon.png';
};

cancelCmisCheckOutAction.prototype.isEnabled = function() {
  var isEnabled = false;
  if (cmisStatus && cmisStatus !== 'checkedout') {
      isEnabled = true;
  }
  return isEnabled;
};

cancelCmisCheckOutAction.prototype.actionPerformed = function(callback) {
  if (!this.dialog) {
      this.dialog = workspace.createDialog();
      this.dialog.setTitle(tr(msgs.CANCEL_CHECK_OUT));
      this.dialog.setButtonConfiguration(sync.api.Dialog.ButtonConfiguration.YES_NO);
      this.dialog.setPreferredSize(250, 180);

      var warningDiv = document.createElement('div');
      warningDiv.textContent = tr(msgs.CANCEL_WARN_);

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
};
