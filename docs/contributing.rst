
.. _Contributing Guide:

Contributing Guide
==================

*Thank you so much for considering contribution to Degasolv!*

First, please read the Degasolv :ref:`Code of Conduct`. This project
will not take any contribution coming from those who do not abide by
the code of conduct. This means that if a person is currently under
disciplinary action via avenues set forth in that document, we will
ignore your PR and/or any issues you may log.

A contribution can be large or small, code or non-code. To make a
contribution, first log a GitHub issue. Talk about what you want, and
ask for other's opinions.

When you go to make the PR, please use the following checklist to test
whether or not it is likely to be accepted:

1. **Is it based on the ``develop`` branch?** Degasolv uses the
   `git-flow`_ framework for branch management. Please make PRs to the
   ``develop`` branch.
2. **Do you have tests in your PR, and do they pass?** Tests are in
   two places in Degasolv: the ``test/degasolv`` directory, where more
   or less normal unit tests reside; and the
   ``test/resources/scripts`` directory, where scripty-integration
   tests reside. You must have at least a script test (and preferrably one or
   more unit tests) as a "spot-check" of your feature if the PR is to be
   merged. The test need not be elaborate; a simple test is better than no
   tests.
3. **Is your PR backwards compatible?** The biggest feature Degasolv
   provides is backwards compatibility.

   We only consider backwards incompatible changes
   in the form of new options for the "correct" behavior and switching the
   default for that option in a new version, ideally a major version.

   If Degasolv breaks a build, it is a bug. If it breaks a build in a way
   that can't be fixed by configuration, it is a bad bug.

   A good test if a PR is "backwards compatible"
   is if 1) it changes any previously merged script test and 2)
   if it breaks any of them.

4. **Did you add documentation around the feature in your PR?**
   Generally this at least means adding something to the :ref:`Command
   Reference` document.
5. **Did you add an entry to the Changelog?** This project keeps a
   curated :ref:`changelog <Degasolv Changelog>`.

There are some exceptions to the above rules. For example, if your patch is
less than two lines' difference from the previous version, your PR may be a
"typo" PR, which may qualify to get around some of the above rules. Just ask
the team on your GitHub issue.

.. _git-flow: http://nvie.com/posts/a-successful-git-branching-model/
