var listOldVersionsAction = function(editor) {
  sync.actions.AbstractAction.call(this, '');
  this.editor = editor;
};

listOldVersionsAction.prototype = Object.create(sync.actions.AbstractAction.prototype);
listOldVersionsAction.prototype.constructor = listOldVersionsAction;
listOldVersionsAction.prototype.getDisplayName = function() {
  return tr(msgs.ALL_VERSIONS_);
};

listOldVersionsAction.prototype.getSmallIcon = function(devicePixelRation) {
  return 'http://icons.iconarchive.com/icons/icons8/windows-8/256/Data-View-Details-icon.png';
}

listOldVersionsAction.prototype.actionPerformed = function(callback) {
  var allVerDialog = this.dialog;

  var root = document.querySelector('[data-root="true"]');
  var noSupport = root.getAttribute('data-pseudoclass-nosupportfor');

  noSupport = (noSupport === 'true');

  allVerDialog = workspace.createDialog();
  allVerDialog.setTitle(tr(msgs.ALL_VERSIONS_));
  allVerDialog.setButtonConfiguration(sync.api.Dialog.ButtonConfiguration.CANCEL);

  if (!noSupport) {
      allVerDialog.setPreferredSize(750, 550);
      allVerDialog.setResizable(true);
  } else {
      allVerDialog.setPreferredSize(430, 500);
  }

  var loader = document.createElement('div');
  loader.setAttribute('id', 'loader');
  allVerDialog.getElement().appendChild(loader);

  allVerDialog.show();

  this.afterList_(callback, allVerDialog, noSupport);
};

listOldVersionsAction.prototype.afterList_ = function(callback, allVerDialog, noSupport) {
  this.editor.getActionsManager().invokeOperation(
      'com.oxygenxml.cmis.web.action.CmisActions', {
          action: 'listOldVersions'
      },
      function(err, data) {
          var jsonFile = JSON.parse(data);

          allVerDialog.setTitle(this.tr(msgs.ALL_VERSIONS_));

          if (allVerDialog) {
              var div = document.createElement('div');
              div.setAttribute('id', 'head');

              var childDiv = document.createElement('div');
              childDiv.setAttribute('id', 'versionDiv');
              childDiv.setAttribute('class', 'headtitle');
              childDiv.textContent = this.tr(msgs.VERSION_);
              div.appendChild(childDiv);

              var childDiv1 = document.createElement('div');
              childDiv1.setAttribute('id', 'userDiv');
              childDiv1.setAttribute('class', 'headtitle');
              childDiv1.textContent = this.tr(msgs.MODIFIED_BY_);
              div.appendChild(childDiv1);

              if (!noSupport) {
                  var childDiv2 = document.createElement('div');
                  childDiv2.setAttribute('id', 'commitDiv');
                  childDiv2.setAttribute('class', 'headtitle');
                  childDiv2.textContent = this.tr(msgs.COMMIT_MESS_);
                  div.appendChild(childDiv2);
              }

              var table = document.createElement('table');
              table.setAttribute('id', 'table');

              for (var key in jsonFile) {
                  if (key === 'filename') {
                      continue;
                  }

                  var tr = document.createElement('tr');
                  var versionTd = document.createElement('td');
                  versionTd.setAttribute('id', 'version');
                  versionTd.setAttribute('class', 'td');

                  var versionLink = document.createElement('a');
                  var value = jsonFile[key];

                  var commitTd = document.createElement('td');
                  commitTd.setAttribute('id', 'commit');
                  commitTd.setAttribute('class', 'td');

                  if (value[1] !== "" || value[1] !== null) {
                      commitTd.textContent = value[1];
                  }

                  var href = window.location.origin + window.location.pathname + value[0];

                  var oldVer = value[0];
                  oldVer = oldVer.substring(oldVer.lastIndexOf('='), oldVer.length);

                  versionLink.setAttribute('href', href);
                  versionLink.setAttribute('target', '_blank');
                  versionLink.setAttribute('class', 'oldlink');
                  versionLink.textContent = key;

                  var userTd = document.createElement('td');
                  userTd.setAttribute('id', 'user');
                  userTd.setAttribute('class', 'td');
                  userTd.textContent = value[2];

                  if (window.location.search.indexOf(oldVer) + 1) {
                      versionLink.setAttribute('href', '#');
                      versionLink.setAttribute('style', 'text-decoration:none;');

                      versionTd.style.backgroundColor = '#ededed';
                      commitTd.style.backgroundColor = '#ededed';
                      userTd.style.backgroundColor = '#ededed';
                  }

                  if (noSupport) {
                      versionTd.style.width = '150px';
                      userTd.style.width = '60%';
                  }

                  versionTd.appendChild(versionLink);
                  tr.appendChild(versionTd);
                  tr.appendChild(userTd);

                  if (!noSupport) {
                      tr.appendChild(commitTd);
                  }

                  table.appendChild(tr);
              }

              document.getElementById("loader").remove();

              allVerDialog.getElement().appendChild(div);
              allVerDialog.getElement().appendChild(table);

              var versionBarWidth = document.getElementById('version').offsetWidth;
              document.getElementById('versionDiv').style.width = versionBarWidth + 'px';

              var userBarWidth = document.getElementById('user').offsetWidth;
              document.getElementById('userDiv').style.width = userBarWidth + 'px';

              if (!noSupport) {
                  var commitBarWidth = document.getElementById('commit').offsetWidth;
                  document.getElementById('commitDiv').style.width = commitBarWidth + 'px';
              }

              allVerDialog.onSelect(function(e) {
                  allVerDialog.dispose();
              });
          }
      });

  callback();
}