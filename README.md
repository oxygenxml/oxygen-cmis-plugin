# CMIS Plugin for Oxygen XML Editor and Oxygen XML Web Author

Integration plugin between any CMIS enabled server and Oxygen XML Editor or Web Author.

# CMIS Plugin for Oxygen XML Web Author

## Download
You can download the CMIS plugin for Web Author from the [releases page](https://github.com/oxygenxml/oxygen-cmis-plugin/releases).


## Build

Alternatively, to build the project, run:

```
mvn clean install
```
The plugin for Oxygen XML Web Author can be found in `web-author-cmis-plugin/target/web-author-cmis-plugin-*-plugin.jar`.

## Install

To install the plugin in Oxygen XML Web Author, you can use the Administration Page to upload the resulting JAR.



# CMIS Plugin for Oxygen XML Editor

##Installation
The plugin is packed as an Oxygen add-on. To install it, follow these instructions:

1. Go to **Help > Install new add-ons...** to open an add-on selection dialog box.
2. Enter or paste `https://raw.githubusercontent.com/oxygenxml/oxygen-cmis-plugin/BRANCH_OXYGEN_RELEASE_21_1_1/oxygen-cmis-workspace-access/addon/addon.xml` in the **Show add-ons from** field.
3. Select the **Oxygen CMIS plugin** add-on and click **Next**.
4. Select the **I accept all terms of the end user license agreement** option and click **Finish**.
5. Restart the application.

## Offline Installation
To install the add-on offline, follow these instructions:
1. Go to the [Releases page](https://github.com/oxygenxml/oxygen-cmis-plugin/releases/latestt) and download `oxygen-cmis-workspace-access-1.0-SNAPSHOT-plugin.zip`
2. Unzip it inside `{oXygenInstallDir}/plugins`. Make sure you don't create any any intermediate folders. After unzipping the archive, the file system should look like this: `{oXygenInstallDir}/plugins/oxygen-cmis-workspace-access-1.0-SNAPSHOT-plugin`, and inside this folder there should be a `plugin.xml`file.



## Build

Alternatively, to build the project, run:

```
mvn clean install
```

The plugin for Oxygen XML Editor/Author can be found in ` oxygen-cmis-workspace-access/target/oxygen-cmis-workspace-access-*-plugin.zip`.


# Copyright and License

Copyright (c) 2018 Syncro Soft SRL. All rights reserved.

This project is licensed under [Apache License 2.0](https://github.com/oxygenxml/oxygen-cmis-plugin/blob/master/LICENSE)
