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

- The first version of degasolv (for the purposes of this guide)
  released was 1.0.2 .

- As of version 1.3.0, All options which take a file name may now have
  ``-`` given as the filename, to specify that standard in should be
  used.

**The earliest usable released version of degasolv that can be
 recommended for use is 1.5.1**. Anything before that wasn't profiled,
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
    -k, --option-pack PACK                  Specify option pack **
    -h, --help                              Print this help page

  Commands are:

    - display-config
    - generate-card
    - generate-repo-index
    - resolve-locations
    - query-repo

  Simply run `degasolv <command> -h` for help information.

Explanation of Options
++++++++++++++++++++++

Using Configuration Files
*************************

Basic Configuration Usage
#########################

+-----------------------------+---------------------------------------+
| Short option                | ``-c FILE``                           |
+-----------------------------+---------------------------------------+
| Long option                 | ``--config-file FILE``                |
+-----------------------------+---------------------------------------+
| Config file key             | N/A                                   |
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

The config file may be a URL or a filepath. Both HTTP and HTTPS URLs are
supported. If the config file is ``-`` (the hyphen character), degasolv
will read standard input instead of any specific file or
URL.

Using Multiple Configuration Files
##################################

As of version 1.2.0, the ``--config-file`` option may be specified multiple
times. Each configuration file specified will get its configuration
merged into the previously specified configuration files. If both
configuration files contain the same option, the option specified in
the latter specified configuration file will be used.

.. _config files section:

As an example, consider the following `display-config command`_::

  java -jar degasolv-<version>-standalone.jar \
    --config-file "$PWD/a.edn" \
    --config-file "$PWD/b.edn" \
    display-config

If this is the contents of the file ``a.edn``::

  {
      :index-strat "priority"
      :repositories ["https://example.com/repo1/"]
      :id "a"
      :version "1.0.0"
  }

And this were the contents of ``b.edn``::

  {
      :conflict-strat "exclusive"
      :repositories ["https://example.com/repo2/"]
      :id "b"
      :version "2.0.0"
  }

Then the output of the above command would look like this::

  {
      :index-strat "priority",
      :repositories ["https://example.com/repo2/"],
      :id "b",
      :version "2.0.0",
      :conflict-strat "exclusive",
      :arguments ["display-config"]
  }

.. _site-wide:

Using Site-Wide Configuration Files
###################################

The merging of config files, together with the interesting
fact that config files may be specified via HTTP/HTTPS URLs,
allows the user to specify a *site config file*.

Multiple sub-commands have options ending in ``-strat`` which
fundamentally change how degasolv works. These are
``--conflict-strat``, ``--index-strat``, ``--resolve-strat`` and
``--search-strat``. It is therefore recommended that they are
specified site-wide.  Specifying these in a site config file, then
serving that config file internally via HTTP(S) would allow all
instances of degasolv to point to a site-wide file, together with a
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
| Config file key             | ``:option-packs ["PACK1",...]``       |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.7.0                                 |
+-----------------------------+---------------------------------------+

Specify one or more option packs.

Degasolv ships with several "option packs", each of which imply
several degasolv options at once. When an option pack is specified,
degasolv looks up which option pack is used and what options are
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

+------------------+------------------------+---------------------------------+
| Short option     | Long option            | Config File Key                 |
+------------------+------------------------+---------------------------------+
| ``-h``           | ``--help``             | N/A                             |
+------------------+------------------------+---------------------------------+

``-h``, ``--help``: Prints the help page. This can be used on every
sub-command as well.

.. _EDN format: https://github.com/edn-format/edn

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

*Introduced as of version 1.6.0*. The ``display-config`` command is
used to print all the options in the *effective configuration*. It
allows the user to debug configuration by printing the actual
configuration used by degasolv after all the command-line arguments
and config files have been merged together. An example of this is
found in the `config files section`_.

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

*Introduced as of version 1.0.2*. This subcommand is used to generate
a card file. This card file is used to represent a package within a
degasolv repository. It is placed in a directory with other card
files, and then the ``generate-repo-index`` command is used to search
that directory for card files to produce a repository index.

Explanation of Options for ``generate-card``
++++++++++++++++++++++++++++++++++++++++++++

Specify Location of the Card File
*********************************

+-----------------------------+---------------------------------------+
| Short option                | ``-C FILE``                           |
+-----------------------------+---------------------------------------+
| Long option                 | ``--card-file FILE``                  |
+-----------------------------+---------------------------------------+
| Config file key             | ``:card-file "FILE"``                 |
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
| Config file key             | ``:id "ID"``                          |
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
| Config file key             | ``:location "LOCATION"``              |
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
| Config file key             | ``:meta {:key1 "value1" ...}``        |
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
top of degasolv. For example, now the operator may store the sha256
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
| Config file key             | ``:requirements ["REQ1", ...]``       |
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
| Config file key             | ``:version "VERSION"``                |
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
| Config file key             | N/A                                   |
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

