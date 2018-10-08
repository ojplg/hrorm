#!/usr/bin/env bash

VERSION=$1

if [ "$VERSION" == "" ]; then
    echo "Version required"
    exit
fi

echo "Downloading version $VERSION"

wget https://oss.sonatype.org/service/local/repositories/releases/content/org/hrorm/hrorm/$VERSION/hrorm-$VERSION.jar
wget https://oss.sonatype.org/service/local/repositories/releases/content/org/hrorm/hrorm/$VERSION/hrorm-$VERSION-sources.jar
wget https://oss.sonatype.org/service/local/repositories/releases/content/org/hrorm/hrorm/$VERSION/hrorm-$VERSION-javadoc.jar