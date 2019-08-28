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
  version selection possible. Supporting features:

- **Minimum Version Selection**: Implement minimum version selection as an
  option pack. Supporting features:
    - Version suggestion: when trying different candidates, if a matching id is
      in the  return of a candidate, put the suggestions in the list to loop
      through.
    - Skip unlikely candidates: Keep a set of problem ids different candidates
      are tried. If the current alternative's ID is not in that set, and if the
      dependencies are the same as the previous candidate, then skip the
      candidate; do not try it.
    - Minimum version preference: In generate repo index, add option to sort
      packages the other way.
    - Proper documentation surrounding order of encounter, that for example for
      subproc degasolv will honor the order of packages found in the repo index
      and that this enables things like MVS.

Future Features
---------------
- **Compile with GraalVM's ``native-image``**: Compile degasolv to machine
  code with GraalVM's ``native-image`` to decrease start-up times.
