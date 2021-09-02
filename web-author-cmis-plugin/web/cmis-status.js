/**
 * The CMIS document status.
 *
 * @param {Element} root The element to check for cmis status attributes.
 *
 * @constructor
 */
CmisStatus = function(root) {
  this.checkedout_ = root.getAttribute('data-pseudoclass-checkedout') === 'true';
  this.locked_ = root.getAttribute('data-pseudoclass-locked') === 'true';
  this.supportsCommitMessage_ = root.getAttribute('data-pseudoclass-supports-commit-message') === 'true';
  this.oldVersion_ = root.getAttribute('data-pseudoclass-oldversion') === 'true';
};

/**
 * Setter for the current document checkedout.
 *
 * @param checkedout the new checkedout state.
 */
CmisStatus.prototype.setCheckedout = function(checkedout) {
  this.checkedout_ = checkedout;
};

/** returns true if the version of the document is not the latest */
CmisStatus.prototype.isOldVersion = function() {
	return this.oldVersion_;
}

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


/**
 * @return {boolean} Whether the current server supports commit messages.
 */
CmisStatus.prototype.supportsCommitMessage = function() {
  return this.supportsCommitMessage_;
};

