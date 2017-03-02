Degasolv Command Reference
==========================

This article describes the Degasolv CLI, what subcommands and options
there are, and what they are for.

Top-Level CLI
-------------

Running ``java -jar degasolv-<version>-standalone.jar -h`` will yield
a page that looks something like this::

  java -jar target/uberjar/degasolv-1.0.2-SNAPSHOT-standalone.jar -h
  Usage: degasolv <options> <command> <<command>-options>

  Options are shown below, with their default values and
    descriptions:

    -c, --config-file FILE  ./degasolv.edn  config file
    -h, --help                              Print this help page

  Commands are:

    - generate-card
    - query-repo
    - generate-repo-index
    - resolve-locations

  Simply run `degasolv <command> -h` for help information.

Explanation of options:

- ``-c FILE``, ``--config-file FILE``: A config file may be specified
  at the command line. The config file is in the `EDN format`_. As a
  rule, any option for any sub-command may be given a value from this
  config file, using the keyword form of the argument. For example,
  instead of running this command::

    java -jar degasolv-<version>-standalone.jar \
       generate-repo-index --search-directory /some/directory \
       [...]

  A configuration file that looks like this could be used instead::

    ;; filename: config.edn
    {
        :search-directory "/some/directory"
    }

  With this command::

    java -jar degasolv-<version>-standalone.jar \
      --config-file "$PWD/config.edn" \
      generate-repo-index [...]

  A few notable exceptions to this rule is the ``--repository`` option
  of the ``resolve-locations`` and ``query-repo`` commands, and the
  ``--requirement`` option of the ``generate-card`` and
  ``resolve-locations`` commands. This is because these options can be
  specified multiple times, and so their configuration file key
  equivalents are named ``:repositories`` and ``:requirements``
  respectively, and they show up in the configuration file as a list
  of strings. So, instead of using this command::

    java -jar degasolv-<version>-standalone.jar \
      resolve-locations \
      --repository "https://example.com/repo1/" \
      --repository "https://example.com/repo2/" \
      --requirement "a" \
      --requirement "b"
      [...]

  This configuration file might be used::

    ; filename: config.edn
    {
        :respositories ["https://example.com/repo1/"
                        "https://example.com/repo2/"]
        :requirements ["a"
                       "b"]
    }

  With this command::

    java -jar degasolv-<version>-standalone.jar \
      --config-file "$PWD/config.edn" \
      [...]

- ``-h``, ``--help``: Prints the help page. This can be used on every
  sub-command as well.

.. _EDN format: https://github.com/edn-format/edn


CLI for ``generate-card``
-------------------------

Running ``java -jar degasolv-<version>-standalone.jar generate-card -h``
returns a page that looks something like this::

  java -jar target/uberjar/degasolv-1.0.2-SNAPSHOT-standalone.jar generate-card -h
  Usage: degasolv <options> generate-card <generate-card-options>

  Options are shown below, with their default values and
    descriptions:

    -i, --id true                         ID (name) of the package
    -v, --version true                    Version of the package
    -l, --location true                   URL or filepath of the package
    -r, --requirement REQ                 List req, may be used multiple times
    -C, --card-file FILE  ./out.dscard    The name of the card file
    -h, --help                            Print this help page

  The following options are required for subcommand `generate-card`:

    - `-i`, `--id`, or the config file key `:id`.
    - `-v`, `--version`, or the config file key `:version`.
    - `-l`, `--location`, or the config file key `:location`.

This subcommand is used to generate a card file. This card file is
used to represent a package within a degasolv repository. It is placed
in a directory with other card files, and then the
``generate-repo-index`` command is used to search that directory for
card files to produce a repository index.

Explanation of options:

- ``-i ID``, ``--id ID``, ``:id "ID"``: **Required**. Specify the name of the
  package described in the card file. May be composed of any characters
  other than the following characters: ``<>=!,;|``.

