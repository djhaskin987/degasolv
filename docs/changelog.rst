Changelog
=========

All notable changes to this project will be documented here.

The format is based on `Keep a Changelog`_
and this project adheres to `Semantic Versioning`_.

.. _Semantic Versioning: http://semver.org/spec/v2.0.0.html
.. _Keep a Changelog: http://keepachangelog.com/en/1.0.0/

`Unreleased`_
-------------

Added
+++++
- Added the ``--meta`` :ref:`option <meta-data>` to
  :ref:`generate-card <generate-card-options>`
- Added metadata a la ``--meta`` to the ``apt`` :ref:`package system
  <package-system>` (experimental)
- Added the ``edn`` :ref:`output format <output-format>`
- Added the ``--output-format`` :ref:`option
  <output-format-query-repo>` to the :ref:`query-repo <query-repo>`
  command


Changed
+++++++

Fixed
+++++

`1.10.0`_
-------------

Added
+++++
- Added the ``--output-format`` :ref:`option <output-format>` to
  :ref:`resolve-locations <resolve-locations>`

`1.9.0`_
--------

Added
+++++
- Added the :ref:`pessimistic greater-than <pess-greater>` comparison operator ``><``.

Fixed
+++++
- Removed validation from the config file option, allowing it to be a URL or
  anything else.

- If no arguments are given, the help screen is now printed instead of a wierd
  error.

`1.8.0`_
--------

Added
+++++
- Distribution is now done via RPM and Debian package as well as JAR fil
- Added the ``--version-comparison`` option to
  :ref:`generate-repo-index <generate-repo-index>` (option :ref:`here
  <version-comparison-generate>`), :ref:`resolve-locations
  <resolve-locations>` (option :ref:`here <version-comparison-resolve>`)
  and :ref:`query-repo <query-repo-options>` (option :ref:`here
  <version-comparison-query>`), allowing the user to specify which
  version comparison algorithm is used.
- Added the ``--search-strat`` option to :ref:`resolve-locations
  <resolve-locations>`, allowing users to select breadth first
  search or depth first search during resolution
- Added the :ref:`matches <matches>` operator (``<>REGEX``) which
  matches a version against a regex
- Added the :ref:`in-range <in-range>` operator (``=>V``) which
  matches a version against a certain range of indexes
- Added the ability to specify ``--present-package`` multiple times using the
  same package name, but different versions. This is useful for when the
  ``:conflict-strat`` is set to ``inclusive``.
- Added tests testing to make sure that unsuccessful runs generate the proper
  error messages.

Changed
+++++++
- Reorganized the unit tests.
- Alphabetized the options for ``generate-card``.
- Alphabetized the options for ``generate-repo-index``.

Fixed
+++++
- Fixed bug wherein if the conflict strategy is set to ``:inclusive``
  and a package satisfying a requirement is already found or present,
  it is used instead of finding a new one.
- Fixed CLI of :ref:`display-config <display-config-cli>` so that
  it actually works as advertised, LOLZ
- Fixed the CLI output of ``--help`` so that default values
  of options are shown again :)
- Refreshed the CLI output of ``--help`` for all the subcommands
  as posted in the docs

`1.7.0`_
--------

Added
+++++
- Added ``--option-pack``, the ability to :ref:`specify multiple
  options at once <option-pack>`

Fixed
+++++
- Fixed how default options work, they no longer override stuff
  found in the config file (ouch)
- Fixed output of printed warning when configuration file is not used

`1.6.0`_
--------

Added
+++++
- Formatted docs better on the front page for PDF purposes
- Add ability to use any (long) option on the command line in
  :ref:`display-config <display-config-cli>`

Improved
++++++++
- Memoized core Degasolv package system repository function (should
  speed the resolver up a bit)
- Changed apt reop function from filtering a list to lookup in a map,
  increasing its speed

`1.5.1`_
--------

Added
+++++

- In just ~15 seconds, it slurps in a rather large apt repository
Packages.gz file.  In another ~45 seconds, it resolves the
ubuntu-desktop package, spitting out a grand total of 797 packages
with their locations.

Fixed
+++++

- While using the apt data and package system to profile degasolv, I
  found some rather nasty bugs. This release fixes them. This tool is
  now ready for prime time.

`1.5.0`_
--------

Added
+++++
- Added the ``--disable-alternatives`` :ref:`option
  <disable-alternatives>` and the ``--enable-alternatives``
  :ref:`option <enable-alternatives>` for debugging purposes.

`1.4.0`_
--------

Added
+++++

- Added the ``--present-package`` :ref:`option <present-package>` and
  the ``--package-system`` :ref:`option <package-system>` to the
  :ref:`resolve-locations <resolve-locations>` subcommand.  This was so
  that degasolv could be profiled using apt package repos
  (real-world data) and thereby have its performance optimized.

`1.3.0`_
--------

Added
+++++

- Add standard input as a file type. All options which take a file name may now
  have ``-`` given as the filename, to specify that standard in should be used.

`1.2.0`_
--------

Added
+++++

- Added the ability to specify multiple configuration files,
  thus allowing for site-wide configuration.

`1.1.0`_
--------

Added
+++++

- Added the ``--conflict-strat`` :ref:`option <conflict-strat>` to the
  :ref:`resolve-locations <resolve-locations>` subcommand.

- Added docs and tests.

1.0.2
-----

- This isn't the first release, but for the purposes of these docs, it is :D

.. _Unreleased: https://github.com/djhaskin987/degasolv/compare/1.10.0...HEAD
.. _1.10.0: https://github.com/djhaskin987/degasolv/compare/1.9.0...1.10.0
.. _1.9.0: https://github.com/djhaskin987/degasolv/compare/1.8.0...1.9.0
.. _1.8.0: https://github.com/djhaskin987/degasolv/compare/1.7.0...1.8.0
.. _1.7.0: https://github.com/djhaskin987/degasolv/compare/1.6.0...1.7.0
.. _1.6.0: https://github.com/djhaskin987/degasolv/compare/1.5.1...1.6.0
.. _1.5.1: https://github.com/djhaskin987/degasolv/compare/1.5.0...1.5.1
.. _1.5.0: https://github.com/djhaskin987/degasolv/compare/1.4.0...1.5.0
.. _1.4.0: https://github.com/djhaskin987/degasolv/compare/1.3.0...1.4.0
.. _1.3.0: https://github.com/djhaskin987/degasolv/compare/1.2.0...1.3.0
.. _1.2.0: https://github.com/djhaskin987/degasolv/compare/1.1.0...1.2.0
.. _1.1.0: https://github.com/djhaskin987/degasolv/compare/1.0.2...1.1.0
