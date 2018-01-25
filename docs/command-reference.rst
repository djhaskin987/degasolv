.. _Command Reference:

Degasolv Command Reference
==========================

This article describes the Degasolv CLI, what subcommands and options
there are, and what they are for.

Some Notes on Versions
----------------------

- On a best-effort basis, features have had the version that they first
  appeared associated with them in this guide.

- Anything tagged with version 1.0.2 *really* means "1.0.2 or
  earlier". The history gets shaky before that :)

- The first version of Degasolv (for the purposes of this guide)
  released was 1.0.2 .

- As of version 1.3.0, All options which take a file name may now have
  ``-`` given as the filename, to specify that standard in should be
  used.

- The earliest usable released version of Degasolv that can be
  recommended for use is 1.5.1 . Anything before that wasn't profiled,
  and had some pretty bad bugs in it.

.. _top-level-cli:

Top-Level CLI
-------------

Top-Level Usage Page
++++++++++++++++++++

Running ``java -jar degasolv-<version>-standalone.jar -h`` will yield
a page that looks something like this::

  Usage: degasolv <options> <command> <<command>-options>

  Options are shown below, with their default values and
    descriptions. Options marked with `**` may be
    used more than once.

  -c, --config-file FILE  ./degasolv.edn  Config file location **
  -j, --json-config FILE                  JSON config file location **
  -k, --option-pack PACK                  Specify option pack **
  -h, --help                              Print this help page

  Commands are:

    - display-config
    - generate-card
    - generate-repo-index
    - resolve-locations
    - query-repo

  Simply run `degasolv <command> -h` for help information.

.. _specifying-files:

A Note on Specifying Files
++++++++++++++++++++++++++

As of version 1.3.0, The whenever an option takes a file in degasolv,
the user can actually specify one of three things:

  1. An ``http://`` or ``https://`` URL. No authentication is
     currently supported.
  2. A ``file://`` URL.
  3. A filesystem reference.
  4. The character ``-``, signifying standard input to the Degasolv process.

This is true for options of Degasolv and options for any of its subcommands.

Explanation of Options
++++++++++++++++++++++

Degasolv parses global options before it parses subcommands or the options for
subcommands; therefore, global options need to be specified first.

Using Configuration Files
*************************

Configuration files may be specified at the command line before specifying any
subcommands. The config file structure is designed so that any command-line
option may be set in the config file instead, and vice versa.

In addition, config files may be specified either in the EDN format or JSON
format. Multiple config files may be specified. "Mixing and matching" of JSON
and EDN config files is supported.

Basic EDN Configuration Usage
#############################

+-----------------------------+---------------------------------------+
| Short option                | ``-c FILE``                           |
+-----------------------------+---------------------------------------+
| Long option                 | ``--config-file FILE``                |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

A config file may be specified at the command line. The config file is
in the `EDN format`_. As a rule, any option for any sub-command may be
given a value from this config file, using the keyword form of the
argument. For example, instead of running this command::

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

Notable exceptions to this rule include options which may be
specified multiple times. These options are named using singular
nouns (e.g. ``--repository REPO``), but their corresponding
configuration file keys are specified using plural nouns (e.g.,
``:repositories ["REPO1", ... ]``).

So, instead of using this
command::

  java -jar degasolv-<version>-standalone.jar \
    resolve-locations \
    --disable-alternatives \
    --present-package "x==0.1" \
    --present-package "y==0.2" \
    --repository "https://example.com/repo1/" \
    --repository "https://example.com/repo2/" \
    --requirement "a" \
    --requirement "b"
    [...]

This configuration file might be used::

  ; filename: config.edn
  {
      :alternatives false
      :respositories ["https://example.com/repo1/"
                      "https://example.com/repo2/"]
      :requirements ["a"
                     "b"]
      :present-packages ["x==0.1"
                         "y==0.2"]
  }

With this command::

  java -jar degasolv-<version>-standalone.jar \
    --config-file "$PWD/config.edn" \
    resolve-locations \
    [...]



.. _json-config:

Basic JSON Configuration Usage
##############################

+-----------------------------+---------------------------------------+
| Short option                | ``-j FILE``                           |
+-----------------------------+---------------------------------------+
| Long option                 | ``--json-config FILE``                |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.12.0                                |
+-----------------------------+---------------------------------------+

Any config file option that can be specified using EDN may also be specified
using the `JSON format`_. The only difference is that a plain string should be
used as the key for the config option instead of an EDN keyword.

For example, instead of using this config file::

    ; filename: config.edn
    {
      :alternatives false
      :respositories ["https://example.com/repo1/"
                      "https://example.com/repo2/"]
      :id "x"
      :version "1.0.0"
      :requirements ["a"
                     "b"]
      :present-packages ["x==0.1"
                         "y==0.2"]
    }

With this command::

  java -jar degasolv-<version>-standalone.jar \
    --config-file "$PWD/config.edn" \
    resolve-locations \
    [...]

This JSON config file may be used instead::

    {
      "alternatives": false,
      "repositories": ["https://example.com/repo1/"
                       "https://example.com/repo2/"],
      "id": "x",
      "version": "1.0.0",
      "requirements": ["a"
                       "b"],
      "present-packages": ["x==0.1"
                           "y==0.2"]
    }

The command to use the above JSON config file would look like this::

  java -jar degasolv-<version>-standalone.jar \
    --json-config "$PWD/config.json" \
    resolve-locations \
    [...]

Using Multiple Configuration Files
##################################

As of version 1.2.0, the ``--config-file`` option may be specified multiple
times. As of version 1.12.0, the ``--json-config`` option may also be
specified, and it too may be multiple times.

Degasolv processes JSON config files together with EDN config
files. Each configuration file specified will get its configuration
merged into the previously specified configuration files, whether those
files be EDN or JSON. If both configuration files contain the same option, the
option specified in the latter specified configuration file will be used.

.. _config files section:

As an example, consider the following `display-config command`_::

  java -jar degasolv-<version>-standalone.jar \
    --config-file "$PWD/a.edn" \
    --json-config "$PWD/j.json" \
    --config-file "$PWD/b.edn" \
    display-config

If this is the contents of the file ``a.edn``::

  {
      :index-strat "priority"
      :repositories ["https://example.com/repo1/"]
      :id "a"
      :version "1.0.0"
  }

And this were the contents of ``j.json``::

  {
      "alternatives": false,
      "requirements": ["x", "y"]
  }

And this were the contents of ``b.edn``::

  {
      :conflict-strat "exclusive"
      :repositories ["https://example.com/repo2/"]
      :id "b"
      :version "2.0.0"
      :requirements []
  }

Then the output of the above command would look like this::

  {
      :alternatives false,
      :index-strat "priority",
      :repositories ["https://example.com/repo2/"],
      :id "b",
      :version "2.0.0",
      :conflict-strat "exclusive",
      :requirements []
      :arguments ["display-config"],
  }

.. note:: The JSON config file keys and their formatting will be
   listed for the options of all the subcommands in this document;
   however, **JSON config files can only be used with Degasolv version 1.12.0
   or greater.** This point bears special emphasis. Lots of config options say
   they were released in earlier versions. This is true; however, the only
   format of config file available for use was the EDN config file type before
   version 1.12.0 of Degasolv.

.. _site-wide:

Using Site-Wide Configuration Files
###################################

The merging of config files, together with the interesting
fact that config files may be specified via HTTP/HTTPS URLs,
allows the user to specify a *site config file*.

Multiple sub-commands have options which fundamentally change how Degasolv
works. These are ``--conflict-strat``, ``--index-strat``, ``--resolve-strat``
and ``--search-strat``. It is therefore recommended that these specific options
are specified site-wide, if they are specified at all.  Specifying these in a
site config file, then serving that config file internally via HTTP(S) would
allow all instances of Degasolv to point to a site-wide file, together with a
build-specific config file, as in this example::

  java -jar degasolv-<version>-standalone.jar \
      --config-file "https://nas.example.com/degasolv/site.edn" \
      --config-file "./degasolv.edn" \
      generate-card

.. _option-pack:
.. _option pack:

Option Packs
************

