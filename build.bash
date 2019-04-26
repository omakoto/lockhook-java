#!/bin/bash

set -e
. mutil.sh
cd "${0%/*}"

cd lockhook
ant -f lockhook.xml
