FROM crops/yocto:ubuntu-18.04-base

USER root

RUN apt-get update && \
    apt-get install -y dialog zstd liblz4-tool ca-certificates && \
    apt-get clean

USER yoctouser

ENV LANG=en_US.UTF-8
ENV TERM=xterm-color

WORKDIR /build