+-----------------------------+---------------------------------------+
| Short option                | ``-k PACK``                           |
+-----------------------------+---------------------------------------+
| Long option                 | ``--option-pack PACK``                |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:option-packs ["PACK1",...]``       |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"option-packs": ["PACK1",...],``    |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.7.0                                 |
+-----------------------------+---------------------------------------+

Specify one or more option packs.

Degasolv ships with several "option packs", each of which imply
several Degasolv options at once. When an option pack is specified,
Degasolv looks up which option pack is used and what options are
implied by using it. More than one option pack may be specified.  If
option packs are specified both on the command line and in the config
file, the option packs on the command line are used and the ones in
the config file are ignored.

The following option packs are supported in the current version:
  - ``multi-version-mode``: Added as of version 1.7.0 . Implies
    ``--conflict-strat inclusive``,
    ``--resolve-strat fast``, and ``--disable-alternatives``.
  - ``firstfound-version-mode``: Added as of version 1.7.0 . Implies
    ``--conflic-strat prioritized``,
    ``--resolve-strat fast``, and ``--disable-alternatives``.

Print the Help Page
*******************

+-----------------------------+---------------------------------------+
| Short option                | ``-h``                                |
+-----------------------------+---------------------------------------+
| Long option                 | ``--help``                            |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

``-h``, ``--help``: Prints the help page. This can be used on every
sub-command as well.

.. _EDN format: https://github.com/edn-format/edn
.. _JSON format: https://github.com/clojure/data.json

.. _display-config command:
.. _display-config-cli:

CLI for ``display-config``
--------------------------

Usage Page for ``display-config``
+++++++++++++++++++++++++++++++++

Running ``java -jar degasolv-<version>-standalone.jar display-config -h``
returns a page that looks something like this::

  Usage: degasolv <options> display-config <display-config-options>

  Options are shown below. Default values are marked as <DEFAULT> and
    descriptions. Options marked with `**` may be
    used more than once.

        --search-directory DIR    .              Find degasolv cards here
        --index-file FILE         index.dsrepo   The name of the repo file
        --index-strat STRAT       priority       May be 'priority' or 'global'.
        --requirement REQ                        Resolve req. **
        --search-strat STRAT      breadth-first  May be 'breadth-first' or 'depth-first'.
        --conflict-strat STRAT    exclusive      May be 'exclusive', 'inclusive' or 'prioritized'.
        --repository INDEX                       Search INDEX for packages. **
        --enable-alternatives                    Consider all alternatives (default)
        --id true                                ID (name) of the package
        --query QUERY                            Display packages matching query string.
        --disable-alternatives                   Consider only first alternatives
        --add-to INDEX                           Add to repo index INDEX
        --card-file FILE          ./out.dscard   The name of the card file
        --present-package PKG                    Hard present package. **
        --resolve-strat STRAT     thorough       May be 'fast' or 'thorough'.
        --location true                          URL or filepath of the package
        --package-system SYS      degasolv       May be 'degasolv' or 'apt'.
        --version-comparison CMP  maven          May be 'debian', 'maven', 'naive', 'python', 'rpm', 'rubygem', or 'semver'.
        --version true                           Version of the package
    -h, --help                                   Print this help page

Overview of ``display-config``
++++++++++++++++++++++++++++++

*This subcommand introduced as of version 1.6.0*.

The ``display-config`` command is used to print all the options in the
*effective configuration*. It allows the user to debug configuration
by printing the actual configuration used by Degasolv after all the
command-line arguments and config files have been merged together. An
example of this is found in the `config files section`_.

As of version 1.6.0, ``display-config`` accepts any valid option
in long form (``--long-form``) which is accepted by any other
subcommand. This enables the user to print out the effective
configuration resulting from multiple config files as well
as any options that might be given on the CLI.

.. _generate-card-options:

CLI for ``generate-card``
-------------------------

Usage Page for ``generate-card``
++++++++++++++++++++++++++++++++

Running ``java -jar degasolv-<version>-standalone.jar generate-card -h``
returns a page that looks something like this::

  Usage: degasolv <options> generate-card <generate-card-options>

  Options are shown below. Default values are marked as <DEFAULT> and
    descriptions. Options marked with `**` may be
    used more than once.

    -C, --card-file FILE   ./out.dscard  The name of the card file
    -i, --id true                        ID (name) of the package
    -l, --location true                  URL or filepath of the package
    -m, --meta K=V                       Add additional metadata
    -r, --requirement REQ                List requirement **
    -v, --version true                   Version of the package
    -h, --help                           Print this help page

  The following options are required for subcommand `generate-card`:

    - `-i`, `--id`, or the config file key `:id`.
    - `-v`, `--version`, or the config file key `:version`.
    - `-l`, `--location`, or the config file key `:location`.

Overview of ``generate-card``
+++++++++++++++++++++++++++++

*This subcommand introduced as of version 1.0.2*.

This subcommand is used to generate a card file. This card file is
used to represent a package within a Degasolv repository. It is placed
in a directory with other card files, and then the
``generate-repo-index`` command is used to search that directory for
card files to produce a repository index.

Explanation of Options for ``generate-card``
++++++++++++++++++++++++++++++++++++++++++++

Specify Location of the Card File
*********************************

+-----------------------------+---------------------------------------+
| Short option                | ``-C FILE``                           |
+-----------------------------+---------------------------------------+
| Long option                 | ``--card-file FILE``                  |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:card-file "FILE"``                 |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"card-file": ["FILE",...],``        |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

Specify the name of the card file to generate. It is best practice
to name this file after the name of the file referred to by the package's
location with a ``.dscard`` extension. For example, if I created a card
using the option ``--location http://example.com/repo/a-1.0.zip``,
I would name the card file ``a-1.0.zip.dscard``, as in
``--card-file a-1.0.zip.dscard``. By default, the card file is named
``out.dscard``.

Specify the ID (Name) of the Package
************************************

+-----------------------------+---------------------------------------+
| Short option                | ``-i ID``                             |
+-----------------------------+---------------------------------------+
| Long option                 | ``--id ID``                           |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:id "ID"``                          |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"id": "ID",``                       |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

**Required**. Specify the ID of the package described in the card
file. The ID serves both as a unique identifier for the package and
its name. It may be composed of any characters other than the
following characters: ``<>=!,;|``.

Specify the Location of the Package
***********************************

+-----------------------------+---------------------------------------+
| Short option                | ``-l LOCATION``                       |
+-----------------------------+---------------------------------------+
| Long option                 | ``--location LOCATION``               |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:location "LOCATION"``              |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"location": "LOCATION",``           |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

**Required**. Specify the location of the file associated with the
package to be described in the generated card file. Degasolv does
not place any restrictions on this string; it can be anything,
including a file location or a URL.

.. _meta-data:

Specify Additional Metadata for a Package
*****************************************

+-----------------------------+---------------------------------------+
| Short option                | ``-m K=V``                            |
+-----------------------------+---------------------------------------+
| Long option                 | ``--meta K=V``                        |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:meta {:key1 "value1" ...}``        |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"meta": {"key1": "value1", ...},``  |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.11.0                                |
+-----------------------------+---------------------------------------+

Specify additional metadata about the package within the card
file. This metadata will stay with the package information in its card
file. It will also be printed with other package information about the
package when the package is printed after dependency resolution when
`resolve-locations`_ subcommand is called, provided that the
`output-format`_ option is also used in a mode other than ``plain``.

This is a powerful feature allowing the operator to build tooling on
top of Degasolv. For example, now the operator may store the sha256
sum of the artifact, the location of its PGP signature, a list of
scripts useful in the build contained within the artifact, etc.

For key/value pairs specified on the command line, keys are turned
into EDN keywords (e.g., ``:K``) internally and values are simply
taken as strings. Additional metadata can also be specified from a
configuration file as well. When they are specified via config file,
they may be any data type allowed by EDN.

Key/value pairs specified via configuration file must be children of
the top-level ``:meta`` key, like this::

  {
      ...
      :meta {
          :sha256sum "sumsumsum"
          :otherkey "suchvalue"
          :key3 ["values", "can", "be", "lists"]
          :key4 {:key1 "or",
                 :key2 "maps"}
      }
  }

