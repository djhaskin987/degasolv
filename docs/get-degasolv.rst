Get Degasolv
============

Download & Run
--------------

Degasolv comes in the form of a ``.jar`` file, `downloadable from GitHub`_.

As of version 1.8.0, it also comes in the form of an RPM or Debian package.

To get the RPM, add the CentOS repository::

  wget https://bintray.com/degasolv/centos/rpm -O bintray-degasolv-centos.repo
  sudo mv bintray-degasolv-centos.repo /etc/yum.repos.d/
  yum clean all
  yum makecache

To get the debian package, add the Ubuntu repository::

  echo "deb https://dl.bintray.com/degasolv/ubuntu stable main" | \
      sudo tee -a /etc/apt/sources.list.d/bintray-degasolv-ubuntu.list

To use it, you need java installed. Degasolv can be run like this::

  java -jar ./degasolv-<version>-standalone.jar

Or, if you are using an OS package, it can be run simply like this::

  degasolv

.. _downloadable from GitHub: https://github.com/djhaskin987/degasolv/releases

Code
----

Degasolv lives out on `Github`_.

.. _Github: https://github.com/djhaskin987/degasolv

Support & Problems
------------------

If you have a hard time using degasolv to resolve dependencies within
builds, it is a bug! Please do not hesitate to let the authors know
via `GitHub issue`_ :).

.. _Github issue: https://github.com/djhaskin987/degasolv/issues

You can also talk to us using `Gitter`_ or the `Google Group "degasolv-users"`_.

.. _Gitter: https://gitter.im/degasolv/Lobby

.. _Google Group "degasolv-users": https://groups.google.com/forum/#!forum/degasolv-users

Contributions
-------------

Please contribute to Degasolv! `Pull requests`_ are most welcome.

.. _Pull requests: https://github.com/djhaskin987/degasolv/pulls
