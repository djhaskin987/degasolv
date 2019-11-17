Roadmap
=======

This file outlines what we plan on doing to democratize dependency management.
It may or may not actually be implemented in the future, but represents a guide
for contributors and users alike as to the hopes and vision for the future of
the Degasolv developers.

Future Releases
---------------

- [ ] Tutorial-like help screens designed to keep people from needing to switch
      from docs to cli and back.

- [ ] Shortened versions of all subcommands, including documentation updates.

- [ ] Documentation and/or code on the topic of supporting the use case of
  different architectures of the same package using prioritized indexes of
  packages named the same with different contents.

- [ ] **Compile with GraalVM's ``native-image``**: Compile degasolv to machine
  code with GraalVM's ``native-image`` to decrease start-up times. This will likely
  coincide with upgrading to Clojure 1.11 because native-image doesn't work with
  Clojure 1.10.1 .

2.3.0
-----

Firefighters need tools that can apply in many situations; similarly, ops and
DevOps professionals, for whom we build this tool, need to a dependency
management tool that can get them out of dependency hell no matter what their
situation.

- [ ] Shortened versions of all subcommands, including documentation updates.
- [ ] The ability to slurp from JDBC URLs for indexes
     - [ ] An extension will be made to ensure that username and password
           can be specified along with a URL. Not all drivers support this
           and it is an important use case.
     - [ ] Generate-repo-index to support JDBC URLs
     - [ ] query-repo and resolve-locations to support JDBC URLs
     - [ ] If the database is empty or doesn't exist, it will be created on
       generate repo index or on index-add
- [ ] New subcommands: index-add, index-rm, to take away from and add to
  as in an installation/removal context
- [ ] New subcommand: resolve-dependents to find all dependents in an *index*
- [ ] USER GUIDES
  - [ ] How to use repositories as generic installation trackers
  - [ ] How to track dependencies between kubernetes services
  - [ ] How to track dependencies between cross-language builds and use this
    for that
      - [ ] Documentation and/or code on the topic of supporting the use case
        of different architectures of the same package using prioritized
        indexes of
  - [ ] How to use degasolv to manage a project installation for development
    purposes
  packages named the same with different contents.