If used from the config file, the map's keys and values will be
placed directly in to the card file. If keys ``:id``, ``:version``
``:location``, or ``:requirements`` are specified in the config
file, or keys ``id=``, ``version=``, ``location=``, or
``requirements=`` on the CLI, they will be ignored.

Specify a Requirement for a Package
***********************************

+-----------------------------+---------------------------------------+
| Short option                | ``-r REQ``                            |
+-----------------------------+---------------------------------------+
| Long option                 | ``--requirement REQ``                 |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:requirements ["REQ1", ...]``       |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"requirements": ["REQ1", ...],``    |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

List a requirement (dependency) of the package in the card file.  May
be specified one or more times as a command line option, or once as a
list of strings in a configuration file. See :ref:`Specifying a
requirement` for more information.

Specify a Version for a Package
*******************************

+-----------------------------+---------------------------------------+
| Short option                | ``-v VERSION``                        |
+-----------------------------+---------------------------------------+
| Long option                 | ``--version VERSION``                 |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:version "VERSION"``                |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"version": "VERSION",``             |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

**Required**. Specify the name of the package described in the card
file.

Print the ``generate-card`` Help Page
*************************************

+-----------------------------+---------------------------------------+
| Short option                | ``-h``                                |
+-----------------------------+---------------------------------------+
| Long option                 | ``--help``                            |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

Print a help page for the subcommand ``generate-card``.

.. _generate-repo-index:

CLI for ``generate-repo-index``
-------------------------------

Usage Page for ``generate-repo-index``
++++++++++++++++++++++++++++++++++++++

Running ``java -jar degasolv-<version>-standalone.jar generate-repo-index -h``
returns a page that looks something like this::

  Usage: degasolv <options> generate-repo-index <generate-repo-index-options>

  Options are shown below. Default values are marked as <DEFAULT> and
    descriptions. Options marked with `**` may be
    used more than once.

    -a, --add-to INDEX                          Add to repo index INDEX
    -d, --search-directory DIR    .             Find degasolv cards here
    -I, --index-file FILE         index.dsrepo  The name of the repo file
    -V, --version-comparison CMP  maven         May be 'debian', 'maven', 'naive', 'python', 'rpm', 'rubygem', or 'semver'.
    -h, --help                                  Print this help page

Overview of ``generate-repo-index``
+++++++++++++++++++++++++++++++++++

*This subcommand introduced as of version 1.0.2*.

This subcommand is used to generate a repository index file. A
repository index file lists all versions of all packages in a
particular Degasolv repository, together with their locations. This
file's location, whether by file path or URL, would then be given to
``resolve-locations`` and ``query-repo`` commands as Degasolv
repositories.

Explanation of Options for ``generate-repo-index``
++++++++++++++++++++++++++++++++++++++++++++++++++

Specify the Repo Search Directory
*********************************

+-----------------------------+---------------------------------------+
| Short option                | ``-d DIR``                            |
+-----------------------------+---------------------------------------+
| Long option                 | ``--search-directory DIR``            |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:search-directory "DIR"``           |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"search-directory": "DIR",``        |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

Look for Degasolv card files in this directory. The directory will be
recursively searched for files with the ``.dscard`` extension and
their information will be added to the index. Default value is the
present working directory (``.``).

Specify the Repo Index File
***************************

+-----------------------------+---------------------------------------+
| Short option                | ``-I FILE``                           |
+-----------------------------+---------------------------------------+
| Long option                 | ``--index-file FILE``                 |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:index-file "FILE"``                |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"index-file": "FILE",``             |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

Write the index file at the location ``FILE``. Default value is
``index.dsrepo``. It is good practice to use the default value.

.. _version-comparison-generate:

Specify the Version Comparison Algorithm
****************************************

+-----------------------------+---------------------------------------+
| Short option                | ``-V CMP``                            |
+-----------------------------+---------------------------------------+
| Long option                 | ``--version-comparison CMP``          |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:version-comparison "CMP"``         |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"version-comparison": "CMP",``      |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.8.0                                 |
+-----------------------------+---------------------------------------+

Use the specified version comparison algorithm when generating the
repository index. When repository indexes are generated, lists of
packages representing different versions of each named package are
created within the index. These lists are sorted in descending order
by version number, so that the latest version of a given package is
tried first when resolving dependencies.

This option allows the operator to change what version comparison
algorithm is used. By default, the algorithm is ``maven``. May be
``maven``, ``debian``, ``maven``, ``naive``, ``python``, ``npm``,
``rubygem``, or ``semver``.

.. caution:: This is one of those options that should not be used
           unless the operator has a good reason, but it is available
           and usable if needed.

.. note:: This option should be used with care, since whatever setting
   is used will greatly alter behavior. Similar options are availabe
   for the ``resolve-locations`` subcommand and the ``query-repo``
   subcommand. They should all agree when used within the same
   site. It is therefore recommended that whichever setting is chosen
   should be used `site-wide`_ within an organization.

Add to an Existing Repository Index
***********************************

+-----------------------------+---------------------------------------+
| Short option                | ``-a INDEX``                          |
+-----------------------------+---------------------------------------+
| Long option                 | ``--add-to INDEX``                    |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:add-to "INDEX"``                   |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"add-to": "INDEX",``                |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

Add to the repository index file found at ``INDEX``. In general, it is
best to simply regenerate a new repository index fresh based on the
card files found in a search directory; however, it may be useful to
use this option to generate a repository file incrementally.

For example, a card file might be generated during a build, then added
to a repository index file in the same build script::

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

``INDEX`` may be a URL or a filepath. Both HTTP and HTTPS URLs are
supported. As of version 1.3.0, an ``INDEX`` may be specified as
``-``, the hyphen character. If ``INDEX`` is ``-``, Degasolv will read
standard input instead of any specific file or URL.

.. _resolve-locations:

CLI for ``resolve-locations``
-----------------------------

Usage Page for ``resolve-locations``
++++++++++++++++++++++++++++++++++++

Running ``java -jar degasolv-<version>-standalone.jar resolve-locations -h``
returns a page that looks something like this::

    Usage: resolve-locations <options>

    Options are shown below. Default values are listed with the
      descriptions. Options marked with `**` may be
      used more than once.

      -a, --enable-alternatives                          Consider all alternatives (default)
      -A, --disable-alternatives                         Consider only first alternatives
      -e, --search-strat STRAT            breadth-first  May be 'breadth-first' or 'depth-first'.
      -g, --enable-error-format                          Enable output format for errors
      -G, --disable-error-format                         Disable output format for errors (default)
      -f, --conflict-strat STRAT          exclusive      May be 'exclusive', 'inclusive' or 'prioritized'.
      -L, --list-strat STRAT              as-set         May be 'as-set', 'lazy' or 'eager'.
      -o, --output-format FORMAT          plain          May be 'plain', 'edn' or 'json'
      -p, --present-package PKG                          Hard present package. **
      -r, --requirement REQ                              Resolve req. **
      -R, --repository INDEX                             Search INDEX for packages. **
      -s, --resolve-strat STRAT           thorough       May be 'fast' or 'thorough'.
      -S, --index-strat STRAT             priority       May be 'priority' or 'global'.
      -t, --package-system SYS            degasolv       May be 'degasolv', 'apt', or 'subproc'.
      -u, --subproc-output-format FORMAT  json           Whether to read `edn` or `json` from the exe's output
      -V, --version-comparison CMP        maven          May be 'debian', 'maven', 'naive', 'python', 'rpm', 'rubygem', or 'semver'.
      -x, --subproc-exe PATH                             Path to the executable to call to get package data
      -h, --help                                         Print this help page

    The following options are required:

      - `-R`, `--repository`, or the config file key `:repositories`.
      - `-r`, `--requirement`, or the config file key `:requirements`.

Overview of ``resolve-locations``
+++++++++++++++++++++++++++++++++

*This subcommand introduced as of version 1.0.2*.

The ``resolve-locations`` command searches one or more repository
index files, and uses the package information in them to attempt to
resolve the requirements given at the command line. If successful, it
exits with a return code of 0 and outputs the name of each package in
the solution it has found, together with that package's location.

