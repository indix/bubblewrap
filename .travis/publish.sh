#!/usr/bin/env bash

# Deploy on tagged builds only
#if ([ "$TRAVIS_TAG" != "" ]);
#then
    if [ ! -z "$TRAVIS" -a -f "$HOME/.gnupg" ]; then
        shred -v ~/.gnupg/*
        rm -rf ~/.gnupg
    fi
    source .travis/gpg.sh
    sbt +publishSigned
    sbt +sonatypeRelease
#fi
