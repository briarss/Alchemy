# OdysseyTemplateMod
Example template mod to help with the initial creation of new server-sided Fabric mods for the Odyssey Network.

- Kotlin Based (KAPI)
- Config Management System
- Command Registration Framework (brigadier, maybe cloud eventually)
- Text Utilities
- Storage System
  - JSON File
  - SQLite Database
  - MongoDB
  - MySQL
- Economy Integrations
- Miscellaneous Utilities

## How to use
1. Clone this repository
2. Open the project in your IDE
3. Configure your mod properties in `gradle.properties`
4. Run the `hydrate` gradle task under `other` to hydrate all packages, files, and strings with your mod properties
