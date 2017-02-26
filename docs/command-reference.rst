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

.. _`version-clj`: https://github.com/xsc/version-clj#version-comparison

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
given at the command line. If successful, it outputs the name of each package
in the solution it has found, together with that package's location.

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

- ``-R INDEX``, ``--repository INDEX``, ``:repositories ["INDEX1", ...]``:
  **Required**. Search the repository index given by INDEX for packages when
  resolving the given requirements. 

  When the index strategy is ``priority`` The last repository index specified
  will be the first to be consulted. If the repository indices are retrieved
  from the config file, they are consulted in order from first to last in the
  list.  If indices are specified both on the command line and in the
  configuration file, the indices in the configuration file are ignored.


- ``-s STRAT``, ``--resolve-strat STRAT``:

CLI for ``query-repo``
----------------------

.. _Specifying a requirement:

Specifying a requirement
------------------------

