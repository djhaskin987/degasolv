Degasolv Command Reference
==========================

This article describes the Degasolv CLI, what subcommands and options
there are, and what they are for.

Top-Level CLI
-------------

Running ``java -jar degasolv-<version>-standalone.jar -h`` will yield
a page that looks something like this::

  Usage: degasolv <options> <command> <<command>-options>

  Options are shown below, with their default values and
    descriptions. Options marked with `**` may be
    used more than once.

    -c, --config-file FILE  ./degasolv.edn  config file
    -h, --help                              Print this help page

  Commands are:

    - display-config
    - generate-card
    - generate-repo-index
    - resolve-locations
    - query-repo

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

  A few notable exceptions to this rule is the ``--repository`` and
  ``--present-package`` options of the ``resolve-locations`` and ``query-repo``
  commands, and the ``--requirement`` option of the ``generate-card`` and
  ``resolve-locations`` commands. This is because these options can be
  specified multiple times, and so their configuration file key equivalents are
  named ``:repositories``, ``:present-packages`` and ``:requirements``
  respectively, and they show up in the configuration file as a list of
  strings. Finally, the ``--enable-alternatives`` and
  ``--disable-alternatives`` options of the ``resolve-locations`` command map a
  boolean value to the ``alternatives`` config file key. So, instead of using
  this command::

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

  As of version 1.2.0, the ``--config-file`` option may be specified multiple
  times. Each config file specified will get its configuration
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

  The merging of config files, together with the interesting
  fact that config files may be specified via HTTP/HTTPS URLs,
  allows the user to specify a *site config file*.

  Many options, such as ``--index-strat``, ``--conflict-strat``,
  and ``--resolve-strat`` fundamentally change how degasolv
  works, and so it is recommended that they are specified site-wide.
  Specifying these in a site config file, then serving that config
  file internally via HTTP(S) would allow all instances of degasolv
  to point to a site-wide file, together with a build-specific config
  file, as in this example::

    java -jar degasolv-<version>-standalone.jar \n
        --config-file "https://nas.example.com/degasolv/site.edn" \
        --config-file "./degasolv.edn" \
        generate-card

- ``-h``, ``--help``: Prints the help page. This can be used on every
  sub-command as well.

.. _EDN format: https://github.com/edn-format/edn

.. _display-config command:

CLI for ``display-config``
--------------------------

Running ``java -jar degasolv-<version>-standalone.jar display-config -h``
returns a page that looks something like this::

  Usage: degasolv <options> display-config <display-config-options>

  Options are shown below, with their default values and
    descriptions:

    -h, --help  Print this help page

The ``display-config`` command is used to print all the options
in the "effective configuration". It allows the user to debug
configuration by printing the actual configuration used by degasolv
after all the command-line arguments and config files have
been merged together. An example of this is found in the
`config files section`_.

CLI for ``generate-card``
-------------------------

Running ``java -jar degasolv-<version>-standalone.jar generate-card -h``
returns a page that looks something like this::

  Usage: degasolv <options> generate-card <generate-card-options>

  Options are shown below, with their default values and
    descriptions. Options marked with `**` may be
    used more than once.

    -i, --id true                        ID (name) of the package
    -v, --version true                   Version of the package
    -l, --location true                  URL or filepath of the package
    -r, --requirement REQ                List req, may be used multiple times
    -C, --card-file FILE   ./out.dscard  The name of the card file
    -h, --help                           Print this help page

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

Running ``java -jar degasolv-<version>-standalone.jar generate-repo-index -h``
returns a page that looks something like this::


  Usage: degasolv <options> generate-repo-index <generate-repo-index-options>

  Options are shown below, with their default values and
    descriptions. Options marked with `**` may be
    used more than once.

    -d, --search-directory DIR  .             Find degasolv cards here
    -I, --index-file FILE       index.dsrepo  The name of the repo file
    -a, --add-to INDEX                        Add to repo index INDEX
    -h, --help                                Print this help page

This subcommand is used to generate a repository index file. A
repository index file lists all versions of all packages in a
particular degasolv repository, together with their locations. This
file's location, whether by file path or URL, would then be given to
``resolve-locations`` and ``query-repo`` commands as degasolv
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

  ``INDEX`` may be a URL or a filepath. Both HTTP and HTTPS URLs are
  supported. If ``INDEX`` is ``-`` (the hyphen character), degasolv
  will read standard input instead of any specific file or
  URL.

