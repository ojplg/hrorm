#!/bin/bash

#
# This is a script for helping to manage the release process.
#
# Right now, all it does it echo out all the steps to be performed manually.
#
# Eventually, most of these steps should be automated.
#

STEPNUMBER=1

function printstep {
    echo $((STEPNUMBER++))")" $1
}

printstep "Correct version number in pom.xml"
printstep "Create tag in git"
printstep "Run mvn deploy (requires gpg passphrase) to publish to oss.sonatype.org"
printstep "Update latest version maven link on website"
printstep "Push artifact from oss.sonatype.org to maven central (requires login)"
printstep "Put jar file artifacts on downloads page https://github.com/ojplg/hrorm/releases"
printstep "Update website on hrorm.org (aws)"
printstep "Update javadocs on hrorm.org (preserve old javadocs)"