- ``-v VERSION``, ``--version VERSION``, ``:version "VERSION"``:
  **Required**. Specify the name of the package described in the card
  file. Version comparison is done via `version-clj`_.

- ``-l LOCATION``, ``--location LOCATION``, ``:location "LOCATION"``:
  **Required**. Specify the location of the file associated with the
  package to be described in the generated card file. Degasolv does
  not place any restrictions on this string; it can be anything,
  including a file location or a URL.

- ``-r REQUIREMENT``, ``--requirement REQUIREMENT``,
  ``:requirements ["REQ1", ...]``: List a requirement (dependency) of the
  package in the card file.  May be specified one or more times as a command
  line option, or once as a list of strings in a configuration file. See
  :ref:`Specifying a requirement` for more information.

- ``-C FILE``, ``--card-file FILE``, ``:card-file "FILE"``:
  Specify the name of the card file to generate. It is best practice
  to name this file after the name of the file referred to by the package's
  location with a ``.dscard`` extension. For example, if I created a card
  using the option ``--location http://example.com/repo/a-1.0.zip``,
  I would name the card file ``a-1.0.zip.dscard``, as in
  ``--card-file a-1.0.zip.dscard``. By default, the card file is named
  ``out.dscard``.

- ``-h``, ``--help``: Print a help page for the subcommand ``generate-dscard``.


CLI for ``generate-repo-index``
-------------------------------

Running ``java -jar degasolv-<version>-standalone.jar generate-card -h``
returns a page that looks something like this::

  Usage: degasolv <options> generate-repo-index <generate-repo-index-options>

  Options are shown below, with their default values and
    descriptions:

    -d, --search-directory DIR  .             Find degasolv cards here
    -I, --index-file FILE       index.dsrepo  The name of the repo file
    -a, --add-to INDEX                        Add to repo index INDEX
    -h, --help                                Print this help page

This subcommand is used to generate a repository index file. A
repository index file lists all versions of all packages in a
particular degasolv repository, together with their locations. This
file's location, whether by file path or URL, would then be given to
``resolve-locations`` and ``query-repo`` commnds as degasolv
repositories.

Explanation of options:

- ``-d DIR``, ``--search-directory DIR``, ``:search-directory "DIR"``:
  Look for degasolv card files in this directory. The directory will
  be recursively searched for files with the ``.dscard`` extension and
  their information will be added to the index. Default value is the
  present working directory (``.``).

- ``-I FILE``, ``--index-file FILE``, ``:index-file "FILE"``: Write the
  index file at the location ``FILE``. Default value is ``index.dsrepo``. It is
  good practice to use the default value.

- ``-a INDEX``, ``--add-to INDEX``, ``:add-to "INDEX"``: Add to
  the repository index file found at ``INDEX``. In general, it is best
  to simply regenerate a new repository index fresh based on the card files
  found in a search directory; however, it may be useful to use this option
  to generate a repository file incrementally.

  For example, a card file might be generated during a build, then
  added to a repository index file in the same build script::

    #!/bin/sh

    java -jar degasolv-<version>-standalone.jar generate-card \
      -i "a" -v "1.0.0" -l "http://example.com/repo/a-1.0.0.zip" \
      -C "a-1.0.0.zip.dscard"

    java -jar degasolv-<version>-standalone.jar generate-repo-index \
      -I "new-index.dsrepo" -a "http://example.com/repo/index.dsrepo" \
      -d "."

    rsync -av a-1.0.0.zip.dscard user@example.com:/var/www/repo/
    rsync -av new-index.dsrepo user@example.com:/var/www/repo/index.dsrepo

  In this example, a card file is generated. Then, a new repository is
  generated based on an existing index and a newly generated card
  file. Then it is copied up to the repo server, replacing the old
  index. The card file is copied up as well to preserve the record in
  the search directory on the actual repository server so that a
  repository index could be generated on the server in the usual way
  later.

CLI for ``resolve-locations``
-----------------------------

