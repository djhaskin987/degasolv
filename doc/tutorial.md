# Quick Start

So you're a Build engineer/DevOps engineer-ish, and you have a list of dependencies, and you want to have them downloaded automatically as part of a build.

Suppose you have your artifacts, all zip files, stored on an
auto-indexed HTTP server at `http://example.com/repo/`. One of the
developer teams you support makes a component or microservice called "A", and one
makes a component or microservice called "B". In order to compose them into a
bigger artifact that you can actually deploy, you need to download them into a build.

Why use a dependency manager to do this? Well, probably because your dependency tree is actually much bigger than just two services/components. But this is a quickstart. 

**So, given these artifacts**:

  - `http://example.com/repo/a-1.0.zip`
  - `http://example.com/repo/b-2.0.zip`
  - `http://example.com/repo/b-3.0.zip`

**You would publish an EDN file in the same directory, here**:

  - `http://example.com/repo/repo.edn`

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
        (slurp "http://example.com/repo/list.edn")))
    :compare clj-semver.core/older?))
```

**That little snippet would give you these results**:

```clojure
[
   "http://example.com/repo/a-1.0.zip"
   "http://example.com/repo/b-3.0.zip"
]
```

**Which URLs you could then use to download the artifacts and complete the build.**

# For more information

To view reference material, see the [reference guide](https://github.com/dependable/blob/develop/doc/reference.md).

To see how requirements works, see the [guide to the requirements parameter](https://github.com/dependable/blob/develop/doc/requirements.md).
