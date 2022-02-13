# eval

This plugin allows executing script code (currently only JavaScript via [Rhino](https://github.com/mozilla/rhino)).

## Configuration

The plugin needs a [hastebin](https://github.com/toptal/haste-server) compatible server for saving longer log outputs.
By default, it uses our hastebin instance https://pasta.with-rice.by.devs-from.asia. You can change that via
the `HASTE_SERVER` environment variable.
