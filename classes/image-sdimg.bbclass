# SD card image names
SDIMG = "${IMGDEPLOYDIR}/${IMAGE_NAME}.sdimg"
SDIMG_LINK_NAME = "${IMGDEPLOYDIR}/${PN}-${MACHINE}.sdimg"

# Use an uncompressed ext4 by default as rootfs
SDIMG_ROOTFS_TYPE ?= "ext4"
SDIMG_ROOTFS = "${IMGDEPLOYDIR}/${IMAGE_NAME}.rootfs.${SDIMG_ROOTFS_TYPE}"

# This image depends on the rootfs image
IMAGE_TYPEDEP_sdimg = "${SDIMG_ROOTFS_TYPE}"

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
SDIMG_PARTITION_ALIGNMENT = "4096"

# Boot partition size [in KiB]
SDIMG_BOOT_SIZE ?= "65536"

do_image_sdimg[depends] += " \
	dosfstools-native:do_populate_sysroot \
	parted-native:do_populate_sysroot \
	mtools-native:do_populate_sysroot \
	coreutils-native:do_populate_sysroot \
"

IMAGE_CMD_sdimg () {
	# Round up boot partition size to the alignment size as well
	SDIMG_BOOT_SIZE_ALIGNED=$(expr ${SDIMG_BOOT_SIZE} + ${SDIMG_PARTITION_ALIGNMENT} - 1)
	SDIMG_BOOT_SIZE_ALIGNED=$(expr ${SDIMG_BOOT_SIZE_ALIGNED} - ${SDIMG_BOOT_SIZE_ALIGNED} % ${SDIMG_PARTITION_ALIGNMENT})

	[ -f ${SDIMG_ROOTFS} ] || bberror "Cannot access the SDIMG_ROOTFS '${SDIMG_ROOTFS}'"
	SDIMG_ROOTFS_SIZE=$(du -bks ${SDIMG_ROOTFS} | awk '{print $1}')

	# Round up RootFS partition size to the alignment size as well
	SDIMG_ROOTFS_SIZE_ALIGNED=$(expr ${SDIMG_ROOTFS_SIZE} + ${SDIMG_PARTITION_ALIGNMENT} - 1)
	SDIMG_ROOTFS_SIZE_ALIGNED=$(expr ${SDIMG_ROOTFS_SIZE_ALIGNED} - ${SDIMG_ROOTFS_SIZE_ALIGNED} % ${SDIMG_PARTITION_ALIGNMENT})

	SDIMG_SIZE=$(expr ${SDIMG_PARTITION_ALIGNMENT} + ${SDIMG_BOOT_SIZE_ALIGNED} + ${SDIMG_ROOTFS_SIZE_ALIGNED})

	bbnote "Creating a image file for SD-Cards with Boot partition ${SDIMG_BOOT_SIZE_ALIGNED} KiB and RootFS ${SDIMG_ROOTFS_SIZE_ALIGNED} KiB"

	# Initialize sdcard image file
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
	rm -f ${WORKDIR}/boot.img
	mkfs.vfat -n "BOOT" -S 512 -C ${WORKDIR}/boot.img $BOOT_BLOCKS

	# Copy the boot files
	for file in ${IMAGE_BOOT_FILES}; do
		[ -f ${DEPLOY_DIR_IMAGE}/${file} ] || bberror "Cannot access the IMAGE_BOOT_FILES file '${DEPLOY_DIR_IMAGE}/${file}'"
		[ -n "${IMAGE_BOOT_FILES}" ] && mcopy -i ${WORKDIR}/boot.img -s ${DEPLOY_DIR_IMAGE}/${file} ::
	done

	# Add stamp file
	echo "${IMAGE_NAME}" > ${WORKDIR}/image-version-info
	mcopy -i ${WORKDIR}/boot.img -s ${WORKDIR}/image-version-info ::

	# Burn the boot partition
	dd if=${WORKDIR}/boot.img of=${SDIMG} conv=notrunc seek=1 bs=$(expr ${SDIMG_PARTITION_ALIGNMENT} \* 1024) && sync || {
		bberror "Burning the boot partition failed!"
	}

	# Burn the rootfs partition
	if [ "${SDIMG_ROOTFS_TYPE}" = "ext4" ]; then
		dd if=${SDIMG_ROOTFS} of=${SDIMG} conv=notrunc seek=1 bs=$(expr ${SDIMG_PARTITION_ALIGNMENT} \* 1024 \+ ${SDIMG_BOOT_SIZE_ALIGNED} \* 1024 ) && sync || {
			bberror "Burning the rootfs partition failed!"
		}
	else
		bberror "SDIMG_ROOTFS_TYPE ${SDIMG_ROOTFS_TYPE} is currently not supported!"
	fi

	# Create a symlink
	ln -sf $(basename ${SDIMG}) ${SDIMG_LINK_NAME}
}