*Introduced as of version 1.0.2*. This subcommand is used to generate
a repository index file. A repository index file lists all versions of
all packages in a particular degasolv repository, together with their
locations. This file's location, whether by file path or URL, would
then be given to ``resolve-locations`` and ``query-repo`` commands as
degasolv repositories.

Explanation of Options for ``generate-repo-index``
++++++++++++++++++++++++++++++++++++++++++++++++++

Specify the Repo Search Directory
*********************************

+-----------------------------+---------------------------------------+
| Short option                | ``-d DIR``                            |
+-----------------------------+---------------------------------------+
| Long option                 | ``--search-directory DIR``            |
+-----------------------------+---------------------------------------+
| Config file key             | ``:search-directory "DIR"``           |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.0.2                                 |
+-----------------------------+---------------------------------------+

Look for degasolv card files in this directory. The directory will be
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
| Config file key             | ``:index-file "FILE"``                |
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
| Config file key             | ``:version-comparison "CMP"``         |
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
| Config file key             | ``:add-to "INDEX"``                   |
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
supported. If ``INDEX`` is ``-`` (the hyphen character), degasolv will
read standard input instead of any specific file or URL.

.. _resolve-locations:

CLI for ``resolve-locations``
-----------------------------

Usage Page for ``resolve-locations``
++++++++++++++++++++++++++++++++++++

Running ``java -jar degasolv-<version>-standalone.jar resolve-locations -h``
returns a page that looks something like this::

    Usage: degasolv <options> resolve-locations <resolve-locations-options>

    Options are shown below. Default values are marked as <DEFAULT> and
      descriptions. Options marked with `**` may be
      used more than once.

      -a, --enable-alternatives                    Consider all alternatives (default)
      -A, --disable-alternatives                   Consider only first alternatives
      -e, --search-strat STRAT      breadth-first  May be 'breadth-first' or 'depth-first'.
      -f, --conflict-strat STRAT    exclusive      May be 'exclusive', 'inclusive' or 'prioritized'.
      -o, --output-format FORMAT    plain          May be 'plain' or 'json'
      -p, --present-package PKG                    Hard present package. **
      -r, --requirement REQ                        Resolve req. **
      -R, --repository INDEX                       Search INDEX for packages. **
      -s, --resolve-strat STRAT     thorough       May be 'fast' or 'thorough'.
      -S, --index-strat STRAT       priority       May be 'priority' or 'global'.
      -t, --package-system SYS      degasolv       May be 'degasolv' or 'apt'.
      -V, --version-comparison CMP  maven          May be 'debian', 'maven', 'naive', 'python', 'rpm', 'rubygem', or 'semver'.
      -h, --help                                   Print this help page

    The following options are required for subcommand `resolve-locations`:

      1. `-R`, `--repository`, or the config file key `:repositories`.
      2. `-r`, `--requirement`, or the config file key `:requirements`.

Overview of ``resolve-locations``
+++++++++++++++++++++++++++++++++

*Introduced as of version 1.0.2*. The ``resolve-locations`` command
searches one or more repository index files, and uses the package
information in them to attempt to resolve the requirements given at
the command line. If successful, it exits with a return code of 0 and
outputs the name of each package in the solution it has found,
together with that package's location.

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
2. ``Packages already present``: Packages which were given to degasolv
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
| Config file key             | ``:alternatives true``                |
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
| Config file key             | ``:alternatives false``               |
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

   Use of this option defeats the purpose of degasolv supporting alternatives
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
| Config file key             | ``:search-strat "STRAT"``             |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.8.0                                 |
+-----------------------------+---------------------------------------+

This option determines whether breadth first search or depth first
search is used during package resolution. Valid values are
``depth-first`` to specify depth-first search or ``breadth-first``
to specify breadth-first search. This option is set to
``breadth-first`` by default.

.. _conflict strategies:

Specify Conflict Strategy
*************************

+-----------------------------+---------------------------------------+
| Short option                | ``-f STRAT``                          |
+-----------------------------+---------------------------------------+
| Long option                 | ``--conflict-strat STRAT``            |
+-----------------------------+---------------------------------------+
| Config file key             | ``:conflict-strat "STRAT"``           |
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

.. _output-format:

Specify Output Format
*********************

+-----------------------------+---------------------------------------+
| Short option                | ``-o FORMAT``                         |
+-----------------------------+---------------------------------------+
| Long option                 | ``--output-format FORMAT``            |
+-----------------------------+---------------------------------------+
| Config file key             | ``:output-format "FORMAT"``           |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.10.0                                |
+-----------------------------+---------------------------------------+

Specify an output format. May be ``plain`` or ``json``. This output
format only takes effect when the package resolution was successful.

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
some of the internal state degasolv had when it resolved
the packages. Among those keys will be a key called "packages", and it will
look something like this::

  {
      ...,
      "packages": [
          {
              "name": "c",
              "version": "3.5.0",
              "location": "https://example.com/repo/c-3.5.0.zip"
          },
          {
              "name": "d",
              "version": "0.8.0",
              "location": "https://example.com/repo/d-0.8.0.zip"
          },
          {
              "name": "e",
              "version": "1.8.0",
              "location:" "https://example.com/repo/e-1.8.0.zip"
          },
          {
              "name": "b",
              "version": "2.3.0",
              "location:" "https://example.com/repo/b-2.3.0.zip"
          }, ...
      ], ...
  }

