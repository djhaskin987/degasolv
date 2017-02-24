Degasolv Command Reference
==========================

This article describes the Degasolv CLI, what subcommands and options
there are, and what they are for.

Top-Level CLI
-------------

Running ``java -jar degasolv-<version>-standalone.jar`` will give you
a page that looks something like this::

  Usage: degasolv <options> <command> <<command>-options>

  Options are shown below, with their default values and
    descriptions:

    -c, --config-file FILE  ~/.config/degasolv/config.edn  config file
    -h, --help                                             Print this help page

  Commands are:

    - generate-card
    - generate-repo-index
    - resolve-locations

  Simply run ``degasolv <command> -h`` for help information.

Explanation of options:

- ``--config-file FILE``: A config file may be specified at the
  command line. The config file is in the `EDN format`_. As a rule,
  any option for any sub-command may be given a value from this config
  file, using the keyword form of the argument. For example, instead
  of running this command::

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

  A notable exception to this rule is the ``--repository`` option of the
  ``resolve-locations`` command. This is because that option can be specified
  multiple times, and so its configuration equivalent is named ``:repositories``,
  and shows up in the configuration file as a list of strings. So, instead of
  using this command::

    java -jar degasolv-<version>-standalone.jar \
      resolve-locations \
      --repository "https://example.com/repo1/" \
      --repository "https://example.com/repo2/" \
      [...]

  You might use this configuration file::

    ; filename: config.edn
    {
        :respositories ["https://example.com/repo1/"
                        "https://example.com/repo2/"]
    }

  With this command::

    java -jar degasolv-<version>-standalone.jar \
      --config-file "$PWD/config.edn" \
      [...]

- ``--help``: Prints the help page. This can be used on every
  sub-command as well.

.. _EDN format: https://github.com/edn-format/edn


CLI for ``generate-card``
-------------------------

Running ``java -jar degasolv-<version>-standalone.jar generate-card -h``
returns a page that looks something like this::

  Usage: degasolv <options> generate-card <generate-card-options>

  Options are shown below, with their default values and
    descriptions:

    -i, --id true                         ID (name) of the package
    -v, --version true                    Version of the package
    -l, --location true                   URL or filepath of the package
    -r, --requirement REQ                 List req, may be used multiple times
    -o, --output-file FILE  ./out.dscard  The name of the card file
    -h, --help                            Print this help page

This command is used to generate a *card* file. A card file is a file
which describes a package that degasolv can understand. It identifies
a file, assigns it a package name and a version, and
dependencies. This card file is then used to represent the package in
a degasolv repository.  In this way, the package's location and file
type is decoupled from the actual repository itself.

Explanation of options:

  - ``--id``:

SO BASICALLY, I need to make the options required first. See here: http://www.rkn.io/2014/02/27/clojure-cookbook-command-line-args/


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
