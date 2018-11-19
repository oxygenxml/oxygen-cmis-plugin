/**
 * The Cancel Checkout action.
 *
 * @param editor the editor.
 * @param {CmisStatus} status the document status.
 */
cancelCmisCheckOutAction = function(editor,status) {
  sync.actions.AbstractAction.call(this, '');
  this.editor = editor;
  this.status = status;
};

cancelCmisCheckOutAction.prototype = Object.create(sync.actions.AbstractAction.prototype);
cancelCmisCheckOutAction.prototype.constructor = cancelCmisCheckOutAction;

cancelCmisCheckOutAction.prototype.getDisplayName = function() {
  return tr(msgs.CMIS_CANCEL_CHECK_OUT);
};

cancelCmisCheckOutAction.prototype.getSmallIcon = function(devicePixelRation) {
  return 'http://icons.iconarchive.com/icons/icons8/ios7/256/Very-Basic-Cancel-icon.png';
};

cancelCmisCheckOutAction.prototype.isEnabled = function() {
  return this.status.isCheckedout();
};

cancelCmisCheckOutAction.prototype.actionPerformed = function(callback) {
  if (!this.dialog) {
      this.dialog = workspace.createDialog();
      this.dialog.setTitle(tr(msgs.CMIS_CANCEL_CHECK_OUT));
      this.dialog.setButtonConfiguration([
        {key: 'discard', caption: tr(msgs.CMIS_CANCEL_CHECK_OUT)},
        {key: 'cancel', caption: tr(msgs.CANCEL_)}
      ]);

      var warningDiv = goog.dom.createDom('div', '',
        tr(msgs.CMIS_LOSE_CHANGES_),
        goog.dom.createDom('br'),
        tr(msgs.CMIS_CANCEL_WARNING_));
      this.dialog.getElement().appendChild(warningDiv);
  }

  this.dialog.show();
  this.dialog.onSelect(goog.bind(function(key, e) {
      if (key === 'discard') {
          this.editor.getActionsManager().invokeOperation(
              'com.oxygenxml.cmis.web.action.CmisCancelCheckOut', {
                  action: 'cancelCmisCheckout'
              }, callback);
        this.status.setCheckedout(false);
      } else {
        goog.isFunction(callback) && callback();
      }
  }, this));
};
