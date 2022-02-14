DEFAULTTUNE ?= "cortexa9hf-neon"
require conf/machine/include/arm/armv7a/tune-cortexa9.inc

SOC_FAMILY="netx4000"
require conf/machine/include/soc-family.inc

#############################################################################
# Bootloader
#############################################################################
EXTRA_IMAGEDEPENDS += "virtual/bootloader"

PREFERRED_PROVIDER_virtual/bootloader ??= "u-boot"
UBOOT_MACHINE ??= "netx4000_defconfig"
UBOOT_ENTRYPOINT ?= "0x40008000"

PREFERRED_PROVIDER_virtual/kernel ??= "linux-yocto"
PREFERRED_VERSION_linux-yocto = "5.10%"
PREFERRED_VERSION_linux-yocto-rt = "5.10%"

PREFERRED_PROVIDER_virtual/dtb ??= "device-tree"
MACHINE_ESSENTIAL_EXTRA_RDEPENDS:append += "device-tree"

#############################################################################
# Kernel
#############################################################################
KERNEL_CLASSES += "kernel-fitimage"
KERNEL_IMAGETYPES:append += "fitImage"
MACHINE_ESSENTIAL_EXTRA_RDEPENDS:append +=" \
	kernel-modules \
	libcifx cifxsample \
	dto-utils \
	kernel-module-netx4000-xc-stdmac kernel-module-mdio-xc-netx4000 hethmac \
"

# poky-lsb requires nfsd in kernel
KERNEL_EXTRA_FEATURES:append += " \
	${@ 'features/nfsd/nfsd-enable.scc' if '${DISTRO}' == 'poky-lsb' else '' } \
	features/firmware/firmware.scc \
	cfg/usb-mass-storage.scc \
"

#############################################################################
# Contents of boot partition (excluding rootfs)
#############################################################################
IMAGE_BOOT_FILES ??= "netx.rom fitImage-${MACHINE}.bin;fitImage"

WKS_FILE ?= "sdimage-bootpart.wks"
IMAGE_FSTYPES ?= "wic.bz2 wic.bmap"
