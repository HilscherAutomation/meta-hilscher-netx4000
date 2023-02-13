#!/bin/bash

SCRIPTDIR=$(readlink -f ${0})
SCRIPTDIR=$(dirname ${SCRIPTDIR})

if [ -d build ]; then
  echo "Using uid of build directory"
  BUILD_UID=`ls -dn build/ | cut -f3 -d " "`:`id -g`
else
  echo "Using uid of logged in user"
  BUILD_UID=`id -u`:`id -g`
fi

docker build -t yocto-buildenv $SCRIPTDIR/docker/.

docker run -it --rm -v $(pwd):/build -u $BUILD_UID:`id -g` yocto-buildenv:20.04 /build/build.sh $@
