(function () {
  
    var regExpOption = sync.options.PluginsOptions.getClientOption('restRootRegExp');
    var ROOT_REGEXP = regExpOption ? new RegExp(regExpOption) : null;
  
    goog.events.listen(workspace, sync.api.Workspace.EventType.BEFORE_EDITOR_LOADED, function(e) {
      console.log('Plugin loaded successfully');
	  
      var url = e.options.url;
      // If the URL has 'rest' protocol we use the rest protocol handler.
      if (url.match('cmis')) {
        var initialUrl = decodeURIComponent(sync.util.getURLParameter('url'));
        var prefix = 'cmis://';

        var limit = initialUrl.substring(prefix.length).indexOf('/') + prefix.length;
        var root = initialUrl.substring(0, limit);
        
        var test = initialUrl.replace(root, "");
        test = test.replace("/", "");
        test = test.substring(test.indexOf('/') + 1, test.length);

        var rootUrl = initialUrl.replace(test, "");

        console.log("--> " + test);

        console.log(initialUrl, rootUrl)
        // set the workspace UrlChooser
        workspace.setUrlChooser(new sync.api.FileBrowsingDialog({
          initialUrl: initialUrl,
          rootUrl: rootUrl
        }));

      }
    });
  
  })();
