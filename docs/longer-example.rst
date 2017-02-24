.. _A Longer Example:

A Longer Example
================

So you're a Build engineer/DevOps engineer-ish, and you have a list of
dependencies, and you want to have them downloaded automatically as
part of a build.

Suppose you have your artifacts, all zip files, stored on an
auto-indexed HTTP server at ``http://example.com/repo/``.

The dependencies
----------------

You have a suite of builds for which you are responsible. These
builds kick out artifacts, which we will here call "components". These
components depend on each other's presence in order to build. That
dependency tree looks like this:

![dependency graph](http://g.gravizo.com/g?
 digraph G {
   a -> b;
   b -> c;
   b -> d;
   d -> e;
   c -> e;
}
)

For example, in order to build (and operate) component `a`, you must
first have `b`, `c`, `d`, and `e` present in the build directory.

Well, *sort of*. You see, there was a recent breaking change to
``a``. Where ``a@1.9`` worked fine with all previous versions of
``b``, the newer ``a@2.1`` only works with `b@2.3` or greater. Since
the ``2.0`` line of ``b`` came out, it relies on the newer ``c@3.5``,
and the ancient-but-still-used ``d``, the only version of which was
published as ``d@0.5``. The last time ``d`` was touched, the newest
version of ``e`` was ``1.1``; however, the newer ``c@3.5`` relies on
the fact that ``e`` must be at least at version ``1.8`` or
newer. There are three published versions of `e`: `e@1.8`, ``e@2.1``,
and ``e@2.4``. Only the ``e@1.8`` version of ``e`` is backwards
compatible with ``e@1.1`` and so it is the only version which will
satisfy all dependencies.

See? THIS is why you use a dependency manager with your builds.

Adding ``e`` to the degasolv repo
---------------------------------

The first step is to build ``e``, since it is at the bottom of our
dependency tree. In our example, when we build ``e``, we mean that we
are generating the file ``e-<version>.zip`` using the source code for
``e``. Let's say that we have as part of this build already created
``e``, at the brand new version of ``1.8``. We might have a file
called ``degasolv.edn`` somewhere in our git repository for ``e``. We
can use this file to specify options to degasolv, including
repositories, requirements, etc. of the build. The file will be simple
for ``e``, though, since ``e`` has no other dependencies. It might
look like this::

  ; filename: degasolv.edn
  {
      :id "e"
      :version "1.8.0"
  }

During the build of ``e``, we push the build artifact ``e-1.8.0.zip``
to the reposerver so that it can be downloaded at
``https://example.com/repo/e-1.8.0.zip``. Then, we generate a
``dscard`` file for ``e``. This file will represent ``e`` in a
degasolv repository. It is done like this::

  $ java -jar degasolv-<version>-standalone.jar -c ./degasolv.edn \
      generate-card \
      --location "https://example.com/repo/e-1.8.0.zip" \
      --output-file "e-1.8.0.zip.dscard"

Note that it is idiomatic to name the output file after the name of
the file that the card will be representing in the degasolv
respository.

This will create a file called ``e-1.8.0.zip.dscard``. You would then
copy this file up to a server where the degasolv server would reside::

  $ rsync e-1.8.0.zip.dscard user@reposerver:/var/www/repo/

Once the card is added to the repo on the repo server, a command is run on the server
to generate (or update) a degasolv repository index::

  $ ssh user@reposerver
  $ cd /var/www/repo
  $ java -jar ./degasolv-<version>-standalone.jar \
      generate-repo-index \
      --search-directory /var/www/repo \
      --output-file /var/www/repo/index.dsrepo

This file takes all of the package information from all of the
degasolv card files found under ``/var/www/repo`` and adds it to the
repository index ``/var/www/repo/index.dsrepo``. Once this is done,
the package ``e`` is listed as available in the degasolv respository
index. We can check that the version of ``e`` that we
put in the degasolv repository made it into the index from any machine
that can see the ``index.dsrepo`` file on the reposerver, like this::

  $ java -jar ./degasolv-<version>-standalone.jar \
      query-repo \
      --repository "https://example.com/repo/index.dsrepo" \
      --query "e"

Supposing that multiple versions of e is in the repository, its output
will look like this::

  e==1.8.0 @ https://example.com/repo/e-1.8.0.zip
  e==2.1.0 @ https://example.com/repo/e-2.1.0.zip
  e==2.4.0 @ https://example.com/repo/e-2.4.0.zip

We can see that the version of ``e`` we were building, namely
``1.8.0``, is now in the repository, so we know that the repository
index has been properly updated.

Adding ``d`` to the degasolv repo
---------------------------------

