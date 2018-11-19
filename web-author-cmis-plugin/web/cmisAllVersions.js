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
};

listOldVersionsAction.prototype.actionPerformed = function(callback) {
  var allVerDialog = this.dialog; //NOSONAR: local variable helps with uglifying.
  var noSupport = document.querySelector('[data-root="true"]').getAttribute('data-pseudoclass-nosupportfor');
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

  this.getVersions_(callback, allVerDialog, noSupport);
};

listOldVersionsAction.prototype.getVersions_ = function(callback, allVerDialog, noSupport) {
  this.editor.getActionsManager().invokeOperation(
      'com.oxygenxml.cmis.web.action.CmisOldVersions', {
          action: 'listOldVersions'
      },
      function(err, data) {
          // remove selection from document.
          document.activeElement.blur();
          allVerDialog.setTitle(this.tr(msgs.ALL_VERSIONS_));

          // Commit message column might not be available on some servers.
          var versionHeader = createTableHeader('versionDiv', tr(msgs.VERSION_));
          var userHeader = createTableHeader('userDiv', tr(msgs.MODIFIED_BY_));
          var commitHeader = (!noSupport) ? createTableHeader('commitDiv', tr(msgs.COMMIT_MESS_)) : '';

          allVerDialog.getElement().querySelector("#loader").remove();
          var jsonFile = JSON.parse(data);
          goog.dom.append(allVerDialog.getElement(),
            goog.dom.createDom('div', { id: 'head' },
              versionHeader,
              userHeader,
              commitHeader
            ),
            createTable(jsonFile, noSupport)
          );

          resizeHeaderWidth(versionHeader, 'version');
          resizeHeaderWidth(userHeader, 'user');
          resizeHeaderWidth(commitHeader, 'commit');

          // In case of older version, scroll it into view.
          var oldVersionSelected = document.querySelector('.current-version:not(:first-child)');
          if (oldVersionSelected) {
            oldVersionSelected.scrollIntoView(false);
          }
          allVerDialog.onSelect(function(e) {
              callback();
              allVerDialog.dispose();
          });
      });
};

function createTable(jsonFile, noSupport) {
  var table = goog.dom.createDom('table', { id: 'cmis-all-versions-table'});
  var isLatestVersionOpenedNow = location.href.indexOf('oldversion') === -1;

  for (var key in jsonFile) {
    if (key === 'filename') {
      continue;
    }

    var value = jsonFile[key];
    var versionUrlParamFromJson = value[0];

    var isThisVersionOpenedNow = window.location.search.indexOf(versionUrlParamFromJson) !== -1;
    var isThisVersionOld = versionUrlParamFromJson.indexOf('oldversion') !== -1;
    var isThisCurrentVersion = (isThisVersionOpenedNow && isThisVersionOld) || (isLatestVersionOpenedNow && !isThisVersionOld);

    var href = window.location.origin + window.location.pathname + versionUrlParamFromJson;
    var versionLink = goog.dom.createDom('a', {
        className: 'oldlink',
        href: isThisCurrentVersion ? '#' : href,
        target: '_blank'
      },
      key
    );

    var versionTd = createTableCell('version', versionLink);
    var userTd = createTableCell('user', value[2]);
    // If file is not versionable, do not create the commit cell.
    var commitTd = noSupport ? '' : createTableCell('commit', value[1]);

    if (noSupport) {
      versionTd.style.width = '150px';
      userTd.style.width = '60%';
    }

    table.appendChild(goog.dom.createDom('tr', {className: isThisCurrentVersion ? 'current-version' : ''},
      versionTd,
      userTd,
      commitTd
    ));
  }
  return table;
}

function createTableHeader(id, text) {
  return goog.dom.createDom('div', { id: id, className: 'headtitle' }, text);
}

function createTableCell(customAttribute, textContent) {
  var cell = goog.dom.createDom('td', 'td', textContent ? textContent : '');
  // Set some data attributes to set the column header widths later.
  goog.dom.dataset.set(cell, customAttribute, customAttribute);
  return cell;
}

/**
 * Resize the header cell accordingly to it's cells.
 *
 * @param header the header cell
 * @param attr the cell type.
 */
function resizeHeaderWidth(header, attr) {

  if (header) {
    var tableCell = document.querySelector('[data-' + attr + '="' + attr + '"]');
    var headerSectionWidth;
    if(tableCell) {
      headerSectionWidth = tableCell.offsetWidth;
    } else {
      // no entries in table.
      var headerContainer = header.parentElement;
      headerSectionWidth = headerContainer.offsetWidth / headerContainer.children.length;
    }
    header.style.width = headerSectionWidth + 'px';
  }
}
