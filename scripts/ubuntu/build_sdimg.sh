#!/bin/bash -e

IMAGE_NAME="netx4000-ubuntu-18.04"
MACHINE=${1:="unknown"}

# SD card image name
SDIMG="${IMAGE_NAME}-${MACHINE}.sdimg"

# Contents of boot partition 
# Note: The basename will be used as destination file name!
SDIMG_BOOT_FILES="uboot/netx.rom uboot/boot.scr oftree"

# ext4 rootfs image name
ROOTFS_IMAGE="${IMAGE_NAME}-rootfs.ext4"

# Size of ext4 rootfs image [in MiB]
ROOTFS_SIZE="2048"

# Image file for boot partition
SDIMG_BOOT="${IMAGE_NAME}-boot.vfat"

# Image file for rootfs partition
SDIMG_ROOTFS="${ROOTFS_IMAGE}"

#
# SD-Card Layout
#
# +--------------+ SDIMG_SIZE = SDIMG_PARTITION_ALIGNMENT + SDIMG_BOOT_SIZE_ALIGNED + SDIMG_ROOTFS_SIZE_ALIGNED
# |              |
# |    rootfs    |
# |              |
# +--------------+ SDIMG_PARTITION_ALIGNMENT + SDIMG_BOOT_SIZE_ALIGNED
# |              |
# |     boot     |
# |              |
# +--------------+ SDIMG_PARTITION_ALIGNMENT (4MiB)
# |              |
# |   alignment  |
# |              |
# +--------------+ 0
#

# Set alignment to 4MiB [in KiB]
SDIMG_PARTITION_ALIGNMENT="4096"

# Boot partition size [in KiB]
SDIMG_BOOT_SIZE="65536"

create_sdimg () {
	# Round up boot partition size to the alignment size as well
	SDIMG_BOOT_SIZE_ALIGNED=$(expr ${SDIMG_BOOT_SIZE} + ${SDIMG_PARTITION_ALIGNMENT} - 1)
	SDIMG_BOOT_SIZE_ALIGNED=$(expr ${SDIMG_BOOT_SIZE_ALIGNED} - ${SDIMG_BOOT_SIZE_ALIGNED} % ${SDIMG_PARTITION_ALIGNMENT})

	[ -f ${SDIMG_ROOTFS} ] || echo "Cannot access the SDIMG_ROOTFS '${SDIMG_ROOTFS}'"
	SDIMG_ROOTFS_SIZE=$(du -bks ${SDIMG_ROOTFS} | awk '{print $1}')

	# Round up RootFS partition size to the alignment size as well
	SDIMG_ROOTFS_SIZE_ALIGNED=$(expr ${SDIMG_ROOTFS_SIZE} + ${SDIMG_PARTITION_ALIGNMENT} - 1)
	SDIMG_ROOTFS_SIZE_ALIGNED=$(expr ${SDIMG_ROOTFS_SIZE_ALIGNED} - ${SDIMG_ROOTFS_SIZE_ALIGNED} % ${SDIMG_PARTITION_ALIGNMENT})

	SDIMG_SIZE=$(expr ${SDIMG_PARTITION_ALIGNMENT} + ${SDIMG_BOOT_SIZE_ALIGNED} + ${SDIMG_ROOTFS_SIZE_ALIGNED})

	echo "Creating a image file for SD-Cards with Boot partition ${SDIMG_BOOT_SIZE_ALIGNED} KiB and RootFS ${SDIMG_ROOTFS_SIZE_ALIGNED} KiB"

	# Initialize sdcard image file
	rm -f ${SDIMG}
	dd if=/dev/zero of=${SDIMG} bs=1024 count=0 seek=${SDIMG_SIZE}

	# Create partition table
	parted -s ${SDIMG} mklabel msdos

	# Create boot partition and mark it as bootable
	parted -s ${SDIMG} unit KiB mkpart primary fat32 ${SDIMG_PARTITION_ALIGNMENT} $(expr ${SDIMG_BOOT_SIZE_ALIGNED} \+ ${SDIMG_PARTITION_ALIGNMENT})
	parted -s ${SDIMG} set 1 boot on

	# Create rootfs partition to the end of disk
	parted -s ${SDIMG} -- unit KiB mkpart primary ext4 $(expr ${SDIMG_PARTITION_ALIGNMENT} \+ ${SDIMG_BOOT_SIZE_ALIGNED}) -1s

	# Create a vfat image file for the boot partition
	BOOT_BLOCKS=$(parted -s ${SDIMG} unit b print | awk '/ 1 / { print substr($4, 1, length($4 -1)) / 1024 }')
	rm -f ${SDIMG_BOOT}
	mkfs.vfat -n "BOOT" -S 512 -C ${SDIMG_BOOT} $BOOT_BLOCKS

	# Copy the boot files
	for file in ${SDIMG_BOOT_FILES}; do
		[ -f ${file} ] || {
			echo "Error: Cannot access the SDIMG_BOOT_FILES '${file}'"
			exit 1
		}
		mcopy -i ${SDIMG_BOOT} -s ${file} ::
	done

	# Add stamp file
	echo "${IMAGE_NAME}-$(date +"%Y%m%d%H%M%S")" > image-version-info
	mcopy -i ${SDIMG_BOOT} -s image-version-info ::
	rm image-version-info

	# Burn the boot partition
	dd if=${SDIMG_BOOT} of=${SDIMG} conv=notrunc seek=1 bs=$(expr ${SDIMG_PARTITION_ALIGNMENT} \* 1024) && sync || {
		echo "Error: Burning the boot partition failed!"
		exit 1
	}
	rm -f ${SDIMG_BOOT}

	# Burn the rootfs partition
	dd if=${SDIMG_ROOTFS} of=${SDIMG} conv=notrunc seek=1 bs=$(expr ${SDIMG_PARTITION_ALIGNMENT} \* 1024 \+ ${SDIMG_BOOT_SIZE_ALIGNED} \* 1024 ) && sync || {
		echo "Error: Burning the rootfs partition failed!"
		exit 1
	}
	rm -f ${SDIMG_ROOTFS}
}

create_ext4_rootfs_image() {
	# Initialize rootfs image file
	rm -f ${ROOTFS_IMAGE}
	dd if=/dev/zero of=${ROOTFS_IMAGE} bs=1M count=0 seek=${ROOTFS_SIZE}

	# Format to ext4
	mkfs.ext4 ${ROOTFS_IMAGE}

	# Mount and copy rootfs
	mp=$(mktemp -d -p ./)
	mount ${ROOTFS_IMAGE} ${mp}
	tar xf ${IMAGE_NAME}-rootfs.tar.bz2 -C ${mp}

	# Cleanup
	umount ${mp}
	rm -rf ${mp}
}

# Main function ...

create_ext4_rootfs_image
create_sdimg