CLI for ``resolve-locations``
-----------------------------

Running ``java -jar degasolv-<version>-standalone.jar resolve-locations -h``
returns a page that looks something like this::

  Usage: degasolv <options> resolve-locations <resolve-locations-options>

  Options are shown below, with their default values and
    descriptions. Options marked with `**` may be
    used more than once.

    -a, --enable-alternatives              Consider all alternatives
    -A, --disable-alternatives             Consider only first alternatives
    -f, --conflict-strat STRAT  exclusive  May be 'exclusive', 'inclusive' or 'prioritized'.
    -p, --present-package PKG              Hard present package. **
    -r, --requirement REQ                  Resolve req. **
    -R, --repository INDEX                 Search INDEX for packages. **
    -s, --resolve-strat STRAT   thorough   May be 'fast' or 'thorough'.
    -S, --index-strat STRAT     priority   May be 'priority' or 'global'.
    -t, --package-system SYS    degasolv   May be 'degasolv' or 'apt'.
    -h, --help                             Print this help page

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

    <id>==<version> @ <location>

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

Explanation of options:

- ``-a``, ``--enable-alternatives``, ``:alternatives true``:
  Consider all `alternatives`_ encountered while resolving dependencies.
  This is the default behavior. It allows the developers and packagers
  to decide whether or not to use alternatives. As alternatives are generally
  expensive to resolve, packagers should of course use them with caution.
  If this option occurs together with the ``--disable-alternatives`` option
  on a command line, the last argument of the two specified wins.


- ``-A``, ``--disable-alternatives``, ``:alternatives false``:
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

.. _conflict strategies:

- ``-f STRAT``, ``--conflict-strat STRAT``, ``:conflict-strat "STRAT"``:
  This option determines how encountered version conflicts will be
  handled.  The default setting is ``exclusive`` and this setting
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
    to ``fast``.

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

    To mimic the behavior of maven, set ``--conflict-strat`` to ``prioritized``
    and ``--resolve-strat`` to ``fast``.

.. _present package:

- ``-p PKG``, ``--present-package PKG``, ``:present-packages ["PKG1", ...]``:
  Specify a "hard present package". Specify ``PKG`` as
  ``<id>==<vers>``, as in this example: ``garfield==1.0``.

  Doing this tells degasolv that a package "already exists" at a particular
  version in the system or build, whatever that means. This means that when
  degasolv encounters a requirement for this package, it will assume the
  package is already found and it will mark the dependency as resolved. On the
  other hand, degasolv will not try to change or update the found package. If
  the version of the present package conflicts with requirements encountered,
  resolution of those requirements may fail.

  This is another one of those options that is provided and, if needed, is
  meant to benefit the user; however, judicious use is recommended. If you
  don't know what you're doing, you probably don't want to use this option.

  For example, if this option is used to tell degasolv that, as part of a
  build, some packages have already been downloaded, degasolv will not
  recommend that those packages be upgraded. This is the "hard" in "hard
  present package": If the user specifies via ``--present-package`` that
  a package is already found and usable, degasolv won't try to find a new
  version for it; it assumes "you know what you're doing" and that the
  package(s) in question are not to be touched.

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

.. _repository option:

.. _specify repositories:

- ``-R INDEX``, ``--repository INDEX``, ``:repositories ["INDEX1", ...]``:
  **Required**. Search the repository index given by INDEX for packages when
  resolving the given requirements.

  When the index strategy is ``priority`` The last repository index specified
  will be the first to be consulted. If the repository indices are retrieved
  from the config file, they are consulted in order from first to last in the
  list.  If indices are specified both on the command line and in the
  configuration file, the indices in the configuration file are ignored. See
  `index strategy`_ for more information.

  ``INDEX`` may be a URL or a filepath. Both HTTP and HTTPS URLs are
  supported. If ``INDEX`` is ``-`` (the hyphen character), degasolv
  will read standard input instead of any specific file or
  URL. Possible use cases for this include downloading the index
  repository first via some other tool (such as `cURL`_).  One reason
  users might do this is if authentication is required to download the
  index, as in this example::

    curl --user username:password https://example.com/degasolv/index.dsrepo | \
        degasolv resolve-locations -R - "req"

  .. _cURL: https://curl.haxx.se/

