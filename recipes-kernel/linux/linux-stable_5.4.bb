KBRANCH = "v5.4-netx4000"
LINUX_KERNEL_TYPE = "standard"
LINUX_VERSION = "5.4.225"

GIT_KERNEL_REPO ?= "github.com/hilscher/netx4000-linux.git;protocol=https"

SRC_URI += "git://${GIT_KERNEL_REPO};name=machine;branch=${KBRANCH};nocheckout=1 \
            git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=yocto-5.4;destsuffix=${KMETA} \
            file://defconfig"

SRCREV_machine="3d258630f2d599a3ab0da094f8f595fc85281baf"
SRCREV_meta="a384efcff80a36817e159f5b3c3ce5d91de1831f"

require linux-hilscher-common.inc

LIC_FILES_CHKSUM = "file://COPYING;md5=bbea815ee2795b2f4230826c0c6b8814"