Running ``java -jar degasolv-<version>-standalone.jar resolve-locations -h``
returns a page that looks something like this::

  Usage: degasolv <options> resolve-locations <resolve-locations-options>

  Options are shown below, with their default values and
    descriptions:

    -r, --requirement REQ                Resolve req. May be used more than once.
    -R, --repository INDEX               Use INDEX. May be used more than once.
    -s, --resolve-strat STRAT  thorough  May be 'fast' or 'thorough'.
    -S, --index-strat STRAT    priority  May be 'priority' or 'global'.
    -h, --help                           Print this help page

  The following options are required for subcommand `resolve-locations`:

    - `-R`, `--repository`, or the config file key `:repositories`.
    - `-r`, `--requirement`, or the config file key `:requirements`.

The ``resolve-locations`` command searches one or more repository index files,
and uses the package information in them to attempt to resolve the requirements
given at the command line. If successful, it exits with a return code of 0 and
outputs the name of each package in the solution it has found, together with
that package's location. 

Example output on a successful run::

    c==3.5.0 @ https://example.com/repo/c-3.5.0.zip
    d==0.8.0 @ https://example.com/repo/d-0.8.0.zip
    e==1.8.0 @ https://example.com/repo/e-1.8.0.zip
    b==2.3.0 @ https://example.com/repo/b-2.3.0.zip

In the above example out, each line takes the form::

    <id>==<version> @ <location

If the command fails, a non-zero exit code is returned. The output from such
a run might look like this::

  The resolver encountered the following problems: 

  Clause: e>=1.1.0,<2.0.0
  - Packages selected:
    - b==2.3.0 @ https://example.com/repo/b-2.3.0.zip
    - d==0.8.0 @ https://example.com/repo/d-0.8.0.zip
  - Packages already present: None
  - Alternative being considered: e>=1.1.0,<2.0.0
  - Package in question was found in the repository, but cannot be used.
  - Package ID in question: e

As shown above, a list of clauses is printed. Each clause is an alternative (part of a requirement)
that the resolver could not fulfill or resolve. Each field is explained as follows:

1. ``Packages selected``: This is a list of packages found in order to resolve previous requirements
   before the "problem" clause was encountered.
2. ``Packages already present``: This is an artifact of the resolver. It will always be ``None`` and can
   be ignored.
3. ``Alternative being considered``: This field displays what alternative from the requirement
   was being currently considered when the problem was encountered.
4. The next field gives a reason for the problem.
5. ``Package ID in question``: This field displays the package searched for
   when the problem was encountered.

Explanation of options:

- ``-r REQ``, ``--requirement REQ``, ``:requirements ["REQ1", ...]``:
  **Required**. Resolve this requirement together with all other requirements
  given.  May be specified one ore more times as a command line option, or once
  as a list of strings in a configuration file. See
  :ref:`Specifying a requirement` for more information.

  The last requirement specified will be the first to be resolved. If the
  requirements are retrieved from the config file, they are resolved in order
  from first to last in the list.  If requirements are specified both on the
  command line and in the configuration file, the requirements in the
  configuration file are ignored.

.. _that option's explanation:

- ``-R INDEX``, ``--repository INDEX``, ``:repositories ["INDEX1", ...]``:
  **Required**. Search the repository index given by INDEX for packages when
  resolving the given requirements.

  When the index strategy is ``priority`` The last repository index specified
  will be the first to be consulted. If the repository indices are retrieved
  from the config file, they are consulted in order from first to last in the
  list.  If indices are specified both on the command line and in the
  configuration file, the indices in the configuration file are ignored. See
  `index strategy`_ for more information.

- ``-s STRAT``, ``--resolve-strat STRAT``, ``:resolve-strat "STRAT"``: This
  option determines which versions of a given package id are considered when
  resolving the given requirements.  If set to ``fast``, only the first
  available version matching the first set of requirements on a particular
  package id is consulted, and it is hoped that this version will match all
  subsequent requirements constraining the versions of that id. If set to
  ``thorough``, all available versions matching the requirements will be
  considered.

  This option should be used with care, since whatever setting is used will
  greatly alter behavior. It is therefore recommended that whichever setting is
  chosen should be used site-wide within an organization.  The default setting
  is ``thorough`` and this setting should work for most environments.