- ``-s STRAT``, ``--resolve-strat STRAT``, ``:resolve-strat "STRAT"``: This
  option determines which versions of a given package id are considered when
  resolving the given requirements.  If set to ``fast``, only the first
  available version matching the first set of requirements on a particular
  package id is consulted, and it is hoped that this version will match all
  subsequent requirements constraining the versions of that id. If set to
  ``thorough``, all available versions matching the requirements will be
  considered. The default setting is ``thorough`` and this setting
  should work for most environments.

  .. note:: This option should be used with care, since whatever setting is
     used will greatly alter behavior. It is therefore recommended that
     whichever setting is chosen should be used `site-wide`_ within an
     organization.

.. _index strategy:

- ``-S STRAT``, ``--index-strat STRAT``, ``:index-strat "STRAT"``: Repositories
  are queried by package id in order to discover what packages are available to
  fulfill the given requirements. This option determines how multiple
  repository indexes are queried if there are more than one. If set to
  ``priority``, the first repository that answers with a non-empty result is
  used, if any. Note that this is true even if the versions don't match what is
  required.

  For example, if ``<repo-x>`` contains a package ``a`` at version ``1.8``,
  and ``<repo-y>`` contains a package ``a`` at version ``1.9``, then the
  following command wil fail::

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

  The default setting is ``priority`` and this setting should work for most
  environments.

  .. note:: This option should be used with care, since whatever setting is
     used will greatly alter behavior. It is therefore recommended that
     whichever setting is chosen should be used `site-wide`_ within an
     organization.

.. _package system:

- ``-t SYS``, ``--package-system SYS``, ``:package-system "SYS"``:
  **Experimental**. Specify package system to use. By default, this value
  is ``degasolv``. Using this option allows the user to run degasolv's
  resolver engine on respositories from other package manager systems. Though
  option was mainly implemented for profiling and debugging purposes, it is
  envisioned that this option will expand to include many package manager
  repositories. This will allow users to use degasolv to resolve packages
  from well-known sources, in a reliable and useful manner.

  Other available values are:

    - ``apt``: resolve using the APT debian package manager. When using this
      method, `specify repositories`_ using the format::

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

      .. note:: Degasolv does not currently support APT dependencies between
         machine architectures, as in ``python:i386``. Also, every degasolv
         repo is currently architecture-specific; each repo has an associated
         architecture, even if that architecture is ``any``.

CLI for ``query-repo``
----------------------

Running ``java -jar degasolv-<version>-standalone.jar query-repo -h`` returns a
page that looks something like this::

  Usage: degasolv <options> query-repo <query-repo-options>

  Options are shown below, with their default values and
    descriptions. Options marked with `**` may be
    used more than once.

    -q, --query QUERY                   Display packages matching query string.
    -R, --repository INDEX              Search INDEX for packages. **
    -S, --index-strat STRAT   priority  May be 'priority' or 'global'.
    -t, --package-system SYS  degasolv  May be 'degasolv' or 'apt'.
    -h, --help                          Print this help page

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
  This option works exactly the same as the `repository option`_ for the
  ``resolve-locations`` command, except that instead of using the repositories
  for resolving requirements, it uses them for simple index queries. See that
  option's explanation for more information.

- ``-S STRAT``, ``--index-strat STRAT``, ``:index-strat "STRAT"``:
  This option works exactly the same as the `index strategy`_ option for the
  ``resolve-locations`` command, except that it is used for simple index
  queries. See that option's explanation for more information.

- ``-t SYS``, ``--package-system SYS``, ``:package-system "SYS"``:
  This option works exactly the same as the `package system`_ option for the
  ``resolve-locations`` command, except that it is used for simple index
  queries. See that option's explanation for more information.

.. _Specifying a requirement:

Specifying a requirement
------------------------


.. _alternative:
.. _alternatives:

A requirement is given as a string of text. A
requirement consists of one or more *alternatives*. Any of the alternatives
will satisfy the requirement. Alternatives are specified by a bar character
(``|``), like this::

  "<alt1>|<alt2>|<alt3>"

Or, more concretely::

  "hickory|maple|oak"

Alternatives will be considered in order of appearance.

.. caution:: In general, specifying more than one alternative should be mostly unecessary, and should generally be avoided. This is because specifying too many alternatives tends to impact performance significantly; but they are available and usable if needed.

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
