inherit kernel
require recipes-kernel/linux/linux-yocto.inc
require recipes-kernel/linux/fix_kbuild_defconfig.inc
require recipes-kernel/linux/linux-dtb-overlays.inc

FILESEXTRAPATHS_prepend := "${THISDIR}/files/linux-4.4:"

GIT_KERNEL_REPO ?= "github.com/hilscher/netx4000-linux.git;protocol=https"
KBRANCH = "v4.4-netx4000-rt"

LINUX_VERSION_EXTENSION = "-netx4000"

SRC_URI = "git://${GIT_KERNEL_REPO};name=machine;branch=${KBRANCH};nocheckout=1 \
           git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=yocto-4.4;destsuffix=${KMETA} \
           ${@bb.utils.contains('KERNEL_ENABLE_NAND',  '1', 'file://enable_nand.cfg', '',d)}"

# netX4000
LINUX_VERSION = "4.4.115-rt130"
SRCREV_machine="100c13c131010dd49c52f9c81c8e32428f55649b"

# netX4000 RLXD (relaxed)
LINUX_VERSION_netx4000-rlxd = "4.4.53-rt66"
SRCREV_machine_netx4000-rlxd="780e4d02d297364d720c6d721f9e29bcdc2aab9b"

SRCREV_meta="b149d14ccae8349ab33e101f6af233a12f4b17ba"

KBUILD_DEFCONFIG = "netx4000_defconfig"
KMETA = "kernel-meta"
KTYPE = "preempt-rt"
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
		mkdir -p ${S}/arch/arm/boot/dts/netx4000/
		cp -r ${WORKDIR}/${DTS_DIR}/* ${S}/arch/arm/boot/dts/netx4000/
	}
}

KERNEL_EXTRA_FEATURES ?= "features/netfilter/netfilter.scc"
KERNEL_FEATURES_append = " ${KERNEL_EXTRA_FEATURES}"
