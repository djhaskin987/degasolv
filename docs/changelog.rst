Changelog
=========

All notable changes to this project will be documented in this file.

The format is based on `Keep a Changelog`_
and this project adheres to `Semantic Versioning`_.

.. _Semantic Versioning: http://semver.org/spec/v2.0.0.html
.. _Keep a Changelog: http://keepachangelog.com/en/1.0.0/

`Unreleased`_
-------------
Added
+++++
- Added the ability to specify ``--present-package`` multiple times using the
  same package name, but different versions. This is useful for when the
  ``:conflict-strat`` is set to ``inclusive``.
- Added tests testing to make sure that unsuccessful runs generate the proper
  error messages.

Changed
+++++++
- Reorganized the unit tests.

Fixed
+++++
- Fixed bug wherein if the conflict strategy is set to ``:inclusive``
  and a package satisfying a requirement is already found or present,
  it is used instead of finding a new one.

`1.7.0`_
--------

Added
+++++
- Added ``--option-pack``, the ability to specify multiple options

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
- Add ability to use any (long) option on the command line in display-config

Improved
++++++++
- Memoized core Degasolv package system repository function (should
  speed the resolver up a bit)
- Changed apt reop function from filtering a list to lookup in a map,
  increasing its speed

.. _Unreleased: https://github.com/djhaskin987/degasolv/compare/1.7.0...HEAD
.. _1.7.0: https://github.com/djhaskin987/degasolv/compare/1.6.0...1.7.0
.. _1.6.0: https://github.com/djhaskin987/degasolv/compare/1.5.1...1.6.0
