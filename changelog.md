- REQUIRES [CraterLib](https://www.curseforge.com/minecraft/mc-mods/craterlib) - [Modrinth](https://modrinth.com/mod/craterlib)
- [Online Config Editor](https://editor.firstdark.dev)
- [Documentation](https://sdlink.fdd-docs.com)
- This single jar works on 1.18.2-1.21.4

*Requires CraterLib 2.1.3 or newer*

**Bug Fixes**:

- Fixed Emojis not working with Emojiful
- Remove synced ranks when account is unverified - [#134](https://github.com/hypherionmc/sdlink/issues/134)
- Don't try to sync mentions to client when running on paper
- Fix database engine not regenerating database files when they are deleted while the server is running
- Fixed Forwarded messages not being relayed to discord
- Added checker to prevent bot from getting stuck in NotReady state

**New Features**:

- Implemented a basic message spam detector
- Added Optional verification, to allow people to use most (not all) features that required access control to be enabled, without it being enabled.
- Added basic spam checker to prevent spammed messages from being relayed to discord. Should help with command spams and ratelimits
- Allow Verify and Unverify commands to be used in DM, and verification codes can also be DM'ed to the bot

**Technical Changes**:

- Updated Discord JDA to Stable 5