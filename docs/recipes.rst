.. _A Longer Example:

Some Useful Recipes
===================

Audience
--------

For the impatient.

Make a bash script to wrap the degasolv jar like this, making sure to make the
script executable::

    #!/bin/sh
    # Filename: /usr/bin/degasolv
    java -jar <location-of-degasolv.jar>/degasolv.jar "${@}"

Generate a card::

    degasolv generate-card -i "name" -v "0.1.0" -l "https://example.com/repo/name-0.1.0.zip" -r "a-dep" -r "another-dep>=3.5.0" -C name-0.1.0.zip.dscard

BASH: Download each location, then its signature, and verify it::

    #!/bin/sh
    set -exou pipefail
    degasolv resolve-locations -R ./index.dsrepo -r a -o json | \
        jq -r .packages[].location | \
        while read url
        do
            wget $url
            wget $url.asc
            gpg --verify $url.asc
        done

