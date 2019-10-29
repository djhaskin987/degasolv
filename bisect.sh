#!/bin/sh

set -ex
rm -rf target/
lein uberjar
test/resources/scripts/test-list-strat
