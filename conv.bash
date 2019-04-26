#!/bin/bash

set -e
. mutil.sh
cd "${0%/*}"

ee java -jar ./lockhook/out/artifacts/lockhook_jar/lockhook.jar \
  -o /tmp/out.jar --spec testtarget/hookspec.yaml \
  "$@"