.. _index strategy:

- ``-S STRAT``, ``--index-strat STRAT``, ``:index-strat "STRAT"``: Repositories
  are queried by package id in order to discover what packages are available to
  fulfill the given requirements. This option determines how multiple
  repository indexes are queried if there are more than one. If set to
  ``priority``, the first repository that answers with a non-empty result is
  used, if any. Not that this is true even if the versions done't match what is
  required.

  For example, if ``<repo-x>`` contains a package ``a`` at version ``1.8``,
  and ``<repo-y>`` contains a package ``a`` at version ``1.9``, then the
  following command wil fail::

    java -jar ./degasolv-<version>-standalone.jar -R <repo-x> -R <repo-y> \
        -r "a==1.9"

  While, on the other hand, this command will succeed::

    java -jar ./degasolv-<version>-standalone.jar -R <repo-y> -R <repo-x> \
        -r "a==1.9"

  By contrast, if ``--index-strat`` is given the STRAT of ``global``, all versions
  from all repositories answering to a particular package id will be considered. So,
  both of the following commands would succeed, under the scenario presented above::

    java -jar ./degasolv-<version>-standalone.jar -S global \
        -R <repo-x> -R <repo-y> -r "a==1.9"

    java -jar ./degasolv-<version>-standalone.jar -S global \
        -R <repo-y> -R <repo-x> -r "a==1.9"

  This option should be used with care, since whatever setting is used will
  greatly alter behavior. It is therefore recommended that whichever setting is
  chosen should be used site-wide within an organization.

  The default setting is ``priority`` and this setting should work for most
  environments.

CLI for ``query-repo``
----------------------

Running ``java -jar degasolv-<version>-standalone.jar query-repo -h`` returns a
page that looks something like this::

  Usage: degasolv <options> query-repo <query-repo-options>

  Options are shown below, with their default values and
    descriptions:

    -R, --repository INDEX             Search INDEX for packages. May be used more than once.
    -q, --query QUERY                  Display packages matching query string.
    -S, --index-strat STRAT  priority  May be 'priority' or 'global'.
    -h, --help                         Print this help page

  The following options are required for subcommand `query-repo`:

    - `-R`, `--repository`, or the config file key `:repositories`.
    - `-q`, `--query`, or the config file key `:query`.

This subcommand queries a repository index or indices for packages. This comand
is intended to be useful or debugging dependency problems.

Explanation of options:

- ``-q QUERY``, ``--query QUERY``: **Required**. Query repository index or indices for a
  package. Syntax is exactly the same as requirements except that only one
  alternative may be specified (that is, using the ``|`` character or
  specifying multiple package ids), and the requirement must specify
  a present package (no ``!`` character may be used either).
  See `Specifying a requirement`_ for more information.

  Examples of valid queries:

    - ``"pkg"``
    - ``"pkg!=3.0.0"``

  Examples if invalid queries:

    - ``"a|b"``
    - ``"!a"``

- ``-R INDEX``, ``--repository INDEX``, ``:repositories ["INDEX1", ...]``: **Required**.
  This option works exactly the same as the repository option for the
  ``resolve-locations`` command, except that instead of using the repositories
  for resolving requirements, it uses them for simple index queries. See `that
  option's explanation`_ for more information.

.. _Specifying a requirement:

Specifying a requirement
------------------------

A requirement is given as a string of text. It is given as a string. A
requirement consists of one or more *alternatives*. Any of the alternatives
will satisfy the requirement. Alternatives are specified by a bar character
(``|``), like this::

  "<alt1>|<alt2>|<alt3>"

Or, more concretely::

  "hickory|maple|oak"

Alternatives will be considered in order fo appearance. In general, specifying
more than one alternative should be msotly unecessary, and generally to be
avoided. THis is because many alternatives tend to impact performance
significantly; but they are there and usable if needed.