In our example, ``d`` is ancient, and not built anymore in our
environment; however, it is still used in other builds. We will not
use a ``degasolv.edn`` file for it, because there is nowhere to commit
such a file to source. We will simply generate a ``dscard`` file for
it using command line options::

  $ java -jar degasolv-<version>-standalone.jar \
      generate-card \
      --id "d" \
      --version "0.5.0" \
      --location "https://example.com/repo/d-0.5.0.zip" \
      --requirement "e>=1.00,<2.0.0" \
      --output-file "d-0.8.0.zip.dscard"

Note that we can either use command-line options or config file keys
to specify the information that degasolv needs.

We then copy the newly created ``d-0.5.0.zip.edn`` file up to the
server and use it to update the repository index in the same way as
for ``e`` above.

Adding ``c`` to the degasolv repo
---------------------------------

The ``c`` artifact (zip file) represents a project that is being
actively built and developed, so we will create a ``degasolv.edn``
file and commit it to the source repository for ``c``. The build for
``c`` relies on the ``e`` artifact being present, so we will resolve that
dependency before we start the build for ``c``. Then, when we
build the ``c`` project, we will create its corresponding degasolv
card file as part of the build, like we did with ``e``.

First, we commit its ``degasolv.edn`` file to source code. It might
look like this::

  ; filename: degasolv.edn
  {
      :id "c"
      :version "3.5.0"
      :requirements ["e>=1.8.0"]
      :repositories ["https://example.com/repo/index.dsrepo"]
  }

As mentioned earlier, ``c`` needs the ``e`` artifact in order to
build. We'll use ``degasolv`` as part of ``c`` build script to
download the most recent version fitting the requirement for ``e``
like this::

  $ java -jar degasolv-<version>-standalone.jar -c ./degasolv.edn \
      resolve-locations

This command will return output looking something like this::

  e: https://example.com/repo/e-1.8.0.zip

We can use this output in a script to download and unzip the zip file
so that it can be used as part of the build for ``c`` like this::

  #!/bin/sh

  java -jar degasolv-<version>-standalone.jar -c ./degasolv.edn \
      resolve-locations | while read pkg
  do
    name=$(echo "${pkg}" | awk -F ': ' '{print $1}')
    url=$(echo "${pkg}" | awk -F ': ' '{print $2}')
    curl -o ${name}.zip -L ${url}
    unzip ${name}.zip
  done

This stanza can be used in a build script to download all of the
dependencies for ``c`` and unzip them in the current directory.

At the end of the build for ``c``, we can create the degasolv card
file for ``c`` like this::

  $ java -jar degasolv-<version>-standalone.jar -c ./degasolv.edn \
      generate-card \
      --location "https://example.com/repo/c-3.5.0.zip" \
      --output-file "c-3.5.0.zip.dscard"

Then we upload this file to our http server and use it to update the
``index.dsrepo`` degasolv repository index file in the same way as
what we did during the build for ``e``.

Let us now suppose that we have repeated these steps for all packages
mentioned at the beginning of this example, except for the package
``a`` -- ``e``, ``d``, ``c``, and ``b``.

Building ``a``
--------------

Now suppose that we are building ``a``. In our example, the build artifact for
``a`` need not be
uploaded to the zip file repository, because `a` represents our final
product, and will be handed off to Project Management or Ops for later
release. We don't need it for any other builds, and so (in this
trivial example) we are not interested in uploading it to the repo.
But we are interested in resolving its dependencies, downloading them,
and using them to build the final product.

Just like when we are uploading a package to the repository, we need a
file called something like `dependable.edn` in the root of the git
repository associated with building `a`. It might look like this:

```clojure
{
    :id "a"
    :version "2.1.0"
    :file-name "a-2.1.0.zip"
    :requirements [[{:status :present :id "b" :spec [{:relation :greater-than :version "2.0"}]}]]
}
```

With some minor arrangements, the code in the quickstart used to show how to
use the `resolve-dependencies` function will suffice here:

```
(ns my.namespace
  (:require [dependable.resolver :refer :all]))

;; ...

(map :location
  (resolve-dependencies
    (:requirements
      (clojure.edn/read-string
        (slurp "dependable.edn")))
    (map-query
      (clojure.edn/read-string
        (slurp "http://example.com/repo/index.edn")))
    :compare clj-semver.core/older?))
```

Or, alternatively, on the commandline:

The following `map` function should return an array of locations
which can then be used to download artifacts needed for the build. In
our example, that should look like this:

```clojure
[
  "http://example.com/repo/b-2.4.0.zip"
  "http://example.com/repo/c-3.5.0.zip"
  "http://example.com/repo/d-0.8.0.zip"
  "http://exmaple.com/repo/e-1.8.0.zip"
]
```

# For more information

To view reference material, see the [reference guide](https://github.com/dependable/blob/develop/doc/reference.md).

To see how requirements works, see the [guide to the requirements parameter](https://github.com/dependable/blob/develop/doc/requirements.md).
