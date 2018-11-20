#!/bin/bash -e

export ARCH=arm
export CROSS_COMPILE=arm-linux-gnueabihf-

echo Using the following parameters ...
echo ----------------------------------
cat ./ubuntu.cfg
echo ----------------------------------
source ./ubuntu.cfg

default_barebox_args=""
default_rootfs_args=""
default_sdimg_args="$(basename $DEVICE_TREE)"

do_build_barebox() {
	echo "Building bootloader"

	if [ ! -d "netx4000-barebox" ]; then
		git clone https://github.com/Hilscher/netx4000-barebox.git
	fi

	# Barebox destination
	export BAREBOX_DEST=$(pwd)/build/barebox

	pushd netx4000-barebox
	git checkout $BAREBOX_BRANCH
	git pull

	# Copy DTS files
	cp -r ../../../meta-hilscher-netx4000/files/dts/* arch/arm/dts/netx4000/

	# Create default config if neccessary
	if [ ! -e .config ]; then
		make netx4000_defconfig

		# Select correct machine device tree file
		sed -i -e "s,\(CONFIG_BUILTIN_DTB_NAME=\).*,\1\"${DEVICE_TREE}\",g" .config
	fi

	make ${@:-${default_barebox_args}}

	if [ "$*" == "clean" ]; then
		rm -f barebox.netx4000
	fi

	if [ -e barebox.netx4000 ]; then
		mkdir -p ${BAREBOX_DEST}
		cp barebox.netx4000 ${BAREBOX_DEST}/netx.rom
	fi

	popd
}

do_build_kernel() {
	echo "Building kernel package"

	if [ ! -d "netx4000-linux" ]; then
		git clone https://github.com/Hilscher/netx4000-linux.git
	fi

	if [ ! -d "yocto-kernel-cache" ]; then
		git clone https://git.yoctoproject.org/git/yocto-kernel-cache
	fi

	# Kernel destination
	export KERNEL_DEST=$(pwd)/build/kernel

	pushd yocto-kernel-cache
	git checkout $KERNEL_CACHE_BRANCH
	git pull
	popd

	pushd netx4000-linux
	git checkout $KERNEL_BRANCH
	git pull

	# Copy DTS files
	cp -r ../../../meta-hilscher-netx4000/files/dts/* arch/arm/boot/dts/netx4000/

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
	barebox)
		shift
		do_build_barebox $@
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
		do_build_barebox
		do_build_kernel
		do_create_rootfs
		do_create_sdimg
		;;
esac