Each alternative is composed of a package id and an optional specification of
what versions of that package satisfy the alternative, like this::

  "<pkgid><version spec>"

For example::

  "hickory>=3.0"

A version spec is a boolean expression of version predicates describing what
versions may satisfy the alternative. The character ``;`` represents discution
(OR) and the character ``,`` represents conjunction (AND), like this::

  "<pred1>,<pred2>;<pred3>,<pred4>"


This is interpreted as::

  "(<pred1> AND <pred2>) OR (<pred3> AND <pred4>)"

Each version predicate is composed of a comparison operator and a valid version
against which to compare a package's fversion. The character sequences ``<``,
``<=``, ``!=``, ``==``, ``>=``, and ``>`` represent the comparisons "older
than", "older than or equal to", "not equal to", "equal to", "newer than or
equal to", and "newer than", respectively.In the current implementation,
versions are compared using `version-clj`_ rules.

.. _`version-clj`: https://github.com/xsc/version-clj#version-comparison

The follwoing are examples of valid alternatives, together with their english
interpretations:

+------------------------------+----------------------------------------------+
| Alternative                  | English Interpretation                       |
+==============================+==============================================+
| ``"oak"``                    | Find package ``oak``                         |
+------------------------------+----------------------------------------------+
| ``"pine>1.0"``               | Find pakcage ``pine`` of version newer than  |
|                              | ``1.0``                                      |
+------------------------------+----------------------------------------------+
| ``"hickory>1.0,<=2.0"``      | Find package ``hickory`` with version newer  |
|                              | than``1.0`` and older than or equal to       |
|                              | ``2.0``.                                     |
+------------------------------+----------------------------------------------+
| ``"fir<=2.0;>3.5,!=3.8"``    | Find a package ``fir`` with version          |
|                              | (newer than ``1.0`` and older than or equal  |
|                              | to ``2.0``) OR (with version newer than      |
|                              | ``3.5`` but not equal to ``3.8``)            |
+------------------------------+----------------------------------------------+

Negative alternatives are requirements that all packages with a particular id
and matching a particular version spec must be absent from the list of packages
found when resolving dependencies. To negate an alternative, prepend it with
the ``!`` character.

For example, the following alternative means "make sure
the ``spruce`` package is not present in the list"::

  !spruce

This alternative means "If package a is present in the list, make sure its
version is not in the range ``(3.0,4.0]``"::

  !a>3.0,<=4.0

The following are practical examples of requirements, together with their
interpretations.

+-------------------------+---------------------------------------------------+
| Requirement             | English Explanation                               |
+-------------------------+---------------------------------------------------+
| ``"oak|pine>5.0"``      | Require ``oak`` at any version, or ``pine`` at    |
|                         | versions greater than ``5.0``                     |
+-------------------------+---------------------------------------------------+
| ``"hickory>=3.0,<4.0"`` | Require ``hickory`` at a ``3.x`` version.         |
+-------------------------+---------------------------------------------------+
| ``"!birch|birch<=3.0"`` | An important example. This demonstrates how to    |
| ``"!birch>3.0"``        | specify what `maven`_ calls a                     |
|                         | `managed dependency`_.                            |
|                         | It means if ``birch`` is required by another      |
|                         | package, ensure that its version is older than or |
|                         | equal to ``3.0``. It is good practice to prefer   |
|                         | the expression with only one alternative.         |
+-------------------------+---------------------------------------------------+
| ``"!oak|maple>3.0"``    | If oak is installed, then make sure maple after   |
|                         | version 3.0 is installed also.                    |
+-------------------------+---------------------------------------------------+
| ``"oak|!pine"``         | Require the presence of the ``oak`` package, or   |
|                         | the absence of the ``pine`` package.              |
+-------------------------+---------------------------------------------------+


.. _maven: https://maven.apache.org/


.. _managed dependency: https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Management