If the command fails, a non-zero exit code is returned. The output from such
a run might look like this::

  The resolver encountered the following problems:

  Clause: e>=1.1.0,<2.0.0
  - Packages selected:
    - b==2.3.0 @ https://example.com/repo/b-2.3.0.zip
    - d==0.8.0 @ https://example.com/repo/d-0.8.0.zip
  - Packages already present:
    - x==0.1.0 @ already present
    - y==0.2.0 @ already present
  - Alternative being considered: e>=1.1.0,<2.0.0
  - Package in question was found in the repository, but cannot be used.
  - Package ID in question: e

As shown above, a list of clauses is printed. Each clause is an
alternative (part of a requirement) that the resolver could not
fulfill or resolve. Each field is explained as follows:

1. ``Packages selected``: This is a list of packages found in order to
   resolve previous requirements before the "problem" clause was
   encountered.
2. ``Packages already present``: Packages which were given to Degasolv
   using the `present package`_ option. If none were specified,
   this will show as ``None``.
3. ``Alternative being considered``: This field displays what
   alternative from the requirement was being currently considered
   when the problem was encountered.
4. The next field gives a reason for the problem.
5. ``Package ID in question``: This field displays the package searched for
   when the problem was encountered.

Explanation of Options for ``resolve-locations``
++++++++++++++++++++++++++++++++++++++++++++++++

.. _enable-alternatives:

Enable the Use of Alternatives
******************************

+-----------------------------+---------------------------------------+
| Short option                | ``-a``                                |
+-----------------------------+---------------------------------------+
| Long option                 | ``--enable-alternatives``             |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:alternatives true``                |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"alternatives": true,``             |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.5.0                                 |
+-----------------------------+---------------------------------------+

Consider all `alternatives`_ encountered while resolving dependencies.
This is the default behavior. It allows the developers and packagers
to decide whether or not to use alternatives. As alternatives are
generally expensive to resolve, packagers should of course use them
with caution.  If this option occurs together with the
``--disable-alternatives`` option on a command line, the last argument
of the two specified wins.

.. _disable-alternatives:

Disable the Use of Alternatives
*******************************

+-----------------------------+---------------------------------------+
| Short option                | ``-A``                                |
+-----------------------------+---------------------------------------+
| Long option                 | ``--disable-alternatives``            |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:alternatives false``               |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"alternatives": false,``            |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.5.0                                 |
+-----------------------------+---------------------------------------+

Consider only the first of any given set of `alternatives`_ for any
particular requirement while resolving dependencies.  It allows the package
consumer to debug dependency resolution issues. This is especially useful
when alternatives are used frequently in specifying requirements by
packagers, thus causing performance issues on the part of the package
consumers; or, when trying to figure out why dependencies won't resolve
properly.  If this option occurs together with the ``--enable-alternatives``
option on a command line, the last argument of the two specified wins.

.. note::

   Use of this option defeats the purpose of Degasolv supporting alternatives
   in the first place. This option is intended generally for use
   when debugging a build. If it *is* used routinely, it should be used
   `site-wide`_.

Specify Solution Search Strategy
********************************

+-----------------------------+---------------------------------------+
| Short option                | ``-e STRAT``                          |
+-----------------------------+---------------------------------------+
| Long option                 | ``--search-strat STRAT``              |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:search-strat "STRAT"``             |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"search-strat": "STRAT",``          |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.8.0                                 |
+-----------------------------+---------------------------------------+

This option determines whether breadth first search or depth first
search is used during package resolution. Valid values are
``depth-first`` to specify depth-first search or ``breadth-first``
to specify breadth-first search. This option is set to
``breadth-first`` by default.

.. _conflict-strat:
.. _conflict strategies:

Specify Conflict Strategy
*************************

+-----------------------------+---------------------------------------+
| Short option                | ``-f STRAT``                          |
+-----------------------------+---------------------------------------+
| Long option                 | ``--conflict-strat STRAT``            |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:conflict-strat "STRAT"``           |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"conflict-strat": "STRAT",``        |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.1.0                                 |
+-----------------------------+---------------------------------------+

This option determines how encountered version conflicts will be
handled. Valid values are ``exclusive``, ``inclusive``, and
``prioritized``. The default setting is ``exclusive`` and this setting
should work for most environments.

.. note:: This option should be used with care, since whatever setting is
   used will greatly alter behavior. It is therefore recommended that
   whichever setting is chosen should be used `site-wide`_ within an
   organization.

- If set to ``exclusive``, all dependencies and their version
  specifications must be satisfied in order for the command to
  succeed, and only one version of each package is allowed. This is
  the default option, and is the safest, though it may carry with it
  significant performance ramifications. It turns dependency
  resolution into an NP hard problem. This is normally not a problem
  since the number of dependencies at most organizations (on the
  order of hundreds) is relatively small, but it is something of which the
  reader should be aware.

- If set to ``inclusive``, all dependencies and their version specifications
  must be satisfied in order for the command to succeed, but multiple versions
  of each package are allowed to be part of the solution. To call for
  similar behavior to ruby's gem or node's npm, for example, set
  ``--conflict-strat`` to ``inclusive`` and set ``--resolve-strat``
  to ``fast``. This can be easily and cleanly specified done by using the
  ``multi-version-mode`` `option pack`_.

- If set to ``prioritized``, then the first time a package is required and
  is found at a particular version, it will be considered to fulfill the
  all other encountered requirements asking for that package. This is
  intended to mimic the behavior of java's maven package manager.

  It means that, for example, if package ``a`` at version ``1``
  requires package ``b`` at version ``1`` and also package ``c`` at
  version ``1``; and package ``c`` at version ``1`` requires package
  ``b`` at version ``2``; then the packages ``a`` at version ``1``,
  the package ``b`` at version ``1``, and the package ``c`` at
  version ``1`` will be found. Despite the fact that ``c`` needed
  ``b`` to be at version ``2``, it had already been found at version
  ``1`` and that version was assumed to fulfill all requirements asking
  for package ``b``.

  To mimic the behavior of maven, set ``--conflict-strat`` to
  ``prioritized`` and ``--resolve-strat`` to ``fast``. This can be
  easily and cleanly specified done by using the
  ``firstfound-version-mode`` `option pack`_.

.. _list-strategy:

Specify List Strategy
*********************

+-----------------------------+---------------------------------------+
| Short option                | ``-L STRAT``                          |
+-----------------------------+---------------------------------------+
| Long option                 | ``--list-strat STRAT``                |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:list-strat "STRAT"``               |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"list-strat": "STRAT",``            |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.12.0                                |
+-----------------------------+---------------------------------------+

This option determines how packages will be listed once they are resolved.
Valid values are ``as-set``, ``lazy``, and ``eager``. The default value
is ``as-set``.


When the value is ``as-set``, packages are listed in no particular order.

When the value is ``lazy`` or ``eager``, packages are listed according to
the following rules:

  1. Barring cases of circular dependency, the child dependencies of
     any package are always listed before the package they depend on.
  2. Circular dependencies are handled properly, but which dependency comes
     first is not guaranteed in all cases. In these cases the resolver
     must choose which dependency to ignore when it sees both. It choses
     to ignore the "deeper" dependency rather then the "shallower" package
     in the package resolution graph. So, for example, if package ``a`` relies
     on package ``b`` and package ``b`` relies on package ``a``, but ``a`` is
     encountered first, the dependency from ``a`` to ``b`` will be honored but
     the dependency from ``b`` to ``a`` will be ignored when deciding in what
     order to list packages.
  3. Otherwise, dependee packages will be listed in the order that the
     requirements they fulfill are listed. This means that, all things being
     equal, a package resolving one requirement of a parent package will be
     printed before a package resolving a different requirement of a
     different package listed further down in the requirements list for the
     parent package.

     For example, if a Degasolv card file called "steel" is made using the
     below config file::

       {
           :requirements ["wool", "wood", "sheep"]
       }

     When resolved, the represented package would be printed (or
     appear in the ``json`` or ``edn`` output, if `output-format`_ is
     set) in this order::

       wool==1.0 @ http://example.com/repo/wool-1.0.zip
       wood==1.0 @ http://example.com/repo/wood-1.0.zip
       sheep==1.0 @ http://example.com/repo/sheep-1.0.zip
       steel==1.0 @ http://example.com/repo/steel-1.0.zip

     It is worth noting that commandline arguments are listed in
     reverse order. Thus, generating a card file with arguments ``-r
     wool -r wood -r sheep`` would yield a list that looks like this::

       sheep==1.0 @ http://example.com/repo/sheep-1.0.zip
       wood==1.0 @ http://example.com/repo/wood-1.0.zip
       wool==1.0 @ http://example.com/repo/wool-1.0.zip
       steel==1.0 @ http://example.com/repo/steel-1.0.zip