.. _present package:
.. _present-package:

Specify that a Package is Already Present
*****************************************

+-----------------------------+---------------------------------------+
| Short option                | ``-p PKG``                            |
+-----------------------------+---------------------------------------+
| Long option                 | ``--present-package PKG``             |
+-----------------------------+---------------------------------------+
| Config file key             | ``:present-packages ["PKG1", ...]``   |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.4.0                                 |
+-----------------------------+---------------------------------------+

Specify a "hard present package". Specify ``PKG`` as ``<id>==<vers>``,
as in this example: ``garfield==1.0``.

Doing this tells degasolv that a package "already exists" at a
particular version in the system or build, whatever that means. This
means that when degasolv encounters a requirement for this package, it
will assume the package is already found and it will mark the
dependency as resolved. On the other hand, degasolv will not try to
change or update the found package. If the version of the present
package conflicts with requirements encountered, resolution of those
requirements may fail.

This is another one of those options that is provided and, if needed,
is meant to benefit the user; however, judicious use is
recommended. If you don't know what you're doing, you probably don't
want to use this option.

For example, if this option is used to tell degasolv that, as part of
a build, some packages have already been downloaded, degasolv will not
recommend that those packages be upgraded. This is the "hard" in "hard
present package": If the user specifies via ``--present-package`` that
a package is already found and usable, degasolv won't try to find a
new version for it; it assumes "you know what you're doing" and that
the package(s) in question are not to be touched.

Specify a Requirement
*********************

+-----------------------------+---------------------------------------+
| Short option                | ``-r REQ``                            |
+-----------------------------+---------------------------------------+
| Long option                 | ``--requirement REQ``                 |
+-----------------------------+---------------------------------------+
| Config file key             | ``:requirements ["REQ1", ...]``       |
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
| Config file key             | ``:repositories ["INDEX1", ...]``     |
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
supported. If ``INDEX`` is ``-`` (the hyphen character), degasolv will
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
| Config file key             | ``:resolve-strat "STRAT"``            |
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
| Config file key             | ``:index-strat "STRAT"``              |
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

Specify a Package System (Experimental)
***************************************

+-----------------------------+---------------------------------------+
| Short option                | ``-t SYS``                            |
+-----------------------------+---------------------------------------+
| Long option                 | ``--package-system SYS``              |
+-----------------------------+---------------------------------------+
| Config file key             | ``:package-system "SYS"``             |
+-----------------------------+---------------------------------------+
| Version introduced          | 1.4.0                                 |
+-----------------------------+---------------------------------------+

**Experimental**. Specify package system to use. By default, this
value is ``degasolv``. Using this option allows the user to run
degasolv's resolver engine on respositories from other package manager
systems. Though option was mainly implemented for profiling and
debugging purposes, it is envisioned that this option will expand to
include many package manager repositories. This will allow users to
use degasolv to resolve packages from well-known sources, in a
reliable and useful manner.

Other available values are:

  - ``apt``: resolve using the APT debian package manager. When using
    this method, `specify repositories`_ using the format::

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

    .. note:: Degasolv does not currently support APT dependencies
       between machine architectures, as in ``python:i386``. Also,
       every degasolv repo is currently architecture-specific; each
       repo has an associated architecture, even if that architecture
       is ``any``.

Specify the Version Comparison Algorithm
****************************************

+-----------------------------+---------------------------------------+
| Short option                | ``-V CMP``                            |
+-----------------------------+---------------------------------------+
| Long option                 | ``--version-comparison CMP``          |
+-----------------------------+---------------------------------------+
| Config file key             | ``:version-comparison "CMP"``         |
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

.. _query-repo-options:

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

*Introduced as of version 1.0.2*. This subcommand queries a repository
index or indices for packages. This comand is intended to be useful or
debugging dependency problems.

Explanation of Options for ``query-repo``
+++++++++++++++++++++++++++++++++++++++++

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
| Config file key             | ``:repositories ["INDEX1", ...]``     |
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
| Config file key             | ``:index-strat "STRAT"``              |
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

Specify the Version Comparison Algorithm
****************************************

+-----------------------------+---------------------------------------+
| Short option                | ``-V CMP``                            |
+-----------------------------+---------------------------------------+
| Long option                 | ``--version-comparison CMP``          |
+-----------------------------+---------------------------------------+
| Config file key             | ``:version-comparison "CMP"``         |
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
of version 1.0.2 or earlier*. A requirement is given as a string of
text. A requirement consists of one or more *alternatives*. Any of the
alternatives will satisfy the requirement. Alternatives are specified
by a bar character (``|``), like this::

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
