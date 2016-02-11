Roadmap for 1.0
===============

Engine
------
- [X] retrieval
- [X] already-installed
- [X] conflicts
- [X] version-spec: retrieval
- [X] version-spec: already-installed
- [ ] version-spec: conflicts
- [ ] no-locking: retrieval
- [ ] no-locking: already-installed
- [ ] no-locking: conflicts
- [ ] requires
- [ ] provides
- [ ] overrides
- [ ] disjunctions


EDN resolver
------------
* requires-query
* overrides-query
* provides-query

Repository
----------
* chained-repository
* pooled-repository
* memoized-repository
* EDN-file-repository
* EDN-repository

Frontend
--------
* EDN Config file override
* EDN repository type
* Integration tests

Trivial Nice-to-haves-maybe
===========================
* Refactor already-installed to be callable instead of
  a map, so that I/O can be performed if need be
* Refactor conflicts (tests) be callable instead of
  a map, so that I/O can be performed if need be
