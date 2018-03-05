inherit kernel
require recipes-kernel/linux/linux-yocto.inc
require fix_kbuild_defconfig.inc
require linux-dtb-overlays.inc

FILESEXTRAPATHS_prepend := "${THISDIR}/files/linux-4.14:"

GIT_KERNEL_REPO ?= "github.com/hilscher/netx4000-linux.git;protocol=https"
KBRANCH = "v4.14-netx4000-stable"

LINUX_VERSION_EXTENSION = "-netx4000"

SRC_URI = "git://${GIT_KERNEL_REPO};name=machine;branch=${KBRANCH};nocheckout=1 \
           git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=yocto-4.14;destsuffix=${KMETA} \
           ${@bb.utils.contains('KERNEL_ENABLE_NAND',  '1', 'file://enable_nand.cfg', '',d)}"
           
# netX4000
LINUX_VERSION = "4.14.14"
SRCREV_machine="d0d06f5b0fc707e64b30045683b3da4e7808e188"

SRCREV_meta="ee7e849882aa2f0947dd56a60ba5b0d2eec558f2"

KBUILD_DEFCONFIG = "netx4000_defconfig"
KMETA = "kernel-meta"
KTYPE = "standard"
KCONF_BSP_AUDIT_LEVEL = "2"

PR = "r0"
PV = "${LINUX_VERSION}+git${SRCPV}"

COMPATIBLE_MACHINE = "netx4000"

# Prevent automatically inclusion of kernel-image into rootfs/image
RDEPENDS_kernel-base=""

SRC_URI += "file://${DTS_DIR}"

do_compile_prepend() {
	[ -n "$(ls ${DTS_DIR})" ] && {
		bbnote "Copying/Replacing device tree files"
		mkdir -p ${S}/arch/arm/boot/dts/netx4000
		cp -r ${WORKDIR}/${DTS_DIR}/* ${S}/arch/arm/boot/dts/netx4000/
	}
}

KERNEL_EXTRA_FEATURES ?= "features/netfilter/netfilter.scc"
KERNEL_FEATURES_append = " ${KERNEL_EXTRA_FEATURES}"
