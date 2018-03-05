inherit kernel
require recipes-kernel/linux/linux-yocto.inc
require fix_kbuild_defconfig.inc
require linux-dtb-overlays.inc

FILESEXTRAPATHS_prepend := "${THISDIR}/files/linux-4.9:"

GIT_KERNEL_REPO ?= "github.com/hilscher/netx4000-linux.git;protocol=https"
KBRANCH = "v4.9-netx4000-stable"

LINUX_VERSION_EXTENSION = "-netx4000"

SRC_URI = "git://${GIT_KERNEL_REPO};name=machine;branch=${KBRANCH};nocheckout=1 \
           git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=yocto-4.9;destsuffix=${KMETA} \
           ${@bb.utils.contains('KERNEL_ENABLE_NAND',  '1', 'file://enable_nand.cfg', '',d)}"

# netX4000
LINUX_VERSION = "4.9.76"
SRCREV_machine="f7da2b259b71439a0c6662af8697292fe2c3afda"

# netX4000 RLXD (relaxed)
LINUX_VERSION_netx4000-rlxd = "4.9.56"
SRCREV_machine_netx4000-rlxd="5f89ca8199f2836d05764a7187841185817e1257"

SRCREV_meta="cdbd35c54b6a62e4fd543164f1dcdf92c85cff2d"

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