The difference between these options is that ``lazy`` will list dependencies
as late as possible while following the above rules, while a value of ``eager``
tells Degasolv to list dependencies as early as possible while
following the above rules.

.. _enable-error-format-resolve:

Enable Error Output Format
**************************

+-----------------------------+---------------------------------------+
| Short option                | ``-g``                                |
+-----------------------------+---------------------------------------+
| Long option                 | ``--enable-error-format``             |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:error-format true``                |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"error-format": true,``             |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.12.0                                |
+-----------------------------+---------------------------------------+

This option extends the functionality of `output-format`_ to include
when errors happen as well.

Normally, when the `output-format`_ key is specified, such as to cause
Degasolv to emit JSON or EDN, this only happens if the command runs
successfully. If package resolution was unsuccessful, an error message
is printed to standard error and the program exits with non-zero
return code. If ``error-format`` is specified, then any error
information will be printed in the form of whatever `output-format`_
specifies to standard output, while still maintaining the same exit
code.

When error information is returned via JSON or EDN, the keys are the same
in the dictionary, except:

- The ``result`` key now has the value of ``unsuccessful``.
- The ``packages`` key is not present.
- A new key, ``problems``, appears in place of the ``packages`` key containing
  information describing what went wrong.

The default behavior is to have ``:error-format`` disabled; this
CLI option enables it.

.. _disable-error-format-resolve:

Disable Error Output Format
***************************

+-----------------------------+---------------------------------------+
| Short option                | ``-G``                                |
+-----------------------------+---------------------------------------+
| Long option                 | ``--disable-error-format``            |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:error-format false``               |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"error-format": false,``            |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.12.0                                |
+-----------------------------+---------------------------------------+

This option sets the ``:error-format`` flag back to ``false``, which is the
default behavior.

.. _output-format:

Specify Output Format
*********************

+-----------------------------+---------------------------------------+
| Short option                | ``-o FORMAT``                         |
+-----------------------------+---------------------------------------+
| Long option                 | ``--output-format FORMAT``            |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:output-format "FORMAT"``           |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"output-format": "FORMAT",``        |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.10.0; EDN introduced 1.11.0         |
+-----------------------------+---------------------------------------+

Specify an output format. May be ``plain``, ``edn`` or ``json``. This
output format only takes effect when the package resolution was
successful.

The default output format is ``plain``. It is a simple text format
that was designed for ease of use within bash scripts while also
being somewhat pleasant to look at.

Example output on a successful run when the format is set to ``plain``::

  c==3.5.0 @ https://example.com/repo/c-3.5.0.zip
  d==0.8.0 @ https://example.com/repo/d-0.8.0.zip
  e==1.8.0 @ https://example.com/repo/e-1.8.0.zip
  b==2.3.0 @ https://example.com/repo/b-2.3.0.zip

In the above example out, each line takes the form::

  <id>==<version> @ <location>

When the output format is JSON, the output would spit out a JSON
document containing lots of different keys and values representing
some of the internal state Degasolv had when it resolved
the packages. Among those keys will be a key called "packages", and it will
look something like this::

  {
    "command": "degasolv",
    "subcommand": "resolve-locations",
    "options": {
      "requirements": [
        "b"
      ],
      "resolve-strat": "thorough",
      "index-strat": "priority",
      "conflict-strat": "exclusive",
      "search-directory": ".",
      "package-system": "degasolv",
      "output-format": "json",
      "version-comparison": "maven",
      "index-file": "index.dsrepo",
      "repositories": [
        "./index.dsrepo"
      ],
      "search-strat": "breadth-first",
      "alternatives": true,
      "present-packages": [
        "x==0.9.0",
        "e==1.8.0"
      ],
      "card-file": "./out.dscard"
    },
    "result": "successful",
    "packages": [
      {
        "id": "d",
        "version": "0.8.0",
        "location": "https://example.com/repo/d-0.8.0.zip",
        "requirements": [
          [
            {
              "status": "present",
              "id": "e",
              "spec": [
                [
                  {
                    "relation": "greater-equal",
                    "version": "1.1.0"
                  },
                  {
                    "relation": "less-than",
                    "version": "2.0.0"
                  }
                ]
              ]
            }
          ]
        ]
      },
      {
        "id": "c",
        "version": "3.5.0",
        "location": "https://example.com/repo/c-3.5.0.zip",
        "requirements": []
      },
      {
        "id": "b",
        "version": "2.3.0",
        "location": "https://example.com/repo/b-2.3.0.zip",
        "requirements": [
          [
            {
              "status": "present",
              "id": "c",
              "spec": [
                [
                  {
                    "relation": "greater-equal",
                    "version": "3.5.0"
                  }
                ]
              ]
            }
          ],
          [
            {
              "status": "present",
              "id": "d",
              "spec": null
            }
          ]
        ]
      }
    ]
  }

If the output format is EDN, the output will be similar, except it will use
the EDN format::

  {
    :command "degasolv",
    :subcommand "resolve-locations",
    :options {
      :requirements ("a<=1.0.0"),
      :resolve-strat "thorough",
      :index-strat "priority",
      :conflict-strat "exclusive",
      :search-directory ".",
      :package-system "degasolv",
      :output-format "edn",
      :version-comparison "maven",
      :index-file "index.dsrepo",
      :repositories (
        "./index.dsrepo"
      ),
      :search-strat "breadth-first",
      :alternatives true,
      :card-file "./out.dscard"
    },
    :result :successful,
    :packages #{
      #degasolv.resolver/PackageInfo {
        :id "b",
        :version "2.3.0",
        :location "https://example.com/repo/b-2.3.0.zip",
        :requirements []
      },
      #degasolv.resolver/PackageInfo {
        :id "a",
        :version "1.0.0",
        :location "https://example.com/repo/a-1.0.0.zip",
        :requirements [
          [
            #degasolv.resolver/Requirement {
              :status :present,
              :id "b",
              :spec nil
            }
          ]
        ]
      }
    }
  }

The output, if the format is not ``plain``, will have the following
top-level keys in it:

  - ``command``: This is will be ``degasolv``.
  - ``subcommand``: This will reflect what subcommand was specified.
    In the current version, this will always be ``resolve-locations``.
  - ``options``: This shows what options were given when Degasolv was
    run. Its contents should roughly reflect the output of ``display-config``
    when run with similar options.
  - ``result``: This displays whether the run was successful or
    not. Since unsuccessful runs result in a printed error and not
    outputted JSON, this will be ``successful``. At present, to
    determine whether a run was successful, use the return code of
    Degasolv rather than this key.
  - ``packages``: This displays the list of packages and, if present,
    any additional `meta-data`_ associated with the package.

.. _present package:
.. _present-package:

Specify that a Package is Already Present
*****************************************

+-----------------------------+----------------------------------------+
| Short option                | ``-p PKG``                             |
+-----------------------------+----------------------------------------+
| Long option                 | ``--present-package PKG``              |
+-----------------------------+----------------------------------------+
| EDN Config file key         | ``:present-packages ["PKG1", ...]``    |
+-----------------------------+----------------------------------------+
| JSON Config file key        | ``"present-packages": ["PKG1", ...],`` |
+-----------------------------+----------------------------------------+
| Version introduced          | 1.4.0                                  |
+-----------------------------+----------------------------------------+

Specify a "hard present package". Specify ``PKG`` as ``<id>==<vers>``,
as in this example: ``garfield==1.0``.

