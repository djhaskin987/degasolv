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
- [ ] requires: retrieval
- [ ] requires: already-installed
- [ ] requires: conflicts
- [ ] requires: version-spec
- [ ] requires: version-spec: retrieval
- [ ] requires: version-spec: already-installed
- [ ] requires: version-spec: conflicts
- [ ] requires, version-spec, conflicts, already-installed
- [ ] no-locking: retrieval
- [ ] no-locking: already-installed
- [ ] no-locking: conflicts
- [ ] no-locking: requires: version-spec a-b\_c
- [ ] no-locking: requires: version-spec a-b\_c
- [ ] no-locking: requires: version-spec a\_b-c\_d
- [ ] provides
- [ ] overrides
- [ ] disjunctions
- [ ] automatic upgrades of dependencies (perhaps sooner?)


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
