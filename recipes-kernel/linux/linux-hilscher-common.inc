inherit kernel
require recipes-kernel/linux/linux-yocto.inc
require fix_kbuild_defconfig.inc
require linux-dtb-overlays.inc

LINUX_VERSION_EXTENSION = "-netx4000"
LINUX_BASEVERSION="${@".".join(d.getVar('LINUX_VERSION', d).split(".")[:2])}"

FILESEXTRAPATHS_prepend := "${THISDIR}/files/linux-${LINUX_BASEVERSION}:"

GIT_KERNEL_REPO ?= "github.com/hilscher/netx4000-linux.git;protocol=https"

SRC_URI = "git://${GIT_KERNEL_REPO};name=machine;branch=${KBRANCH};nocheckout=1 \
           git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=yocto-${LINUX_BASEVERSION};destsuffix=${KMETA} \
           ${@bb.utils.contains('KERNEL_ENABLE_NAND',  '1', 'file://enable_nand.cfg', '',d)} \
           ${@bb.utils.contains('MACHINE_FEATURES', 'tpm', 'file://enable_tpm.cfg', '', d)}"

KBUILD_DEFCONFIG = "netx4000_defconfig"
KMETA = "kernel-meta"
KCONF_BSP_AUDIT_LEVEL = "2"

PR = "r0"
PV = "${LINUX_VERSION}+git${SRCPV}"

# Prevent automatically inclusion of kernel-image into rootfs/image
RDEPENDS_kernel-base=""

python () {
	import os
	import hashlib

	dtsDirsModTimeList = ""

	for dtsDir in d.getVar('DTS_DIRS').split():
		for root, dirs, files in os.walk(dtsDir):
			bb.parse.mark_dependency(d, root)

			if dtsDirsModTimeList:
				dtsDirsModTimeList += ", "

			mtime = bb.parse.cached_mtime(root)
			dtsDirsModTimeList += "('dir:%s',%s)" % (root, mtime)

			for file in files:
				filename = root+"/"+file
				mtime = bb.parse.cached_mtime(filename)
				dtsDirsModTimeList += ", ('file:%s',%s)" % (filename, mtime)

	for file in dtsDirsModTimeList.split(', '):
		bb.debug(2, "DTS_DIRS (mtime): %s" % (file))

	dtsDirsHash = hashlib.sha256(dtsDirsModTimeList.encode("utf8")).hexdigest()
	bb.debug(1, "DTS_DIRS (overall hash): %s" % dtsDirsHash)

	d.setVar('DTS_DIRS_HASH', dtsDirsHash)
	d.appendVarFlag('do_unpack', 'vardeps', 'DTS_DIRS_HASH')
}

do_copy_dts() {
	[ -n "${DTS_DIRS}" ] && {
		bbnote "Copying/Replacing device tree files"
		mkdir -p ${S}/arch/arm/boot/dts/netx4000/
		for dtsDir in ${DTS_DIRS}; do
			bbnote "src:$dtsDir"
			cp -r $dtsDir/* ${S}/arch/arm/boot/dts/netx4000/
		done
	}
}
addtask do_copy_dts after do_patch before do_configure

KERNEL_EXTRA_FEATURES ?= "features/netfilter/netfilter.scc"
KERNEL_FEATURES_append = " ${KERNEL_EXTRA_FEATURES}"

COMPATIBLE_MACHINE = "netx4000"