Doing this tells Degasolv that a package "already exists" at a
particular version in the system or build, whatever that means. This
means that when Degasolv encounters a requirement for this package, it
will assume the package is already found and it will mark the
dependency as resolved. On the other hand, Degasolv will not try to
change or update the found package. If the version of the present
package conflicts with requirements encountered, resolution of those
requirements may fail.

This is another one of those options that is provided and, if needed,
is meant to benefit the user; however, judicious use is
recommended. If you don't know what you're doing, you probably don't
want to use this option.

For example, if this option is used to tell Degasolv that, as part of
a build, some packages have already been downloaded, Degasolv will not
recommend that those packages be upgraded. This is the "hard" in "hard
present package": If the user specifies via ``--present-package`` that
a package is already found and usable, Degasolv won't try to find a
new version for it; it assumes "you know what you're doing" and that
the package(s) in question are not to be touched.

Specify a Requirement
*********************

+-----------------------------+---------------------------------------+
| Short option                | ``-r REQ``                            |
+-----------------------------+---------------------------------------+
| Long option                 | ``--requirement REQ``                 |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:requirements ["REQ1", ...]``       |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"requirements": ["REQ1", ...],``    |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

**Required**. Resolve this requirement together with all other
requirements given.  May be specified one ore more times as a command
line option, or once as a list of strings in a configuration file. See
:ref:`Specifying a requirement` for more information.

The last requirement specified will be the first to be resolved. If
the requirements are retrieved from the config file, they are resolved
in order from first to last in the list.  If requirements are
specified both on the command line and in the configuration file, the
requirements in the configuration file are ignored.

.. _repository option:

.. _specify repositories:

Specify a Repository to Search
******************************

+-----------------------------+---------------------------------------+
| Short option                | ``-R INDEX``                          |
+-----------------------------+---------------------------------------+
| Long option                 | ``--repository INDEX``                |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:repositories ["INDEX1", ...]``     |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"repositories": ["INDEX1", ...],``  |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

**Required**. Search the repository index given by INDEX for packages
when resolving the given requirements.

When the index strategy is ``priority`` The last repository index
specified will be the first to be consulted. If the repository indices
are retrieved from the config file, they are consulted in order from
first to last in the list.  If indices are specified both on the
command line and in the configuration file, the indices in the
configuration file are ignored. See `index strategy`_ for more
information.

``INDEX`` may be a URL or a filepath pointing to a `*.dsrepo`
file. For example, index might be
`http://example.com/repo/index.dsrepo`. Both HTTP and HTTPS URLs are
supported. As of version 1.1.0, If ``INDEX`` is ``-`` (the hyphen character), Degasolv will
read standard input instead of any specific file or URL. Possible use
cases for this include downloading the index repository first via some
other tool (such as `cURL`_).  One reason users might do this is if
authentication is required to download the index, as in this example::

  curl --user username:password https://example.com/degasolv/index.dsrepo | \
      degasolv resolve-locations -R - "req"

.. _cURL: https://curl.haxx.se/

Specify a Resolution Strategy
*****************************

+-----------------------------+---------------------------------------+
| Short option                | ``-s STRAT``                          |
+-----------------------------+---------------------------------------+
| Long option                 | ``--resolve-strat STRAT``             |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:resolve-strat "STRAT"``            |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"resolve-strat": "STRAT",``         |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

This option determines which versions of a given package id are
considered when resolving the given requirements.  If set to ``fast``,
only the first available version matching the first set of
requirements on a particular package id is consulted, and it is hoped
that this version will match all subsequent requirements constraining
the versions of that id. If set to ``thorough``, all available
versions matching the requirements will be considered. The default
setting is ``thorough`` and this setting should work for most
environments.

.. note:: This option should be used with care, since whatever setting
   is used will greatly alter behavior. It is therefore recommended
   that whichever setting is chosen should be used `site-wide`_ within
   an organization.

.. _index strategy:

Specify an Index Strategy
*************************

+-----------------------------+---------------------------------------+
| Short option                | ``-S STRAT``                          |
+-----------------------------+---------------------------------------+
| Long option                 | ``--index-strat STRAT``               |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:index-strat "STRAT"``              |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"index-strat": "STRAT",``           |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

Repositories are queried by package id in order to discover what
packages are available to fulfill the given requirements. This option
determines how multiple repository indexes are queried if there are
more than one. If set to ``priority``, the first repository that
answers with a non-empty result is used, if any. Note that this is
true even if the versions don't match what is required.

For example, if ``<repo-x>`` contains a package ``a`` at version
``1.8``, and ``<repo-y>`` contains a package ``a`` at version ``1.9``,
then the following command wil fail::

  java -jar ./degasolv-<version>-standalone.jar -R <repo-x> -R <repo-y> \
      -r "a==1.9"

While, on the other hand, this command will succeed::

  java -jar ./degasolv-<version>-standalone.jar -R <repo-y> -R <repo-x> \
      -r "a==1.9"

By contrast, if ``--index-strat`` is given the STRAT of ``global``,
all versions from all repositories answering to a particular package
id will be considered. So, both of the following commands would
succeed, under the scenario presented above::

  java -jar ./degasolv-<version>-standalone.jar -S global \
      -R <repo-x> -R <repo-y> -r "a==1.9"

  java -jar ./degasolv-<version>-standalone.jar -S global \
      -R <repo-y> -R <repo-x> -r "a==1.9"

The default setting is ``priority`` and this setting should work for
most environments.

.. note:: This option should be used with care, since whatever setting
   is used will greatly alter behavior. It is therefore recommended
   that whichever setting is chosen should be used `site-wide`_ within
   an organization.

.. _package system:
.. _package-system:

Specify a Package System
************************

+-----------------------------+---------------------------------------+
| Short option                | ``-t SYS``                            |
+-----------------------------+---------------------------------------+
| Long option                 | ``--package-system SYS``              |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:package-system "SYS"``             |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"package-system": "SYS",``          |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.4.0                                 |
+-----------------------------+---------------------------------------+

Specify package system to use. By default, this
value is ``degasolv``. This causes the Degasolv ``resolve-locations``
command to behave normally.

Other available values are shown below.

**The "apt" Package System**

**Experimental**. The ``apt`` package system resolves using the APT
debian package manager.  When using this method, `specify
repositories`_ using the format::

  {binary-amd64|binary-i386} <url> <dist> <pool>

Or, in the case of naive apt repositories::

  {binary-amd64|binary-i386} <url> <relative-path>

For example, I might use the repository option like this::

  java -jar degasolv-<version>-standalone.jar resolve-locations \
      -R "binary-amd64 https://example.com/ubuntu/ /"
      -t "apt" \
      --requirement "ubuntu-desktop"

Or this::

  java -jar degasolv-<version>-standalone.jar resolve-locations \
      -R "binary-amd64 https://example.com/ubuntu/ yakkety main" \
      -R "binary-i386 https://example.com/ubuntu/ yakkety main" \
      -t "apt" \
      --requirement "ubuntu-desktop"

Degasolv does not currently support APT dependencies
between machine architectures, as in ``python:i386``. Also,
every Degasolv repo is currently architecture-specific; each
repo has an associated architecture, even if that architecture
is ``any``.

.. _subproc-pkgsys:

**The "subproc" Package System**

The ``subproc`` package system allows the user to give Degasolv
package information via a subprocess (shell-out) command. A path
to an executable on the filesystem is given via the `subproc-exe`_ option.
For each repository specified via the `repository option`_, the
subproc executable path is executed with the string given for the
repository as its only argument. The executable is expected to
print out JSON or EDN to standard output, depending on the value
of the `subproc-output-format`_ option. This information will then
be read into Degasolv and used to resolve dependencies.

If the format is JSON, which is the default, the output should be of the form::

  {
      "pkgname": [
          {
              "id": "pkgname",
              "version": "p.k.g-version",
              "location": "pkg-url",
              <optional kv-pairs associated with package>
          }
      ],
      "otherpkgname": [...]
  }

If the format is EDN, the output should be of the form::

  {
      "pkgname" [
          # The following will be referred
          {
              :id "pkgname"
              :version: "p.k.g-version"
              :location": "pkg-url"
              <optional kv-pairs associated with package>
          }
      ]
      "otherpkgname" [...]
  }

