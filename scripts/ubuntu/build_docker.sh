#!/bin/bash

docker build -t ubuntu-buildenv docker/.

METADIR="$(readlink -f $(pwd)/../../)"

docker run --privileged -it --rm -v $(pwd):/build -v $METADIR:/meta-hilscher-netx4000 ubuntu-buildenv /build/build_ubuntu_img.sh $@
