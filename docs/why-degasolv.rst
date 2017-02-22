Why Degasolv?
=============

Degasolv is a generic dependency resolver that exists independent of
programming languages or systems.  You can use it to easily declare
the existence of files that your build depends on, version them, and
retrieve the URLs of files which are of the correct versions.  You can
easily use these URLs in your builds to download everything the build
needs.

Since Degasolv is a dependency resolver that is relatively
technology-agnostic, you can declare dependencies between components
that are not of the same technology.  You can declare that a DLL
depends on a pip package, or that in order to use an NPM package, a
certain ruby gem file must be present as well.

Often when building software, multiple different components from
multiple different teams must be used to create a larger build
artifact. These components are "released" by different teams, and
these teams each use different technologies, or the teams themselves
do not use a dependency manager. As a build engineer, it's your job to
bring all of these components together to make the build
work. Degasolv helps you do this. Some exmamples of people who might
use degasolv are below. For a more detailed example, see :ref:`A
Longer Example`.

  - Sravan, a build engineer, is repsponsible for the deployment
    pipeline of his company's cloud offering. There are several
    components created by different teams within his company, using
    totally different technologies: docker images, VM templates, and
    even PXE files are used to deploy different parts of the cloud
    stack. Each come from a different team, and each are released on
    different schedules. Sravan uses degasolv to version and track
    these artifacts and define relationships between them. He
    uses a CI build to get the URLs of every VM template, docker
    image, and PXE file, and places all these in a zip file which
    represents his entire cloud stack as a standalone build artifact,
    which can be promoted through environments and run through QA with
    minimal effort.

  - Sheila, a build engineer, is responsible for the build of an
    microsoft installer which installs her company's product. The
    installer contains python components, native code components, and
    ruby components.  The installer also takes files from a
    self-extracting tarball created by a third-party vendor, which has
    its own dependencies. With degasolv, Sheila can track all of these
    files independently of where the files are actually located, and
    she can use degasolv to tell her the exact files she needs for her
    build.

  - Daryl, a build engineer, is responsible for building a native code
    library (DLL or SO) file from the native code output from multiple
    teams. Each team releases their code as a zip file or tarball, but
    haven't realy adopted a formal dependency resolver in their
    builds. Further, they are building code for microsoft as well as
    linux, and MSBuild files only support relative paths when
    referring to dependencies in a build. Daryl puts the zip archives
    on his NAS, and uses degasolv to resolve the dependencies of the
    library. Degasolv returns URLs for all the packages to Daryl, who
    then uses them in a script to download them and place them in the
    directories where they can be found by the MSBuild files. The
    build works flawlessly ;)
