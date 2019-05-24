# CMIS Plugin for Oxygen XML Editor and Web Author

Integration plugin between any CMIS enabled server and Oxygen XML Editor and Web Author.

# Download
You can download the CMIS plugin for Oxygen XML Web Author as well as for the Oxygen XML Editor/Author from the [releases page](https://github.com/oxygenxml/oxygen-cmis-plugin/releases).

# Build

Alternatively, to build the project run:

```
mvn clean install
```

The plugin for Oxygen XML Editor/Author can be found in ` oxygen-cmis-workspace-access/target/oxygen-cmis-workspace-access-*-plugin.zip`.

The plugin for Oxygen XML Web Author can be found in `web-author-cmis-plugin/target/web-author-cmis-plugin-*-plugin.jar`.

# Install in Oxygen XML Web Author

To install the plugin in Oxygen XML Web Author you can use the Administration Page to upload the resulting JAR.

# Install the CMIS plugin in Oxygen XML Editor/Author (desktop)

This add-on is compatible with Oxygen XML Editor/Author/Developer version 18.1 or higher. 

To install the add-on, follow these instructions:

1. Go to **Help->Install new add-ons** to open an add-on selection dialog box.
2. Enter or paste https://raw.githubusercontent.com/oxygenxml/oxygen-cmis-plugin/master/oxygen-cmis-workspace-access/addon/addon.xml in the **Show add-ons from** field.
3. Select the **Oxygen CMIS plugin** add-on and click **Next**.
4. Select the **I accept all terms of the end user license agreement** option and click **Finish**.
5. Restart the application.

Result: A **CMIS Explorer** view will now be available in Oxygen XML Editor/Author/Developer. If it is not visible, go to **Window->Show View** and select **CMIS Explorer**. You can read some more about the plugin's capabilities: https://github.com/oxygenxml/oxygen-cmis-plugin/releases/download/v1-sa/oxygen-cmis-plugin.pdf


# Copyright and License

Copyright (c) 2018 Syncro Soft SRL. All rights reserved.

This project is licensed under [Apache License 2.0](https://github.com/oxygenxml/oxygen-cmis-plugin/blob/master/LICENSE)
