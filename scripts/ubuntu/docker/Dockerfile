FROM ubuntu:18.04

ENV LANG=en_US.UTF-8
ENV TERM="xterm-color"

RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y locales debootstrap qemu-user-static \
    build-essential gcc-arm-linux-gnueabihf git fakechroot libssl-dev python3 \
    lzop libncurses5-dev python bc apt-utils flex bison rsync \
    kernel-package parted dosfstools mtools && \
    apt-get clean

RUN locale-gen en_US.UTF-8
RUN update-locale LC_ALL=en_US.UTF-8 LANG=en_US.UTF-8

WORKDIR /build