Any additional kv-pairs specified in a package's record as shown
above will appear in the resolution output if the `output-format`_
option is set to something other than ``plain``.

If the executable exits with a non-zero error status code, Degasolv
will print an error message looking like the following and also exit
with a non-zero status code::

  Error while evaluating repositories: Executable
  `<path-to-exe>` given argument
  `<repository-string>` exited with non-zero status `1`.

The resolver will search for packages in the order
given in the output of the executable. Unless you
have a good reason not to, you should list packages
under the name of the package in the data structure
on standard out in version-descending order.

.. _subproc-output-format:

Specify Subproc Package System Output Format
********************************************

+-----------------------------+----------------------------------------+
| Short option                | ``-u FORMAT``                          |
+-----------------------------+----------------------------------------+
| Long option                 | ``--subproc-output-format FORMAT``     |
+-----------------------------+----------------------------------------+
| EDN Config file key         | ``:subproc-output-format "FORMAT"``    |
+-----------------------------+----------------------------------------+
| JSON Config file key        | ``"subproc-output-format": "FORMAT",`` |
+-----------------------------+----------------------------------------+
| Version introduced          | 1.12.0                                 |
+-----------------------------+----------------------------------------+

This option only takes effect if the ``subproc`` choice was listed for
the `package-system`_ option. It says whether the executable used by Degasolv
to get information needed to resolve dependencies will come in the form of an EDN
or a JSON document. This option is set to ``json`` by default. See `package-system`_
docs for more information.

.. _version-comparison-resolve:

Specify the Version Comparison Algorithm
****************************************

+-----------------------------+---------------------------------------+
| Short option                | ``-V CMP``                            |
+-----------------------------+---------------------------------------+
| Long option                 | ``--version-comparison CMP``          |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:version-comparison "CMP"``         |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"version-comparison": "CMP",``      |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.8.0                                 |
+-----------------------------+---------------------------------------+

Use the specified version comparison algorithm when resolving
dependencies.

This option allows the operator to change what version comparison
algorithm is used. By default, the algorithm is "maven". May be
"debian", "maven", "naive", "python" (PEP 440), "rpm", "rubygem", or
"semver" (2.0.0). Version comparison algorithms are taken from the
Serovers library. Descriptions for these algorithms can be found in
the `Serovers docs`_.

.. _Serovers docs: http://djhaskin987.gitlab.io/serovers/serovers.core.html

.. caution:: This is one of those options that should not be used
           unless the operator has a good reason, but it is
           available and usable if needed.

.. note:: This option should be used with care, since whatever setting
   is used will greatly alter behavior. Similar options are availabe
   for the ``generate-repo-index`` subcommand and the ``query-repo``
   subcommand. They should all agree when used within the same
   site. It is therefore recommended that whichever setting is
   chosen should be used `site-wide`_ within an organization.

.. _subproc-exe:

Specify Subproc Package System Output Format
********************************************

+-----------------------------+---------------------------------------+
| Short option                | ``-x PATH``                           |
+-----------------------------+---------------------------------------+
| Long option                 | ``--subproc-exe PATH``                |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:subproc-exe "PATH"``               |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"subproc-exe": "PATH",``            |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.12.0                                |
+-----------------------------+---------------------------------------+

This option only takes effect if the ``subproc`` choice was listed for
the `package-system`_ option; however, it is required if the
``subproc`` choice was listed. It lists the path to the executable to
use to get resolution information. See `package-system`_ docs for more
information.

.. _query-repo:

CLI for ``query-repo``
----------------------

Usage Page for ``query-repo``
+++++++++++++++++++++++++++++

Running ``java -jar degasolv-<version>-standalone.jar query-repo -h`` returns a
page that looks something like this::

  Usage: degasolv <options> query-repo <query-repo-options>

  Options are shown below. Default values are marked as <DEFAULT> and
    descriptions. Options marked with `**` may be
    used more than once.

    -g, --enable-error-format               Enable output format for errors
    -G, --disable-error-format              Disable output format for errors (default)
    -q, --query QUERY                       Display packages matching query string.
    -R, --repository INDEX                  Search INDEX for packages. **
    -S, --index-strat STRAT       priority  May be 'priority' or 'global'.
    -t, --package-system SYS      degasolv  May be 'degasolv' or 'apt'.
    -V, --version-comparison CMP  maven     May be 'debian', 'maven', 'naive', 'python', 'rpm', 'rubygem', or 'semver'.
    -h, --help                              Print this help page

  The following options are required for subcommand `query-repo`:

    - `-R`, `--repository`, or the config file key `:repositories`.
    - `-q`, `--query`, or the config file key `:query`.

Overview of ``query-repo``
++++++++++++++++++++++++++

*This subcommand introduced as of version 1.0.2*.

This subcommand queries a repository index or indices for
packages. This comand is intended to be useful or debugging dependency
problems.

Explanation of Options for ``query-repo``
+++++++++++++++++++++++++++++++++++++++++

.. _enable-error-format-query:

Enable Error Output Format
**************************

+-----------------------------+---------------------------------------+
| Short option                | ``-g``                                |
+-----------------------------+---------------------------------------+
| Long option                 | ``--enable-error-format``             |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:error-format true``                |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"error-format": true,``             |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.12.0                                |
+-----------------------------+---------------------------------------+

This option extends the functionality of `output-format`_ to include
when errors happen as well.

Normally, when the `output-format`_ key is specified, such as to cause
Degasolv to emit JSON or EDN, this only happens if the command runs
successfully. If querying thre repo was unsuccessful, an error message
is printed to standard error and the program exits with non-zero
return code. If ``error-format`` is specified, then any error
information will be printed in the form of whatever `output-format`_
specifies to standard output, while still maintaining the same exit
code.

When error information is returned via JSON or EDN, the keys are the same
in the dictionary, except:

- The ``result`` key now has the value of ``unsuccessful``.

- The ``packages`` key is not present.

- A new key, ``problems``, appears in place of the ``packages`` key containing
  information describing what went wrong.

The default behavior is to have ``:error-format`` disabled; this
CLI option enables it.

.. _disable-error-format-query:

Disable Error Output Format
***************************

+-----------------------------+---------------------------------------+
| Short option                | ``-G``                                |
+-----------------------------+---------------------------------------+
| Long option                 | ``--disable-error-format``            |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:error-format false``               |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"error-format": false,``            |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.12.0                                |
+-----------------------------+---------------------------------------+

This option sets the ``:error-format`` flag back to ``false``, which is the
default behavior.

.. _output-format-query-repo:

Specify Output Format
*********************

+-----------------------------+---------------------------------------+
| Short option                | ``-o FORMAT``                         |
+-----------------------------+---------------------------------------+
| Long option                 | ``--output-format FORMAT``            |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:output-format "FORMAT"``           |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"output-format": "FORMAT"``         |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.11.0                                |
+-----------------------------+---------------------------------------+

Specify an output format. May be ``plain``, ``edn`` or ``json``. By
default the output format is ``plain``. This output format only takes
effect when the query returns a non-empty set of results. This is
exactly like the `output-format`_ option for `resolve-locations`_,
except that the ``subcommand`` field is new returned as
``query-repo``.

Specify Query
*************

+-----------------------------+---------------------------------------+
| Short option                | ``-q QUERY``                          |
+-----------------------------+---------------------------------------+
| Long option                 | ``--query QUERY``                     |
+-----------------------------+---------------------------------------+
| Config file key             | N/A                                   |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

**Required**. Query repository index or indices for a package. Syntax
is exactly the same as requirements except that only one alternative
may be specified (that is, using the ``|`` character or specifying
multiple package ids), and the requirement must specify a present
package (no ``!`` character may be used either).  See `Specifying a
requirement`_ for more information.

Examples of valid queries:

  - ``"pkg"``
  - ``"pkg!=3.0.0"``

Examples if invalid queries:

  - ``"a|b"``
  - ``"!a"``

Specify a Repository to Search
******************************

+-----------------------------+---------------------------------------+
| Short option                | ``-R INDEX``                          |
+-----------------------------+---------------------------------------+
| Long option                 | ``--repository INDEX``                |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:repositories ["INDEX1", ...]``     |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"repositories": ["INDEX1", ...],``  |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

