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

printstep "Run the dumb_check.sh script"
printstep "Run the update_files.sh script"
printstep "Run mvn deploy (requires gpg passphrase) to publish to oss.sonatype.org"
printstep "Push artifact from oss.sonatype.org to maven central (requires login)"
printstep "Wait a while"
printstep "Run download_artifacts.sh script to grab jars for tag"
printstep "Create tag in git"
printstep "Log onto aws and git pull and run deploy_site.sh script"
