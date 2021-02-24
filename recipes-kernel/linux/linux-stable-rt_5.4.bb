KBRANCH = "v5.4-netx4000-rt"
LINUX_KERNEL_TYPE = "preempt-rt"
LINUX_VERSION = "5.4.93"

GIT_KERNEL_REPO ?= "github.com/hilscher/netx4000-linux.git;protocol=https"

SRC_URI += "git://${GIT_KERNEL_REPO};name=machine;branch=${KBRANCH};nocheckout=1 \
            git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=yocto-5.4;destsuffix=${KMETA} \
            file://defconfig"

SRCREV_machine="5a708da9d148443b376a6012f9a26135ca73eea0"
SRCREV_meta="4f6d6c23cc8ca5d9c39b1efc2619b1dfec1ef2bc"

require linux-hilscher-common.inc

LIC_FILES_CHKSUM = "file://COPYING;md5=bbea815ee2795b2f4230826c0c6b8814"
