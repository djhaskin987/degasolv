Contributing Guide
==================

*Thank you so much for considering contribution to Degasolv!*

First, please read the Degasolv :ref:`Code of Conduct`. *We do not welcome any
contribution from those who do not abide by the code of conduct.* If a person
is currently under disciplinary action via avenues set forth in that document,
we will ignore your PR and/or any issues you may log.

A contribution can be large or small, code or non-code. To make a contribution,
first log a GitHub issue. Talk about what you want, and ask for other's
opinions.

When you go to make the PR, please use the following checklist to test whether
or not it is likely to be accepted:

1. **Is it off of the ``develop`` branch?** Degasolv uses the `git-flow`_
   framework for branch management. Please make PRs to the ``develop`` branch.
2. **Do you have tests in your PR, and do they pass?** Tests are in two places
   in degasolv: the ``test/degasolv`` directory, where more or less normal unit
   tests reside; and the ``test/resources/scripts`` directory, where
   scripty-integration tests reside. You must have at least a script test as
   a "spot-check" of your feature if the PR is to be merged. The test need not
   be elaborate; simple tests are better than no tests.
3. **Is your PR backwards compatible?** The biggest feature Degasolv provides
   is backwards compatibility. If Degasolv breaks a build, it is a bug. A PR is
   herein defined to be "backwards incompatible" if 1) it changes any previously
   merged unit or script test and 2) if it breaks any of them.
4. **Did you add documentation around the feature in your PR?** Generally this
   at least means adding something to the :ref:`Command Reference` document.

There are some exceptions to the above rules. If your patch is less than
two lines' difference from the previous version, your PR may be a "typo" PR,
which may qualify to get around some of the above rules. Just ask the team
on your GitHub issue.
