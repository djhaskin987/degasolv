Roadmap
=======

This document is intended to help guide what features are upcoming in degasolv.
Some of these features are aspirational; that is, we want to implement them,
but may not be able to or may decide later that we didn't really want that
after all. Nevertheless, for what it's worth, here is the current roadmap.

2.1.0
-----
- **Catch and release**: Catch failed clause resolutions early when the failed
  clause's package name in a recursive call matches the present clause. This
  should significantly speed up clause resolution and make the idea of minimum
  version selection possible.
- **Minimum Version Selection**: Implement minimum version selection by
  introducing an option into resolve-locations that instructs degasolv which to
  choose: the smallest version available or the largest.

Future Features
---------------
- **Compile with GraalVM's ``native-image``**: Compile degasolv to machine
  code with GraalVM's ``native-image`` to decrease start-up times.
