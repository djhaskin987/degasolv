.. _Code of Conduct:

Architecture
============

This article describes the overarching architecture of Degasolv, together with
some explanation about some of the design decisions.

Background
----------

At one of my previous jobs, I was a Build Engineer -- a person who built the
code that the developers wrote and made it available. I had *lots* of
dependency problems coming at me from all different sides:

  * One module in language A depending on another from language B
  * Developers working with a language (at the time) with no clear package
    manager ecosystem (**cough** C++ **cough**)
  * Package manager developers breaking builds with backwards-incompatible
    behavior
  * A dependency graph that looked like a dream catcher

So I decided to build a tool that would do these things:

  * Resolve dependencies the right way, safely
  * Even resolve dependency chains for different package systems (apt, pip,
    java)
  * Be super versatile and generic, able to be plugged into an arbitrary build
    script

Core Resolver
-------------

At the core of Degasolv is a monster method called `resolve-dependencies`_. It
is a rather large method with a backtracking SAT-solver-ish design. Originally
it was written to have a :ref:`conflict-strat<conflict-strat>` of ``exclusive``
and a :ref:`resolve-strat<resolve-strat>` of ``thorough`` hard-coded. In other
words, the "first class" original use case of Degasolv was a SAT-solver-class
depedency resolver that only allowed a single version of any dependency, and
ensured that all parties depending on that dependency had a chance to agree on
what was chosen. These options were later added to allow Degasolv to act more
like maven and give any Building Engineer using Degasolv useful "handbreaks" to
change how resolution was being done in-house so that it could be modified to
conform to business needs. Other options, such as `list-strat<list-strat>` and
`search-strat<search-strat>` were added as time progressed as well for similar
reasons, and also, frankly, to fix bugs (behaviors that were never originally
intended).

.. _resolve-dependencies: https://github.com/djhaskin987/degasolv/blob/develop/src/degasolv/resolver_core.clj#L519
