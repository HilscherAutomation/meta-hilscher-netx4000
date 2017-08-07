inherit kernel
require recipes-kernel/linux/linux-yocto.inc
require fix_kbuild_defconfig.inc

GIT_KERNEL_REPO ?= "github.com/hilscher/netx4000-linux.git;protocol=https"
KBRANCH = "v4.4-netx4000-stable"

LINUX_VERSION = "4.4.57"
LINUX_VERSION_EXTENSION = "-netx4000"

SRC_URI = "git://${GIT_KERNEL_REPO};branch=${KBRANCH};nocheckout=1"
SRCREV="d05f819c12b48e892812876c6bc899fbe5f9e1ce"

KBUILD_DEFCONFIG = "netx4000_defconfig"

PR = "r0"
PV = "${LINUX_VERSION}+git${SRCPV}"

COMPATIBLE_MACHINE = "netx4000"

# Prevent automatically inclusion of kernel-image into rootfs/image
RDEPENDS_kernel-base=""

python () {
    kernel_fdt = d.getVar('KERNEL_DEVICETREE') or ""

    if kernel_fdt != "":
        raise bb.parse.SkipPackage("Kernel 4.4 does not support KERNEL_DEVICETREE feature")
}
