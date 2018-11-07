#!/usr/bin/env bash

VERSION=$1

if [ "$VERSION" == "" ]; then
    echo "Version required"
    exit
fi

echo "Setting version $VERSION"

# set version in maven file
sed -i -e "0,/<version>.*<\/version>/s/<version>.*<\/version>/<version>$VERSION<\/version>/" ../pom.xml
# set version in site home page
sed -i -e "0,/&lt;version&gt;.*&lt;\/version&gt;/s/&lt;version&gt;.*&lt;\/version&gt;/\&lt;version\&gt;$VERSION\&lt;\/version\&gt;/" ../site/index.html
# add new version link to documentation index
sed -i -e "s/<ul>/<ul>\n    <li><a href=\"\/documentation\/$VERSION\">$VERSION<\/a><\/li>/" ../site/documentation/index.html
# add new version link to javadoc index
sed -i -e "s/<ul>/<ul>\n    <li><a href=\"\/javadocs\/$VERSION\/\">$VERSION<\/a><\/li>/" ../site/javadocs/index.html
