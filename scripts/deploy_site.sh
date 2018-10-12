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
cp $GIT_HOME/site/javadocs/index.html .
wget https://oss.sonatype.org/service/local/repositories/releases/content/org/hrorm/hrorm/$VERSION/hrorm-$VERSION-javadoc.jar
jar xvf hrorm-$VERSION-javadoc.jar
cd ..
rm latest
ln -s $VERSION latest

cd $WEB_HOME/documentation
mkdir $VERSION
cp $GIT_HOME/site/documentation/index.html .
cp $GIT_HOME/site/documentation/latest/index.html $VERSION/index.html
cd ..
rm latest
ln -s $VERSION latest