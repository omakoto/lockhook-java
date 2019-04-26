#!/bin/bash

set -e
. mutil.sh
cd "${0%/*}"

./build.bash
java -jar lockhook/out/artifacts/testtarget_pp_jar/testtarget.pp.jar
