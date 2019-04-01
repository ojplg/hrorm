#!/bin/bash

cd ..
FILE=./src/test/java/org/hrorm/database/HelperFactory.java
grep 'public static DatabasePlatform CURRENT_PLATFORM' $FILE
sed -i 's/public static DatabasePlatform CURRENT_PLATFORM = H2;/public static DatabasePlatform CURRENT_PLATFORM = Postgres;/' $FILE
grep 'public static DatabasePlatform CURRENT_PLATFORM' $FILE
mvn clean test
sed -i 's/public static DatabasePlatform CURRENT_PLATFORM = Postgres;/public static DatabasePlatform CURRENT_PLATFORM = H2;/' $FILE
grep 'public static DatabasePlatform CURRENT_PLATFORM' $FILE
