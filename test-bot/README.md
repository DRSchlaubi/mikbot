# test-bot
This bot allows easy testing of plugins

# Usage
In order to test your plugins, please enter all plugins you want to test into a plugins.txt file like this.
```text
# Please add a list of gradle submodule paths, to add plugins
:core:gdpr
:core:redeploy-hook
:music
```
Plugins are gradle project paths, resolved from test-bot so the root project is `:` and the gdpr plugin is `:core:gdpr`

In order to start the bot please start the test-bot Launcher.kt DO NOT start the main bot Launcher.kt

If plugin loading fails run clean on the plugin which fails loading.
