inherit kernel
require recipes-kernel/linux/linux-yocto.inc
require fix_kbuild_defconfig.inc

GIT_KERNEL_REPO ?= "github.com/hilscher/netx4000-linux.git;protocol=https"
KBRANCH = "v4.9-netx4000-rt"

LINUX_VERSION = "4.9.20-rt16"
LINUX_VERSION_EXTENSION = "-netx4000"

SRC_URI = "git://${GIT_KERNEL_REPO};branch=${KBRANCH};nocheckout=1"
SRCREV="bc21d67390579ba0821980abe73b67bd00042908"

KBUILD_DEFCONFIG = "netx4000_defconfig"

PR = "r0"
PV = "${LINUX_VERSION}+git${SRCPV}"

COMPATIBLE_MACHINE = "netx4000"

# Prevent automatically inclusion of kernel-image into rootfs/image
RDEPENDS_kernel-base=""

SRC_URI += "file://${DTS_DIR}"

do_compile_prepend() {
	[ -n "$(ls ${DTS_DIR})" ] && {
		bbnote "Copying/Replacing device tree files"
		mkdir -p ${S}/arch/arm/boot/dts/netx4000/
		cp -r ${WORKDIR}/${DTS_DIR}/* ${S}/arch/arm/boot/dts/netx4000/
	}
}