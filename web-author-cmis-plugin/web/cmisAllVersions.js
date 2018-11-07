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
      'com.oxygenxml.cmis.web.action.CmisOldVersions', {
          action: 'listOldVersions'
      },
      function(err, data) {
          var jsonFile = JSON.parse(data);

          allVerDialog.setTitle(this.tr(msgs.ALL_VERSIONS_));

          if (allVerDialog) {
              var div = document.createElement('div');
              div.setAttribute('id', 'head');

              var createDom = goog.dom.createDom;
              var commitHeader;
              var versionHeader = createDom('div', {
                  id: 'versionDiv',
                  className: 'headtitle'
                },
                tr(msgs.VERSION_)
              );

            var userHeader = createDom('div', {
                id: 'userDiv',
                className: 'headtitle'
              },
              tr(msgs.MODIFIED_BY_)
            );

              if (!noSupport) {
                commitHeader = createDom('div', {
                    id: 'commitDiv',
                    className: 'headtitle'
                  },
                  tr(msgs.COMMIT_MESS_)
                );
              }
              // Add the headers to the header div.
              goog.dom.append(div,
                versionHeader,
                userHeader,
                commitHeader);

              var table = document.createElement('table');
              table.setAttribute('id', 'table');

              for (var key in jsonFile) {
                  if (key === 'filename') {
                      continue;
                  }

                  var tableRow = document.createElement('tr');
                  var versionTd = document.createElement('td');
                  goog.dom.dataset.set(versionTd, 'version', 'version');
                  versionTd.setAttribute('class', 'td');

                  var versionLink = document.createElement('a');
                  var value = jsonFile[key];

                  var commitTd = document.createElement('td');
                  goog.dom.dataset.set(commitTd, 'commit', 'commit');
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
                  goog.dom.dataset.set(userTd, 'user', 'user');
                  userTd.setAttribute('class', 'td');
                  userTd.textContent = value[2];

                  if (window.location.search.indexOf(oldVer) + 1) {
                      versionLink.setAttribute('href', '#');
                      goog.dom.classlist.add(tableRow, 'current-version');
                  }

                  if (noSupport) {
                      versionTd.style.width = '150px';
                      userTd.style.width = '60%';
                  }

                  versionTd.appendChild(versionLink);
                  tableRow.appendChild(versionTd);
                  tableRow.appendChild(userTd);

                  if (!noSupport) {
                      tableRow.appendChild(commitTd);
                  }

                  table.appendChild(tableRow);
              }

              document.getElementById("loader").remove();

              allVerDialog.getElement().appendChild(div);
              allVerDialog.getElement().appendChild(table);

              var versionBarWidth = document.querySelector('[data-version="version"]').offsetWidth;
              versionHeader.style.width = versionBarWidth + 'px';

              var userBarWidth = document.querySelector('[data-user="user"]').offsetWidth;
              userHeader.style.width = userBarWidth + 'px';

              if (!noSupport) {
                  var commitBarWidth = document.querySelector('[data-commit="commit"]').offsetWidth;
                  commitHeader.style.width = commitBarWidth + 'px';
              }

              allVerDialog.onSelect(function(e) {
                  allVerDialog.dispose();
              });
          }
      });

  callback();
};
