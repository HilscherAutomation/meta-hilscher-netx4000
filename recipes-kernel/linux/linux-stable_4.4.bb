inherit kernel
require recipes-kernel/linux/linux-yocto.inc

GIT_KERNEL_REPO ?= "github.com/hilscher/netx4000-linux.git;protocol=https"
BRANCH = "v4.4-netx4000-stable"

LINUX_VERSION = "4.4.57"
LINUX_VERSION_EXTENSION = "-netx4000"

SRC_URI = "git://${GIT_KERNEL_REPO};branch=${BRANCH};name=machine;nocheckout=1"

SRCREV_machine="ba62e1a75be4eeaff7d40ee233ffb7077944c825"
#SRCREV_machine="${AUTOREV}"

KBUILD_DEFCONFIG = "netx4000_defconfig"

PR = "r0"
PV = "${LINUX_VERSION}+git${SRCPV}"

COMPATIBLE_MACHINE = "netx4000"

# Prevent automatically inclusion of kernel-image into rootfs/image
RDEPENDS_kernel-base=""

