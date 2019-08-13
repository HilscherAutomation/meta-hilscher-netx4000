
# Uncomment the following line to enable the hard floating point abi. Note that
# this breaks some binary libraries and 3D (neither of which ship with
# meta-yocto). For maximum compatibility, leave this disabled.
DEFAULTTUNE ?= "cortexa9hf-neon"
require conf/machine/include/tune-cortexa9.inc

SOC_FAMILY="netx4000"
require conf/machine/include/soc-family.inc

#############################################################################
# Bootloader
#############################################################################
EXTRA_IMAGEDEPENDS += "barebox-netx4000"

PREFERRED_PROVIDER_virtual/bootloader ?= "barebox-netx4000"

PREFERRED_PROVIDER_virtual/kernel ??= "linux-stable"
PREFERRED_VERSION_linux-stable ??= "4.9.%"
PREFERRED_VERSION_linux-stable-rt ??="4.9.%"

PREFERRED_PROVIDER_virtual/dtb = "device-tree"
MACHINE_ESSENTIAL_EXTRA_RDEPENDS_append += "device-tree"

#############################################################################
# Kernel
#############################################################################
KERNEL_CLASSES += "kernel-fitimage"
KERNEL_IMAGETYPES ?= "zImage fitImage"
MACHINE_ESSENTIAL_EXTRA_RDEPENDS_append +=" \
	kernel-modules \
	libcifx cifxsample \
	dto-utils \
"

# Kernel default config does not set default parameters and yocto uses allnoconfig
# per default, thus we need to override it to use alldefconfig
KCONFIG_MODE ?= "--alldefconfig"

# poky-lsb requires nfsd in kernel
KERNEL_EXTRA_FEATURES_append += "${@ 'features/nfsd/nfsd-enable.scc' if '${DISTRO}' == 'poky-lsb' else '' }"

#############################################################################
# Initramfs
#############################################################################
INITRAMFS_IMAGE ?= "core-image-minimal-initramfs"
INITRAMFS_IMAGE_BUNDLE ?= ""
INITRAMFS_FSTYPES ?= "cpio.gz"

#############################################################################
# Contents of boot partition (excluding rootfs)
#############################################################################
IMAGE_BOOT_FILES ?= "netx.rom"
