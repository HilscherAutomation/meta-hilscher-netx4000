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
RDEPENDS_${KERNEL_PACKAGE_NAME}-base=""

KERNEL_EXTRA_FEATURES ?= "features/netfilter/netfilter.scc"
KERNEL_FEATURES_append = " ${KERNEL_EXTRA_FEATURES}"

COMPATIBLE_MACHINE = "netx4000"

UBOOT_SIGN_ENABLE = "${PLATFORM_SIGN}"
UBOOT_SIGN_KEYNAME = "${PLATFORM_KEYNAME}"
UBOOT_SIGN_KEYDIR = "${PLATFORM_KEYDIR}"
UBOOT_DTB_BINARY = ""
