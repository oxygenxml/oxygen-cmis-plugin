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
  this.editor_ = editor;
  this.status_ = status;
  this.dialog_ = null;
};
goog.inherits(CmisCheckOutAction, sync.actions.AbstractAction);

/** @override */
CmisCheckOutAction.prototype.getDisplayName = function() {
  return tr(msgs.CMIS_CHECK_OUT);
};

/** @override */
CmisCheckOutAction.prototype.getSmallIcon = function(devicePixelRation) {
  return '../plugin-resources/cmis/icons/CheckOut16.png';
};

/** @override */
CmisCheckOutAction.prototype.getLargeIcon = function(devicePixelRation) {
  return '../plugin-resources/cmis/icons/CheckOut16@2x.png';
};

/** @override */
CmisCheckOutAction.prototype.isEnabled = function() {
  return !this.status_.isCheckedout() && !this.status_.isLocked();
};

/** @override */
CmisCheckOutAction.prototype.actionPerformed = function(callback) {
  this.editor_.getActionsManager().invokeOperation(
      'com.oxygenxml.cmis.web.action.CmisCheckOut', {
          action: 'cmisCheckout'
      }, goog.bind(this.handleOperationResult_, this, callback));
};

/**
 * Handles the operation result.
 *
 * @param callback function to call when handling finished.
 * @param err error data.
 * @param data response data.
 *
 * @private
 */
CmisCheckOutAction.prototype.handleOperationResult_ = function(callback, err, data) {

  if (data === null) {
    goog.isFunction(callback) && callback();
    return;
  }
  var cause = JSON.parse(data);

  if (cause.error === 'denied') {
    this.status_.setCheckedout(false);

    if (!this.dialog_) {
      this.dialog_ = workspace.createDialog();
      this.dialog_.setTitle(tr(msgs.ERROR_TITLE_));
      this.dialog_.setButtonConfiguration(sync.api.Dialog.ButtonConfiguration.CANCEL);
      this.dialog_.setPreferredSize(350, 300);
    }

    var errorMessage = err ? err.message : cause.message;

    var dialogContent = this.dialog_.getElement();
    goog.dom.removeChildren(dialogContent);
    goog.dom.append(dialogContent,
      goog.dom.createDom('div', 'warningdiv', tr(msgs.ERROR_WARN_)),
      goog.dom.createDom('hr', {id: 'cmis-warnhr'}),
      goog.dom.createDom('div', {id: 'cmis-messdiv'}, errorMessage)
    );

    this.dialog_.show();
  } else {
    this.status_.setCheckedout(true);
  }
};
