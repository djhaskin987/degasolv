Degasolv
========

Download & Run
--------------

Degasolv comes in the form of a ``.jar`` file, `downloadable from GitHub`_.

To use it, you need java installed. Degasolv can be run like this::

  java -jar ./degasolv-<version>-standalone.jar

.. _downloadable from GitHub: https://github.com/djhaskin987/degasolv/releases

Quickstart
----------

This quickstart is meant to be illustrative. For ideas on how to use degasolv in real life,
have a look at :ref:`A Longer Example`.

**Given these artifacts**:

  - ``http://example.com/repo/a-1.0.zip``
  - ``http://example.com/repo/b-2.0.zip``
  - ``http://example.com/repo/b-3.0.zip``

1. Generate dscard files to represent them in a degasolv respository,
   like this::


      $ java -jar degasolv-<version>-standalone.jar generate-card \
          --id "a" \
          --version "1.0" \
          --location "https://example.com/repo/a-1.0.zip" \
          --requirement "b>2.0" \
          --card-file "$PWD/a-1.0.zip.dscard"

      $ java -jar degasolv-<version>-standalone.jar generate-card \
          --id "b" \
          --version "2.0" \
          --location "https://example.com/repo/b-2.0.zip" \
          --card-file "$PWD/b-2.0.zip.dscard"

      $ java -jar degasolv-<version>-standalone.jar generate-card \
          --id "b" \
          --version "3.0" \
          --location "https://example.com/repo/b-3.0.zip" \
          --card-file "$PWD/b-2.0.zip.dscard"

2. Generate a ``dsrepo`` file from the cards::

      $ java -jar degasolv-<version>-standalone.jar \
          generate-repo-index \
          --search-directory $PWD \
          --index-file $PWD/index.dsrepo

3. Then use the ``dsrepo`` file to resolve dependencies::

      $ java -jar degasolv-<version>-standalone.jar \
          resolve-locations \
          --repository $PWD/index.dsrepo \
          --requirement "b"

   This should return something like this::

      a==1.0 @ http://example.com/repo/a-1.0.zip
      b==3.0 @ http://example.com/repo/b-3.0.zip

Support & Problems
------------------

If you have a hard time using degasolv to resolve dependencies within
builds, it is a bug! Please do not hesitate to let the authors know
via `GitHub issue`_ :).

.. _Github issue: https://github.com/djhaskin987/degasolv/issues

You can also talk to us using `Gitter`_ or the `Google Group "degasolv-users"`_.

.. _Gitter: https://gitter.im/degasolv/Lobby

.. _Google Group "degasolv-users": https://groups.google.com/forum/#!forum/degasolv-users

Contribution
------------

Please contribute to Degasolv! `Pull requests`_ are most welcome.

.. _Pull requests: https://github.com/djhaskin987/degasolv/pulls

Detailed Usage
--------------

To see the help page, call degasolv or any of its subcommands with the
``-h`` option. If this is your first time using degasolv, it's
recommended that you read :ref:`A Longer Example`.

Further Reading
---------------

.. toctree::

   Why Degasolv? <why-degasolv>
   A Longer Example <longer-example>
   Command Reference <command-reference>

Indices and tables
------------------
* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`
