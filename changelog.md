- REQUIRES [CraterLib](https://www.curseforge.com/minecraft/mc-mods/craterlib) - [Modrinth](https://modrinth.com/mod/craterlib)
- [Migration guide](https://sdlink.fdd-docs.com/migration/) for V2 users
- [Online Config Editor](https://editor.firstdark.dev)

*Requires CraterLib 2.1.2 or newer*

**Bug Fixes**:

- Fixed Emojis not working with Emojiful
- Remove synced ranks when account is unverified - [#134](https://github.com/hypherionmc/sdlink/issues/134)
- Don't try to sync mentions to client when running on paper
- Fix database engine not regenerating database files when they are deleted while the server is running
- Fixed Forwarded messages not being relayed to discord
- Added checker to prevent bot from getting stuck in NotReady state

**New Features**:

- Implemented a basic message spam detector

**Technical Changes**:

- Updated Discord JDA to Stable 5