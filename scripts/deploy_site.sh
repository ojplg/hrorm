#!/usr/bin/env bash

VERSION=$1

if [ "$VERSION" == "" ]; then
    echo "Version required"
    exit
fi

GIT_HOME=$HOME/git/hrorm/
WEB_HOME=$HOME/www/html/

cp $GIT_HOME/site/index.html $WEB_HOME/index.html

cd $WEB_HOME/javadocs
mkdir $VERSION
cd $VERSION
wget https://oss.sonatype.org/service/local/repositories/releases/content/org/hrorm/hrorm/$VERSION/hrorm-$VERSION-javadoc.jar
jar xvf hrorm-$VERSION-javadoc.jar
rm latest
ln -s 0.2.1 latest