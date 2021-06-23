#!/bin/bash -e

export ARCH=arm
export CROSS_COMPILE=arm-linux-gnueabihf-

git config --global user.email "build@localhost"
git config --global user.name "build"

echo Using the following parameters ...
echo ----------------------------------
cat ./ubuntu.cfg
echo ----------------------------------
source ./ubuntu.cfg

default_uboot_args=""
default_rootfs_args=""
default_sdimg_args="$(basename $DEVICE_TREE)"

do_build_uboot() {
	echo "Building bootloader"

	if [ ! -d "netx4000-uboot" ]; then
		git clone https://source.denx.de/u-boot/u-boot.git netx4000-uboot
	else
		cd netx4000-uboot
		git fetch
		cd -
	fi

	# u-boot destination
	export UBOOT_DEST=$(pwd)/build/uboot

	pushd netx4000-uboot
	git checkout master
	if git show-ref --verify --quiet refs/heads/build; then
		git branch -D build
	fi
	git checkout $UBOOT_VERSION -b build
	git am ../../../meta-hilscher-netx4000/recipes-bsp/u-boot/files/netx4000/*.patch

	# Copy DTS files
	cp -r ../../../meta-hilscher-netx4000/recipes-bsp/device-tree/files/src/* arch/arm/dts/

	# Create default config if neccessary
	if [ ! -e .config ]; then
		make netx4000_defconfig

		# Enable legacy format for boot.scr and uImage
		sed -e 's/.*CONFIG_LEGACY_IMAGE_FORMAT.*/CONFIG_LEGACY_IMAGE_FORMAT=y/g' \
		    -i .config

		# Select correct machine device tree file
		sed -i -e "s,\(CONFIG_BUILTIN_DTB_NAME=\).*,\1\"${DEVICE_TREE}\",g" .config
	fi

	make ${@:-${default_uboot_args}}

	# Create boot script
	cat <<EOF>boot.cmd
setenv fdt_addr "0x60000000"
load mmc 0:2 \${loadaddr} uImage
load mmc 0:1 \${fdt_addr} oftree
setenv bootargs "root=/dev/mmcblk0p2 rw rootwait console=ttyAMA0,115200 earlyprintk"
bootm \${loadaddr} - \${fdt_addr}
EOF
	tools/mkimage -A arm -O linux -T script -C none -n "Boot ubuntu" -d boot.cmd boot.scr

	if [ "$*" == "clean" ]; then
		rm -f u-boot.netx4000
	fi

	if [ -e u-boot.netx4000 ]; then
		mkdir -p ${UBOOT_DEST}
		cp u-boot.netx4000 ${UBOOT_DEST}/netx.rom
	fi

	if [ -e boot.scr ]; then
		mkdir -p ${UBOOT_DEST}
		cp boot.scr ${UBOOT_DEST}/
	fi

	popd
}

do_build_kernel() {
	echo "Building kernel package"

	if [ ! -d "netx4000-linux" ]; then
		git clone https://kernel.googlesource.com/pub/scm/linux/kernel/git/stable/linux.git netx4000-linux
	else
		cd netx4000-linux
		git fetch
		cd -
	fi

	if [ ! -d "yocto-kernel-cache" ]; then
		git clone https://git.yoctoproject.org/git/yocto-kernel-cache
	else
		cd yocto-kernel-cache
		git fetch
		cd -
	fi

	# Kernel destination
	export KERNEL_DEST=$(pwd)/build/kernel

	pushd yocto-kernel-cache
	git checkout $KERNEL_CACHE_BRANCH
	popd

	pushd netx4000-linux
	git checkout master
	if git show-ref --verify --quiet refs/heads/build; then
		git branch -D build
	fi
	git checkout $KERNEL_VERSION -b build
	git am ../../../meta-hilscher-netx4000/recipes-kernel/linux/files/netx4000/*.patch

	# Copy DTS files
	cp -r ../../../meta-hilscher-netx4000/recipes-bsp/device-tree/files/src/* arch/arm/boot/dts/netx4000/

	# Create default config if neccessary
	if [ ! -e .config ]; then
		scripts/kconfig/merge_config.sh arch/arm/configs/netx4000_defconfig ${KERNEL_FRAGMENTS} > merge.log
	fi

	if [ $@ ]; then
		export ARCH=arm
		export CROSS_COMPILE=arm-linux-gnueabihf-
		make $@
	else
		make-kpkg $KERNEL_PKG_ARGS
		mkdir -p ${KERNEL_DEST}
		rm -f ${KERNEL_DEST}/*.deb
		mv ../*.deb ${KERNEL_DEST}

		export ARCH=arm
		export CROSS_COMPILE=arm-linux-gnueabihf-
		make ${DEVICE_TREE}.dtb
		[ -e ../build/oftree ] && rm ../build/oftree
		cp arch/arm/boot/dts/${DEVICE_TREE}.dtb ../build/oftree
	fi

	popd
}

do_create_rootfs() {
	pushd build
	../build_rootfs.sh ${@:-${default_rootfs_args}}
	popd
}

do_create_sdimg() {
	pushd build
	../build_sdimg.sh  ${@:-${default_sdimg_args}}
	popd
}

case $1 in
	uboot)
		shift
		do_build_uboot $@
		;;
	kernel)
		shift
		do_build_kernel $@
		;;
	rootfs)
		shift
		do_create_rootfs $@
		;;
	sdimg)
		shift
		do_create_sdimg $@
		;;
	*)
		do_build_uboot
		do_build_kernel
		do_create_rootfs
		do_create_sdimg
		;;
esac
