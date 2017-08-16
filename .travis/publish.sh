#!/usr/bin/env bash

if ([ "$TRAVIS_BRANCH" == "master" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ]);
then
    echo "Triggering a versioned release of the project"
    if [ ! -z "$TRAVIS" -a -f "$HOME/.gnupg" ]; then
        shred -v ~/.gnupg/*
        rm -rf ~/.gnupg
    fi
    echo "Generating a new GPG key pair and publishing it to a keyserver"
    source .travis/gpg.sh
    echo "Generated a new GPG key pair and published it to a keyserver"
    echo "Attempting to publish signed jars"
    sbt +publishSigned
    echo "Published the signed jars"
    echo "Attempting to make a release of the sonatype staging"
    sbt sonatypeRelease
    echo "Released the sonatype staging setup"
else
    echo "Not running publish since we're either not in master or in a Pull Request"
fi
