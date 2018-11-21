/**
 * The CMIS document status.
 *
 * @param checkedout the document's checkedout status.
 * @param locked whether the document is locked by another user.
 *
 * @constructor
 */
CmisStatus = function(checkedout, locked) {
  this.checkedout_ = checkedout;
  this.locked_ = locked;
};

/**
 * Setter for the current document checkedout.
 *
 * @param checkedout the new checkedout state.
 */
CmisStatus.prototype.setCheckedout = function(checkedout) {
  this.checkedout_ = checkedout;
};


/**
 * Getter for the current document status.
 */
CmisStatus.prototype.isCheckedout = function() {
  return this.checkedout_;
};

/**
 * Setter for the current document lock state.
 *
 * @param locked the new locked state.
 */
CmisStatus.prototype.setLocked = function(locked) {
  this.locked_ = locked;
};


/**
 * Whether the current document is locked by another user.
 */
CmisStatus.prototype.isLocked = function() {
  return this.locked_;
};