**Required** This option works exactly the same as the `repository
option`_ for the ``resolve-locations`` command, except that instead of
using the repositories for resolving requirements, it uses them for
simple index queries. See that option's explanation for more
information.

Specify an Index Strategy
*************************

+-----------------------------+---------------------------------------+
| Short option                | ``-S STRAT``                          |
+-----------------------------+---------------------------------------+
| Long option                 | ``--index-strat STRAT``               |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:index-strat "STRAT"``              |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"index-strat": "STRAT",``           |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

This option works exactly the same as the `index strategy`_ option for the
``resolve-locations`` command, except that it is used for simple index
queries. See that option's explanation for more information.

Specify a Package System (Experimental)
***************************************

+--------------+---------------------------+-----------------------------------+
| Short option | Long option               | Config File Key                   |
+--------------+---------------------------+-----------------------------------+
| ``-t SYS``   | ``--package-system SYS``  | ``:package-system "SYS"``         |
+--------------+---------------------------+-----------------------------------+

This option works exactly the same as the `package system`_ option for
the ``resolve-locations`` command, except that it is used for simple
index queries. See that option's explanation for more information.

.. _version-comparison-query:

Specify the Version Comparison Algorithm
****************************************

+-----------------------------+---------------------------------------+
| Short option                | ``-V CMP``                            |
+-----------------------------+---------------------------------------+
| Long option                 | ``--version-comparison CMP``          |
+-----------------------------+---------------------------------------+
| EDN Config file key         | ``:version-comparison "CMP"``         |
+-----------------------------+---------------------------------------+
| JSON Config file key        | ``"version-comparison": "CMP",``      |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.8.0                                 |
+-----------------------------+---------------------------------------+

Use the specified version comparison algorithm when querying the
repository.

This option allows the operator to change what version comparison
algorithm is used. By default, the algorithm is "maven". May be
"debian", "maven", "naive", "python" (PEP 440), "rpm", "rubygem", or
"semver" (2.0.0). Version comparison algorithms are taken from the
Serovers library. Descriptions for these algorithms can be found in
the `Serovers docs`_.

.. _Serovers docs: http://djhaskin987.gitlab.io/serovers/serovers.core.html

.. caution:: This is one of those options that should not be used
           unless the operator has a good reason, but it is available
           and usable if needed.

.. note:: This option should be used with care, since whatever setting
   is used will greatly alter behavior. Similar options are availabe
   for the ``generate-repo-index`` subcommand and the
   ``resolve-locations`` subcommand. They should all agree when used
   within the same site. It is therefore recommended that whichever
   setting is chosen should be used `site-wide`_ within an
   organization.

.. _Specifying a requirement:

Specifying a requirement
------------------------

.. _alternative:
.. _alternatives:

*Unless otherwise noted, features in this section were introduced as
of version 1.0.2 or earlier*.

A requirement is given as a string of text. A requirement consists of
one or more *alternatives*. Any of the alternatives will satisfy the
requirement. Alternatives are specified by a bar character (``|``),
like this::

  "<alt1>|<alt2>|<alt3>"

Or, more concretely::

  "hickory|maple|oak"

Alternatives will be considered in order of appearance.

.. caution:: In general, specifying more than one alternative is
             mostly unecessary, and should generally be avoided. This
             is because specifying too many alternatives tends to
             impact performance significantly; but they are available
             and usable if needed.

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

For example, this expression::

  "spruce>=1.0.0,<2.0.0;>=3.0.0,<4.0.0"

Is interpreted as::

  "spruce at version ((>=1.0.0 AND <2.0.0) OR (>=3.0.0 AND <4.0.0))"

.. _matches:
.. _in-range:
.. _pess-greater:

Comparison Operators
++++++++++++++++++++

Each version predicate is composed of a comparison operator and a valid version
against which to compare a package's version. The character sequences ``<``,
``<=``, ``!=``, ``==``, ``>=``, and ``>`` represent the comparisons "older
than", "older than or equal to", "not equal to", "equal to", "newer than or
equal to", and "newer than", respectively, using whatever version comparison
algorithm was specified using the CLI, or using the maven version comparison
algorithm by default.

In addition to the above operators, three other version spec operators are
provided:

  * The "matches" operator: ``<>``. *Introduced of version
    1.8.0*. This operator is given in a version spec as
    ``<>REGEX``. The version of any package found during the
    resolution process must match the given `java regular
    expression`_. Examples:

      * The expression ``<>\d+\.\d+\.\d+`` matches any version containing a
        three-part version in it.

      * The expression ``<>f[ea]{2}ture`` matches any version
        containing the strings "feature", "faeture", "feeture" or
        "faature".

    .. _java regular expression: http://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html

  * The "in-range" operator: ``=>``. *Introduced as of version
    1.8.0*. This operator is given in a version spec
    as ``=>RANGE``. The version of any package found during the resolution
    process must be in the given version range. Examples:

      * The expression ``=>3.x`` matches the versions ``3.0.0``, ``3.0.0.0``
        and ``3.0`` but not ``4.0`` or higher.
      * The expression ``=>3.3.x`` matches the versions ``3.3.0``, ``3.3.8``
        and ``3.3.8.99999`` but not ``3.4.0``.

    Ranges are calculated in the following way:

      * Any non-digit characters found on the end of the ``RANGE`` string are
        removed.

      * All digit characters found on the end of the ``RANGE`` string are
        converted into a number and incremented. The incremented number
        is then put back into the version string, replacing any digit
        characters that were at the end of the string before. So,
        ``3.x`` becomes ``4``, ``3.`` becomes ``4``, and ``2ormore``
        becomes ``3``.

      * Finally, any versions comparing greater than or equal to the
        original ``RANGE`` string, but less than the incremented
        version string as computed in the previous step, are
        considered for dependency resolution.

  * The "pessimistic greater-than" operator: ``><``. *Introduced as of
    version 1.9.0*. This operator is given in a version spec as
    ``><VERS``. The version of any package found during the resolution
    process must be greater or equal to the given version but less
    than the next major version. Examples:

      * The expression ``><3.2.1`` matches the versions ``3.2.1``, ``3.4.3``
        but not ``4.0.0`` or higher, nor does it match ``3.2.0``.
      * The expression ``><3.3.3`` matches the versions ``3.3.3``, ``3.3.8``
        and ``3.9.8`` but not ``4.0.0``.

    "The next major version" is calculated similarly to how ranges are
    calculated:

      * The first found set of digit characters found in the ``VERS``
        string are converted into a number and incremented. The
        remainder of the version string after the incremented number
        is discarded.
      * Any versions comparing greater than or equal to the
        original ``VERS`` string, but less this new "incremented"
        version string as computed in the previous step, are
        considered for dependency resolution.

Examples
++++++++

The following are examples of valid alternatives, together with their english
interpretations:

+------------------------------+----------------------------------------------+
| Alternative                  | English Interpretation                       |
+==============================+==============================================+
| ``"oak"``                    | Find package ``oak``                         |
+------------------------------+----------------------------------------------+
| ``"pine>1.0"``               | Find package ``pine`` of version newer than  |
|                              | ``1.0``                                      |
+------------------------------+----------------------------------------------+
| ``"pine><3.4.1-alpha8"``     | Find package ``pine`` of version newer than  |
|                              | or equal to ``3.4.1-alpha8`` but less than   |
|                              | ``4``.                                       |
+------------------------------+----------------------------------------------+
| ``"fir<>\\d+\\.8"``          | Find package ``fir`` containing "<digits>.8" |
|                              | somewhere in the version string              |
+------------------------------+----------------------------------------------+
| ``"cedar=>3.x"``             | Find package ``cedar`` at version greater    |
|                              | or equal to major component ``3`` but less   |
|                              | than ``4``                                   |
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

.. note:: To make debugging easier, try to keep things as simple as
   possible. Try not to make requirement strings very long. When using
   the ``inclusive`` or ``priority`` `conflict strategies`_, it is
   recommended to specify exact package names and versions, like this:
   ``pkgname==1.0.0``. The simpler the requirement string, the easier
   it will be to untangle any untoward dependency problems.

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
| Requirement             | Explanation                                       |
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
