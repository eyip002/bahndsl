#!/bin/bash
cd "$(dirname "$0")" || exit 1
./gradlew de.uniba.swt.dsl:build || exit 1
./gradlew de.uniba.swt.dsl.ide:build || exit 1

# build folder
mkdir build
cd build || exit

# copy the file
cp ../de.uniba.swt.dsl/build/distributions/*.zip ./
cp ../de.uniba.swt.dsl.ide/build/distributions/*.zip ./