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
CmisCheckOutAction.prototype.getSmallIcon = function() {
  return sync.util.computeHdpiIcon('../plugin-resources/cmis/icons/CheckOut16.png');
};

/** @override */
CmisCheckOutAction.prototype.isEnabled = function() {
  return !this.status_.isCheckedout() && !this.status_.isOldVersion();
};

/** @override */
CmisCheckOutAction.prototype.actionPerformed = function(callback) {
  this.editor_.getActionsManager().invokeOperation(
      'com.oxygenxml.cmis.web.action.CmisCheckOut',
      {},
      goog.bind(this.handleOperationResult_, this, callback));
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

    this.createErrorDialog_(350, 300);
    this.appendErrorMessageWithWarning_(err, cause);
    this.dialog_.show();
  } else if(cause.error === "checked_out_by"){
    this.status_.setCheckedout(false);
    
    this.createErrorDialog_(350, 200);  
	this.appendErrorMessage_(err, cause);
	this.dialog_.show();
  } else {
    this.status_.setCheckedout(true);
  }
};

/**
 * Creates an error dialog
 *
 * @param {number} width the width of the dialog
 * @param {number} height the height of the dialog
 *
 * @private
 */
CmisCheckOutAction.prototype.createErrorDialog_ = function(width, height) {
  if (!this.dialog_) {
    this.dialog_ = workspace.createDialog();
    this.dialog_.setTitle(tr(msgs.ERROR_TITLE_));
    this.dialog_.setButtonConfiguration(sync.api.Dialog.ButtonConfiguration.OK);
    this.dialog_.setPreferredSize(width, height);
  } else {
    goog.dom.removeChildren(this.dialog_.getElement());
  }
};

CmisCheckOutAction.prototype.appendErrorMessageWithWarning_ = function(err, cause) {
  this.appendWarning_();
  this.appendErrorMessage_(err, cause);
}

CmisCheckOutAction.prototype.appendErrorMessage_ = function(err, cause) {
  var errorMessage = err ? err.message : cause.message;
  goog.dom.append(this.dialog_.getElement(), 
    goog.dom.createDom('div', {id : 'cmis-messdiv'	}, errorMessage)
  );
}

CmisCheckOutAction.prototype.appendWarning_ = function() {
  goog.dom.append(this.dialog_.getElement(), 
    goog.dom.createDom('div','warningdiv', tr(msgs.ERROR_WARN_)), 
	goog.dom.createDom('hr', {id : 'cmis-warnhr'})
  );
};

