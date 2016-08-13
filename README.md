# dependable
Dependency resolver for the impatient.

I got really tired of seeing how often this problem was re-solved over and over
every time a new programming language came out, and every time a new package
manager was born. Further, some of those things only created an (often
self-described) half-done solution, something that *sort of* resolved
dependencies, but didn't really. I've also been in situations where I'm solving
a problem as a build engineer and I just need a generic dependency resolver.
So I am building this so that I can use it in my career.

This resolves dependencies and downloads packages for a myriad of packaging
tools. It's nice to have it all together. I'm a devops engineer, and sometimes
when I'm packaging/releasing stuff, I just want to download packages from many
places. Sometimes the package manager is broken, or sometimes the actual
package specified dependencies incorrectly, and I'd like to override these
problems from the resolver side.

# Motivation

## Installation

This will soon be on clojars.

## Usage

Something like

    $ java -jar dependable-0.1.0-standalone.jar [args]

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...

## License

Copyright © 2016 Daniel Jay Haskin

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
