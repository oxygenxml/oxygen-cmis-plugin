/**
 * The Cancel Checkout action.
 *
 * @param editor the editor.
 * @param {CmisStatus} status the document status.
 */
cancelCmisCheckOutAction = function(editor,status) {
  sync.actions.AbstractAction.call(this, '');
  this.editor_ = editor;
  this.status_ = status;
  this.dialog_ = null;
};
goog.inherits(cancelCmisCheckOutAction, sync.actions.AbstractAction);

cancelCmisCheckOutAction.prototype.getDisplayName = function() {
  return tr(msgs.CMIS_CANCEL_CHECK_OUT);
};

cancelCmisCheckOutAction.prototype.getSmallIcon = function(devicePixelRation) {
  return 'http://icons.iconarchive.com/icons/icons8/ios7/256/Very-Basic-Cancel-icon.png';
};

cancelCmisCheckOutAction.prototype.isEnabled = function() {
  return this.status_.isCheckedout();
};

cancelCmisCheckOutAction.prototype.actionPerformed = function(callback) {
  if (!this.dialog_) {
      var dialog = workspace.createDialog();
      dialog.setTitle(tr(msgs.CMIS_CANCEL_CHECK_OUT));
      dialog.setButtonConfiguration([
        {key: 'discard', caption: tr(msgs.CMIS_CANCEL_CHECK_OUT)},
        {key: 'cancel', caption: tr(msgs.CANCEL_)}
      ]);

      goog.dom.append(dialog.getElement(),
        tr(msgs.CMIS_LOSE_CHANGES_),
        goog.dom.createDom('br'),
        tr(msgs.CMIS_CANCEL_WARNING_)
      );
      this.dialog_ = dialog;
  }

  this.dialog_.show();
  this.dialog_.onSelect(goog.bind(function(key, e) {
      if (key === 'discard') {
          this.editor_.getActionsManager().invokeOperation(
              'com.oxygenxml.cmis.web.action.CmisCancelCheckOut', {
                  action: 'cancelCmisCheckout'
              }, callback);
        this.status_.setCheckedout(false);
      } else {
        goog.isFunction(callback) && callback();
      }
  }, this));
};
