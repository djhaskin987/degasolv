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

Package Installation Manager
++++++++++++++++++++++++++++

This is what has been missing from Degasolv for a long time: the notion of an
installation. We've got dependency management down; now we need to get into
installation management.

In keeping with Degasolv's original vision, we will *not* make specific what
is actually being installed or maintained. This way, Degasolv can be kept
as a neutral, generic tool that can apply in many situations. Firefighters
need tools that can apply in many situations; similarly, ops and DevOps
professionals, for whom we build this tool, need to a dependency management
tool that can get them out of dependency hell no matter what their situation.

Therefore, we focus on two explicit dimensions of installation management, the
"hard parts", and leave the implementation details like unpacking archives and
writing to file or key-value stores to the DevOps professional.

These are:
1. Resource management
2. Package installation database

An Installation Database
++++++++++++++++++++++++

A new database that represents a particular package "installation". This could
be used for a file installation, a kubernetes/windows/whatever installation.

package installation will house:
- Site-wide configuration
- Packages that have been installed and the relationships between them

This database will allow us to track and manage:
- package installations
- package removals
- package verification

- [ ] A new global option, ``--installation``, that would take a JDBC
      connection string. If the option ``--ignore-installation`` were also
      present any functionality would be turned off, including ignoring the
      installation for configuration items. An optional username and password
      for the connection, ``--installation-username``, and ``--installation-password``,
      would be allowed. Last wins. 

- [ ] If the ``--installation`` option is present, the ``resolve-locations``
      subcommand would take the installation into account in its resolution as
      present packages.

- [ ] If the ``--enable-installation-only`` is present, the ``resolve-locations``
      subcommand would take the installation into account as its sole index.
      Any subsequent options given for other indices will be recorded, but
      ignored.  These will be turned "back on" if the option
      ``--disable-installation-only`` is encountered.

- [ ] If the ``--enable-from-installation`` is present, the ``resolve-locations``
      subcommand would take the installation into account as one of its
      indexes in "the usual order", as if it were another specified index
      on the commandline. Its order will be reset if it is encountered again.
      It can be turned off with ``--disable-from-installation``. Last wins.

- [ ] A new subcommand, ``init-installation``, would require that
  ``--installation`` were given, optional username and password for the
  connection. It would record present configuration (that is, configuration as
  observed by init-installation), and set up the necessary tables for future
  use.

- [ ] A new subcommand, ``resolve-dependents``, that takes an installation,
  package and version (presumably installed) and lists all packages and
  versions that depend on that version of that package.

      - [ ] A new function, ``resolve-dependents``, that takes a package and
        version and query function and uses the query function to determine
        what other packages depend on the current package, recursively.

- [ ] A new subcommand, ``install-package``, that would take a plain list or
  json blob of packages and installs them provided their resources were
  mutually exclusive with each other and all installed packages (see below) and
  provided their dependencies were met. this can easily be done by calling
  ``resolve-dependencies`` with a nullary query function and big list of
  packages to install. Dependencies will not be checked if ``--force`` is
  present.

- [ ] A new subcommand, ``remove-package``, that would take a plain list or
  json blob of packages and remove them provided all dependencies were met.
  Package dependencies will not be checked if ``--force`` is present.

- [ ] A new subcommand, ``list-packages``, that would yield to standard out a
  plain/json list of packages from an installation. It would take an optional
  parameter that lists out a particular package at a particular version, or all
  packages matching only a package name.

A New Notion of Resources
+++++++++++++++++++++++++

As part of this new initiative, packages will be given a new attribute called
"resources". This attribute will house a map from resource type names to a map
of resource states. This map will map a resources to their states upon
installation. Both resources and resource states are simply strings. Meta
information under the ``meta`` key will also be stored.

For example, resources could be files and states could be sha256 hashes,
with any metadata about the files, like whether they are config or not::

  {
      "files": {
          "/var/lib/foo": {
              "state": "600dc0d36077a10ada600dd3a10fda7a600dc0d36077a10ada600dd3a10fda7a",
              "meta": {
                "tags": ["config"]
              }
          }
      }
  }

With this new notion of resources, resource management can be correctly
implemented upon package installation, removal, creation, and verification.

- [ ] A new option needs to be added to ``generate-card`` to specify resources.
  This would be an executable that would take a package name, version and
  location and would return resources in JSON over standard output.

- [ ] A new subcommand, ``verify-package``, needs to be created that verifies
  listed package resources against listed resources. This would take an
  executable as an option that took the name and type of a resource and yielded
  a string on standard output that would be the state. A diff of the supposed
  state and the actual state would be printed. If no arguments are given, it
  lists verification information for all such packages, plain/json.

- [ ] A feature of ``install-package``: A package can only be installed if no
  package, currently being installed or previously installed, installs the same
  resources.


2.2.0
-----

- [ ] Environment-variable specification of options: the ability to specify
  options using environment variables. A string will be returned as the value
  of the option, unless a caret (``^``) character is present in the string, in
  which case the string will be split into a list of strings using the caret
  characters as boundaries. Arguments that take a boolean option will atttempt
  to parse the string, expecting either the values ``true`` or ``false`` as the
  value of the string. It is an error for boolean arguments (e.g.,
  ``DEGASOLV_ALTERNATIVES`` or ``DEGASOLV_ERROR_FORMAT``) to have any other
  string.

- [ ] Authenticated HTTP and HTTPS connections: we will provide a way by which
  HTTP and HTTPS connections are authenticated, I think using a .netrc-like
  mapping between hosts and username/password pairs. Documentation should be
  written around specifying username and password securely according to needs,
  whether by standard input using the conventional config file mechanisms, by
  environment variable, or by command line. Each has security implications that
  the user needs to be aware of.

- [ ] Documentation should be written around specifying username and password
  securely according to needs, whether by standard input using the conventional
  config file mechanisms, by environment variable, or by command line. Each has
  security implications that the user needs to be aware of.


2.1.0
-----

- [ ] **Minimum Version Selection**: Implement minimum version selection as an
  option pack. Supporting features:
    - [x] Version suggestion: when trying different candidates, if a matching
      id is in the  return of a candidate, put the suggestions in the list to
      loop through.

    - CANCELLED: Skip unlikely candidates: Keep a set of problem ids different
      candidates are tried. If the current alternative's ID is not in that set,
      and if the dependencies are the same as the previous candidate, then skip
      the candidate; do not try it.

      This was cancelled because it was problematic.

    - [ ] Minimum version preference: In generate repo index, add option to
      sort packages the other way.

    - [ ] Proper documentation surrounding order of encounter, that for example
      for subproc degasolv will honor the order of packages found in the repo
      index and that this enables things like MVS.

- CANCELLED: **Git package system**: Implement git package system that knows
  how to read bitbucket, bitbucket server, github, github server, gitlab,
  gitlab server, and raw git repos.  This was cancelled because it felt "out of
  scope"; i didn't want to build ever-changing vendor-specific API details into
  a tool I hope will be a more generic tool that outlasts these APIs.
  I think git integrations is the subject for a good but different tool.

- [ ] **Inclusive/Absent corner case**: In the case that ``conflict-strat`` is
  inclusive, a case may arise where an absence is required, but then another
  requirement asks for the same package to be present at a conflicting version.
  basically, what do requirements of absence mean within and inclusive context.
  because right now when absence requirements are given, they hold force even
  in inclusive contexts. This seems silly. Requirements of absence should just
  be ignored when ``conflict-strat`` is ``inclusive``.

- [ ] Fix #17, it is awful.


