KBRANCH = "v5.4-netx4000-rt"
LINUX_KERNEL_TYPE = "preempt-rt"
LINUX_VERSION = "5.4.221"

GIT_KERNEL_REPO ?= "github.com/hilscher/netx4000-linux.git;protocol=https"

SRC_URI += "git://${GIT_KERNEL_REPO};name=machine;branch=${KBRANCH};nocheckout=1 \
            git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=yocto-5.4;destsuffix=${KMETA} \
            file://defconfig"

SRCREV_machine="3250754d359675dd93df57cc45e4b2137260f0ac"
SRCREV_meta="c917f683a6394ae00f81139ae57ae0112d4b7528"

require linux-hilscher-common.inc

LIC_FILES_CHKSUM = "file://COPYING;md5=bbea815ee2795b2f4230826c0c6b8814"
