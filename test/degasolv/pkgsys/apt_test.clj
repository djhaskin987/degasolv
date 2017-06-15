(ns degasolv.pkgsys.apt-test
  (:require [clojure.test :refer :all]
            [degasolv.resolver :refer :all]
            [degasolv.pkgsys.apt :refer :all]))

(deftest ^:pkgsys-apt deb-to-degasolv-requirements-test
    (testing "Empty cases"
      (is (= nil
            (deb-to-degasolv-requirements nil)))
      (is (= nil
            (deb-to-degasolv-requirements ""))))
    (testing "Normal cases"
      (is (= [[(->Requirement
                :present
                "a"
                [[(->VersionPredicate
                    :greater-than
                    "5.0")]])]
              [(->Requirement
                :present
                "b"
                [[(->VersionPredicate
                    :greater-equal
                    "4.0")]])]]
             (deb-to-degasolv-requirements "a (>>5.0), b (>= 4.0)")))
        (is (= [[(->Requirement
                   :present
                   "a"
                   nil)
                 (->Requirement
                   :present
                   "b"
                   [[(->VersionPredicate
                       :less-than
                       "1.2.3")]])]
                [(->Requirement
                   :present
                   "c"
                   [[(->VersionPredicate
                       :equal-to
                       "1.0.0")]])]]
               (deb-to-degasolv-requirements
                "a|b (<< 1.2.3), c (= 1.0.0)")))))

(deftest ^:pkgsys-apt test-group-package-lines
    (testing "Empty cases"
      (is (empty?
            (group-pkg-lines '())))
      (is (=
            [[""]]
            (group-pkg-lines [""]))))
    (testing "Edge cases"
             (is (= [["Package: "
                      "foo"]]
                    (group-pkg-lines
                      ["Package: "
                       "foo"])))
             (is (= [["Package: foo"]
                     ["Package: bar" "Version: 1.0.0"]
                     ["Package: baz"]]
                    (group-pkg-lines
                      ["Package: foo"
                       "Package: bar"
                       "Version: 1.0.0"
                       "Package: baz"]))))
    (testing "Normal Cases"
             (is (= [["Package: foo"
                      "Version: 1.0.0"]
                     ["Package: bar"
                      "Version: 2.0.0"]
                     ["Package: baz"
                      "Version: 3.0.0"]]
                    (group-pkg-lines
                      ["Package: foo"
                       "Version: 1.0.0"
                       "Package: bar"
                       "Version: 2.0.0"
                       "Package: baz"
                       "Version: 3.0.0"])))))

(deftest ^:pkgsys-apt test-apt-repo
         (testing "A basic, sanity-check test on apt-repo"
                  (is (=
                        {"a11y-profile-manager"
                         [(->PackageInfo
                            "a11y-profile-manager"
                            "0.1.11-0ubuntu3"
                            "http://us.archive.ubuntu.com/ubuntu/pool/main/a/a11y-profile-manager/a11y-profile-manager_0.1.11-0ubuntu3_amd64.deb"
                            [[(->Requirement
                                :present
                                "liba11y-profile-manager-0.1-0"
                                [[(->VersionPredicate
                                    :greater-equal
                                    "0.1.11")]])]
                             [(->Requirement
                                :present
                                "libc6"
                                [[(->VersionPredicate
                                    :greater-equal
                                    "2.4")]])]
                             [(->Requirement
                                :present
                                "libglib2.0-0"
                                [[(->VersionPredicate
                                    :greater-equal
                                    "2.26.0")]])]])]
                         "a11y-profile-manager-doc" [(->PackageInfo
                                                       "a11y-profile-manager-doc"
                                                       "0.1.11-0ubuntu3"
                                                       "http://us.archive.ubuntu.com/ubuntu/pool/main/a/a11y-profile-manager/a11y-profile-manager-doc_0.1.11-0ubuntu3_all.deb"
                                                       nil)]}
                        (apt-repo
                          "http://us.archive.ubuntu.com/ubuntu/"
                          "Package: a11y-profile-manager
Priority: optional
Section: misc
Installed-Size: 27
Maintainer: Luke Yelavich <themuso@ubuntu.com>
Architecture: amd64
Version: 0.1.11-0ubuntu3
Depends: liba11y-profile-manager-0.1-0 (>= 0.1.11), libc6 (>= 2.4), libglib2.0-0 (>= 2.26.0)
Filename: pool/main/a/a11y-profile-manager/a11y-profile-manager_0.1.11-0ubuntu3_amd64.deb
Size: 6310
MD5sum: 88048849b5897f17b987c0bfd8f1c899
SHA1: 3520ea78e489da35a7e71048dd5ff3fe6d99e13e
SHA256: a14a3bf010d5e5f8a2b46ff94836808cca02ebb1610b9e36558d3a4d8a7296d9
Description: Accessibility Profile Manager - Command-line utility
Multi-Arch: foreign
Homepage: https://launchpad.net/a11y-profile-manager
Description-md5: ecbac70f8ff00c7dbf5fdc46d7819613
Bugs: https://bugs.launchpad.net/ubuntu/+filebug
Origin: Ubuntu
Supported: 9m
Task: ubuntu-live, ubuntu-gnome-desktop, ubuntu-mate-live

Package: a11y-profile-manager-doc
Priority: optional
Section: doc
Installed-Size: 118
Maintainer: Luke Yelavich <themuso@ubuntu.com>
Architecture: all
Source: a11y-profile-manager
Version: 0.1.11-0ubuntu3
Recommends: devhelp
Filename: pool/main/a/a11y-profile-manager/a11y-profile-manager-doc_0.1.11-0ubuntu3_all.deb
Size: 13362
MD5sum: d47968ecee4e0ef7b647b87022c9f6c7
SHA1: f14bf9a6cf95b7f0e22e03c9628ab8c394e32a1e
SHA256: 9827eea0cdb6f142057dc5768a8980f91f21dbb1544c9860a77e75ff3dfc183c
Description: Accessibility Profile Manager - Documentation
Homepage: https://launchpad.net/a11y-profile-manager
Description-md5: 1c71821ee46c31ca86e8242f7517c26e
Bugs: https://bugs.launchpad.net/ubuntu/+filebug
Origin: Ubuntu
Supported: 9m")))))
