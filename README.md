# dependable

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.djhaskin987/dependable.svg)](https://clojars.org/org.clojars.djhaskin987/dependable)

Dependency resolver for the impatient.

I got really tired of seeing how often this problem was re-solved over
and over every time a new programming language came out, and every
time a new package manager was born. I ended up writing this library,
containing a particular function,
`dependable.resolver/resolve-dependencies`, which can be used as a
generic dependency resolver.

This will soon be on clojars.

Rather than give you not-so-informative example code using it, I will
point you to the unit tests for this function, found under
`test/dependable/resolver_test.clj`. All unit tests point to functionality
found in the function in question, and so also serve as a great starting
point to using this function.

Is this function complicated? Yes. Sorry. Welcome to dep management :(

## Recent Improvements

I just added dependency strategy option! You can set this to `:thorough` or
`:fast` when calling the function. This means you can decide whether or not
the resolver works like a SAT solver or just takes the first packages it
finds instead.

## License

Copyright © 2016 Daniel Jay Haskin

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
