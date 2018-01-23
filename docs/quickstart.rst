Quickstart
==========

This quickstart is meant to be illustrative. For ideas on how to use degasolv
in real life, have a look at :ref:`A Longer Example`.

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

To see the help page, call degasolv or any of its subcommands with the
``-h`` option. If this is your first time using degasolv, it's
recommended that you read :ref:`A Longer Example`.
