Quick Start
===========

So you're a Build engineer/DevOps engineer-ish, and you have a list of
dependencies, and you want to have them downloaded automatically as
part of a build.

Suppose you have your artifacts, all zip files, stored on an
auto-indexed HTTP server at `http://example.com/repo/`. One of the
developer teams you support makes a component or microservice called
"A", and one makes a component or microservice called "B". In order to
compose them into a bigger artifact that you can actually deploy, you
need to download them into a build.

Why use a dependency manager to do this? Well, probably because your
dependency tree is actually much bigger than just two
services/components. But this is a quickstart.

**So, given these artifacts**:

  - `http://example.com/repo/a-1.0.zip`
  - `http://example.com/repo/b-2.0.zip`
  - `http://example.com/repo/b-3.0.zip`

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

Now, a longer example.
======================

The dependencies
----------------

So, you have a suite of builds for which you are responsible. These
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
`a`. Where `a@1.9` worked fine with all previous versions of `b`, the
newer `a@2.1` only works with `b@2.3` or greater. Since the `2.0` line of
`b` came out, it relies on the newer `c@3.5.2`, and the
ancient-but-still-used `d`, the only version of which was published as
`d@0.5`. The last time `d` was touched, the newest version of `e` was
`1.1`; however, the newer `c@3.5` relies on the fact that `e` must be
newer than `1.8`. There are three published versions of `e`: `e@1.8`,
`e@2.1`, and `e@2.4`. Only the `e@1.8` version of `e` is backwards
compatible with `e@1.1` and so it is the only version which will
satisfy all dependencies.

See? THIS is why you use a dependency manager with your builds.

Uploading a build
-----------------

The first step is to build one of these dependencies. Let's say that
we have as part of this build already created `e`, at the brand new
version of `1.8`. We might have a file called `dependable.edn` somewhere
in our git repository for `e`. It might look like this:

```clojure
{
    :id "e"
    :version "1.8.0"
    :file-name "e-1.8.0.zip"
    :requirements []
}
```

We will use this file to update the information of the downstream package repository like this:

```clojure
(spit "new-repo-index.edn"
  (let [e-information (clojure.edn/read-string (slurp "./dependable.edn"))]
    (dependable.util/assoc-conj
      (clojure.edn/read-string
        (slurp "http://example.com/repo/index.edn"))
      (:id information)
      (assoc
        (dissoc e-information :file-name)
        :location
        (str "http://example.com/repo/" (:file-name a-information))))))
```

This new file could then be copied up to the HTTP server in place of the old file
located at `http://example.com/repo/index.edn`.

So, to summarize:
  - We've just build `e`.
  - After we build `e`, we update the repo metadata.
  - We also upload the file associated
    with `e` to the repo, in this case it's `e-1.8.0.zip`.

Let us now suppose that we have repeated these steps for all packages
mentioned above, except for the package `a`. The repo's `index.edn`
file might then look like this:

```clojure
{
    "b"
    [{
        :id "b"
        :version "1.7.0"
        :location "http://example.com/repo/b-1.7.0.zip"
        :requirements []
    }
    {
        :id "b"
        :version "2.3.0"
        :location "http://example.com/repo/b-2.4.0.zip"
        :requirements [[{:status :present :id "c" :spec [{:relation :greater-equal :version "3.5"}]}]]
    }]
    "c"
    [
        {
        :id "c"
        :version "2.4.7"
        :location "http://example.com/repo/c-2.4.7.zip"
        :requirements []
        }
        {
        :id "c"
        :version "3.5.0"
        :requirements [[{:status :present :id "e" :spec [{:relation :greater-equal :version "1.8"}]}]]
        }
    ]
    "d"
    [{
        :id "d"
        :version "0.8.0"
        :location "http://example.com/repo/d-0.8.0.zip"
        :requirements [[{:status :present :id "e"
            :spec [
              {:relation :greater-equal :version "1.1"}
              {:relation :less-than :version "2.0"}]}]]
    }]
    "e"
    [
        {
        :id "e"
        :version "1.8"
        :requirements []
        }
        {
        :id "e"
        :version "2.1"
        :requirements []
        }
        {
        :id "e"
        :version "2.4"
        :requirements []
        }
    ]
}
```

Resolving dependencies
----------------------

Now suppose that we are building `a`. In our example, `a` need not be
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
    :version "2.1"
    :file-name "a-2.1.zip"
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
