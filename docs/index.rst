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

**Given these artifacts**:

  - ``http://example.com/repo/a-1.0.zip``
  - ``http://example.com/repo/b-2.0.zip``
  - ``http://example.com/repo/b-3.0.zip``

1. Generate dscard files to represent them in a degasolv respository,
   like this::

      #!/bin/sh

      java -jar degasolv-<version>-standalone.jar generate-card \
          --id "a" \
          --version "1.0" \
          --location "https://example.com/repo/a-1.0.zip" \
          --requirement "b>2.0" \
          --output-file "$PWD/a-1.0.zip.dscard"

      java -jar degasolv-<version>-standalone.jar generate-card \
          --id "b" \
          --version "2.0" \
          --location "https://example.com/repo/b-2.0.zip" \
          --output-file "$PWD/b-2.0.zip.dscard"

      java -jar degasolv-<version>-standalone.jar generate-card \
          --id "b" \
          --version "3.0" \
          --location "https://example.com/repo/b-3.0.zip" \
          --output-file "$PWD/b-2.0.zip.dscard"

2. Generate a ``dsrepo`` file from the cards::

      java -jar degasolv-<version>-standalone.jar \
          generate-repo-index \
          --search-directory $PWD \
          --output-file $PWD/index.dsrepo

3. Then use the ``dsrepo`` file to resolve dependencies::

      java -jar degasolv-<version>-standalone.jar \
          resolve-locations \
          --repository $PWD/index.dsrepo \
          "b"

***




**You would publish an EDN file in the same directory, here**:

  - `http://example.com/repo/index.edn`

**It might have these contents**:

```clojure
{
    "a"
    [{
        :id "a"
        :version "1.0"
        :location "http://example.com/repo/a-1.0.zip"
        :requirements [[{:status :present :id "b" :spec [{:relation :greater-than :version "2.0"}]}]]
    }]
    "b"
    [{
        :id "b"
        :version "2.0"
        :location "http://example.com/repo/b-2.0.zip"
    }
    {
        :id "b"
        :version "3.0"
        :location "http://example.com/repo/b-3.0.zip"
    }]
}
```

**Now, to get the URLs of the artifacts you should download if you
wish to download "A" and all its dependencies, you would write this**:

```clojure
(ns my.namespace
  (:require [dependable.resolver :refer :all]))

;; ...

(map :location
  (resolve-dependencies
    [[(present "a")]]
    (map-query
      (clojure.edn/read-string
        (slurp "http://example.com/repo/index.edn")))
    :compare clj-semver.core/older?))
```

**That little snippet would give you these results**:

```clojure
[
   "http://example.com/repo/a-1.0.zip"
   "http://example.com/repo/b-3.0.zip"
]
```

**Which URLs you could then use to download the artifacts and complete
the build.**

Support & Problems
------------------

If you have a hard time using degasolv to resolve dependencies within
builds, it is a bug! Please do not hesitate to let the authors know
via `GitHub issue`_ :).

.. _Github issue: https://github.com/djhaskin987/degasolv/issues

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
   :maxdepth: 2

   Why Degasolv? <why-degasolv>
   A Longer Example <longer-example>
   Resolving Dependency Problems <resolving-problems>
   Command Reference <command-reference>

Indices and tables
------------------
* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`
