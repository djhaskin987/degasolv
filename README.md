![Degasolv Logo](https://github.com/djhaskin987/degasolv/raw/develop/Degasolv-small.png)


# Degasolv

Degasolv is a generic dependency resolver that will work across
programming language boundaries, customizable to fit the needs of
build engineers who are just trying to get their software built.

## What is a "Degasolv"?

Named for *Degas* the painter, and it's a *Solv*er.


## Download



Ubuntu: [ ![Download for Ubuntu](https://api.bintray.com/packages/degasolv/ubuntu/degasolv/images/download.svg) ](https://bintray.com/degasolv/ubuntu/degasolv/_latestVersion)

CentOS: [ ![Download for CentOS](https://api.bintray.com/packages/degasolv/centos/degasolv/images/download.svg) ](https://bintray.com/degasolv/centos/degasolv/_latestVersion)

See the [releases](https://github.com/djhaskin987/degasolv/releases)
tab for more (including the jar file).

## Quick Start

This quickstart is meant to be illustrative. For ideas on how to use
degasolv in real life, have a look at
[A Longer Example](http://degasolv.readthedocs.io/en/latest/longer-example.html).

**Given these artifacts**:

  - `http://example.com/repo/a-1.0.zip`
  - `http://example.com/repo/b-2.0.zip`
  - `http://example.com/repo/b-3.0.zip`

1. Generate dscard files to represent them in a degasolv respository,
   like this:

```
      $ java -jar degasolv-<version>-standalone.jar generate-card \
          --id "a" \
          --version "1.0" \
          --location "https://example.com/repo/a-1.0.zip" \
          --requirement "b>2.0" \
          --card-file "$PWD/a-1.0.zip.dscard"

      $ java -jar degasolv-<version>-standalone.jar generate-card \
          --id "b" \
          --version "2.0" \
          --location "https://example.com/repo/b-2.0.zip" \
          --card-file "$PWD/b-2.0.zip.dscard"

      $ java -jar degasolv-<version>-standalone.jar generate-card \
          --id "b" \
          --version "3.0" \
          --location "https://example.com/repo/b-3.0.zip" \
          --card-file "$PWD/b-2.0.zip.dscard"
```

2. Generate a `dsrepo` file from the cards:

```
    $ java -jar degasolv-<version>-standalone.jar \
          generate-repo-index \
          --search-directory $PWD \
          --index-file $PWD/index.dsrepo
```

3. Then use the `dsrepo` file to resolve dependencies:

```
      $ java -jar degasolv-<version>-standalone.jar \
          resolve-locations \
          --repository $PWD/index.dsrepo \
          --requirement "b"
```

   This should return something like this:

```
      a==1.0 @ http://example.com/repo/a-1.0.zip
      b==3.0 @ http://example.com/repo/b-3.0.zip
```

To see the help page, call degasolv or any of its subcommands with the
`-h` option. If this is your first time using degasolv, it's
recommended that you read [A Longer Example](http://degasolv.readthedocs.io/en/latest/longer-example.html).

## Installation, Quick Start, Tutorials, Reference?

See [Read the Docs](http://degasolv.readthedocs.io/en/develop/).

## 2017 Degasolv Presentation Slides

See [here](http://bit.ly/degasolv2017pres).

## License

Copyright © 2016-2017 Daniel Jay Haskin and others, see the AUTHORS.md file.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
