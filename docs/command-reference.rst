Degasolv Command Reference
==========================

This article describes the Degasolv CLI, what subcommands and options
there are, and what they are for.

Top-Level CLI
-------------

Running ``java -jar degasolv-<version>-standalone.jar -h`` will give you
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

  You could simply have a configuration file that looks like this::

    ;; filename: config.edn
    {
        :search-directory "/some/directory"
    }

  And use the configuration file like this::

    java -jar degasolv-<version>-standalone.jar \
      --config-file "$PWD/config.edn" \
      generate-repo-index [...]

  A few notable exceptions to this rule is the ``--repository`` option
  of the ``resolve-locations`` command, and the ``--requirement``
  rule. This is because these option can be specified multiple times,
  and so their configuration equivalent is named ``:repositories`` and
  ``:requirements`` respectively, and they show up in the
  configuration file as a list of strings. So, instead of using this
  command::

    java -jar degasolv-<version>-standalone.jar \
      resolve-locations \
      --repository "https://example.com/repo1/" \
      --repository "https://example.com/repo2/" \
      --requirement "a" \
      --requirement "b"
      [...]

  You might use this configuration file::

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
    -o, --output-file FILE  ./out.dscard  The name of the card file
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
  ``:requirements ["REQ1", ...]``: Specify a requirement.  May be
  specified multiple times as a command line option, or once as a list
  of strings in a configuration file. See :ref:`Specifying a
  requirement` for more information.

- ``-o FILE``, ``--output-file FILE``, ``:output-file "FILE"``:
  Specify the name of the card file to generate. It is best practice
  to name this file after the name of the file referred to by the package's
  location with a ``.dscard`` extension. For example, if I created a card
  using the option ``--location http://example.com/repo/a-1.0.zip``,
  I would name the output file ``a-1.0.zip.dscard``, as in
  ``--output-file a-1.0.zip.dscard``. By default, the output file is named
  ``out.dscard``.

- ``-h``, ``--help``: Print a help page for the subcommand ``generate-dscard``.

.. _`version-clj`: https://github.com/xsc/version-clj#version-comparison

CLI for ``generate-repo-index``
-------------------------------



CLI for ``resolve-locations``
-----------------------------

Explanation for each option and subcommand is below


~/Workspace/src/github.com/djhaskin987/degasolv $ java -jar target/uberjar/degasolv-1.0.2-SNAPSHOT-standalone.jar``, -h

~/Workspace/src/github.com/djhaskin987/degasolv $ java -jar target/uberjar/degasolv-1.0.2-SNAPSHOT-standalone.jar generate-repo-index -h
Usage: degasolv <options> generate-repo-index <generate-repo-index-options>

Options are shown below, with their default values and
  descriptions:

  -a, --add-to REPO_LOC                     Add to package information alread to be found at repo index REPO_LOC
  -o, --output-file FILE      index.dsrepo  The file to which to output the information.
  -d, --search-directory DIR  .             Directory to search for degasolv cards
  -h, --help                                Print this help page
~/Workspace/src/github.com/djhaskin987/degasolv $ java -jar target/uberjar/degasolv-1.0.2-SNAPSHOT-standalone.jar resolve-locations -h
Usage: degasolv <options> resolve-locations <resolve-locations-options>

Options are shown below, with their default values and
  descriptions:

  -r, --repository REPO                         Specify a repository to use. May be used more than once.
  -s, --resolve-strategy STRATEGY     thorough  Specify a strategy to use when resolving. May be 'fast' or 'thorough'.
  -R, --repo-merge-strategy STRATEGY  priority  Specify a repo merge strategy. May be 'priority' or 'global'.
  -h, --help                                    Print this help page
~/Workspace/src/github.com/djhaskin987/degasolv $  


CLI for ``query-repo``
----------------------

.. _Specifying a requirement:

Specifying a requirement
------------------------

foo.
