#!/bin/bash

set -e
. mutil.sh
cd "${0%/*}"

./build.bash

pre=/tmp/lockhook-pre.txt
post=/tmp/lockhook-post.txt

java-dump lockhook/out/artifacts/testtarget_jar/testtarget.jar lockhooktesttarget > $pre
java-dump lockhook/out/artifacts/testtarget_pp_jar/testtarget.pp.jar lockhooktesttarget > $post

meld $pre $post &
java -jar lockhook/out/artifacts/testtarget_pp_jar/testtarget.pp.jar
