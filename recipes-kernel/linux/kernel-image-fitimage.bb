SUMMARY = "Package that contains kernel fitImage for installing into rootfs"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PROVIDES = "fitImage"

python __anonymous () {
    depends = d.getVar("DEPENDS", True)
    depends = "%s u-boot-mkimage-native dtc-native virtual/kernel" % depends
    d.setVar("DEPENDS", depends)

    image = d.getVar('INITRAMFS_IMAGE', True)
    if image:
        d.appendVarFlag('do_install', 'depends', ' ${INITRAMFS_IMAGE}:do_image_complete')

    d.appendVarFlag('do_install', 'depends', ' virtual/kernel:do_deploy')
}

FITIMAGE_DESCR ??= "U-Boot fitImage for ${DISTRO_NAME}/${PV}/${MACHINE}"
FITIMAGE_HASH_ALGO ??= "sha256"
FITIMAGE_SIGN_ALGO ??= "sha256,rsa4096"
FITIMAGE_SIGN ??= "0"
FITIMAGE_KERNEL_LOADADDR ??= "0xFFFFFFFF"
FITIMAGE_KERNEL_ENTRY ??= "0xFFFFFFFF"
FITIMAGE_INITRD_LOADADDR ??= "0xFFFFFFFF"
FITIMAGE_INITRD_ENTRY ??= "0xFFFFFFFF"

UBOOT_MKIMAGE_DTCOPTS ??= "${@bb.utils.contains('FITIMAGE_SIGN', '1', '-I dts -O dtb -p 2000', '', d)}"

fit_write_header() {
  cat << EOF >> ${1}
/dts-v1/;
/ {
        description = "${FITIMAGE_DESCR}";
        #address-cells = <1>;
EOF
}

fit_write_section_start() {
  echo "        ${1} {" >> ${2}
}

fit_write_section_end() {
  echo "        };" >> ${1}
}

fit_write_kernel_image() {
  mkdir -p kernel
  cp -L ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGETYPE} kernel/

  # Setup compression
  compression="none"

  # Setup load address and entrypoint
  load="${FITIMAGE_KERNEL_LOADADDR}"
  entry="${FITIMAGE_KERNEL_ENTRY}"

  cat << EOF >> ${1}
                kernel@1 {
                        description = "Linux kernel";
                        data = /incbin/("kernel/${KERNEL_IMAGETYPE}");
                        type = "kernel";
                        arch = "${ARCH}";
                        os = "linux";
                        compression = "${compression}";
                        load = <${load}>;
                        entry = <${entry}>;
                        hash@1 {
                                algo = "${FITIMAGE_HASH_ALGO}";
                        };
                };
EOF
}

fit_write_fdt_image() {
  mkdir -p fdt
  cp -L ${DEPLOY_DIR_IMAGE}/${KERNEL_DEVICETREE} fdt/.

  cat << EOF >> ${1}
                fdt@1 {
                        description = "Flattened Device Tree blob";
                        data = /incbin/("fdt/${KERNEL_DEVICETREE}");
                        type = "flat_dt";
                        arch = "${ARCH}";
                        compression = "none";
                        hash@1 {
                                algo = "${FITIMAGE_HASH_ALGO}";
                        };
                };
EOF
}

fit_write_ramdisk_image() {
  # Following snippets replace copy_initramfs from kernel.bbclass which already
  # decompress the initrd

  ramdisk_image=$(ls -1 ${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE}-${MACHINE}.cpio*)
  case $ramdisk_image in
    *gz)   compression="gzip"  ;;
    *bz2)  compression="bzip2" ;;
    *)     compression="none" ;;
# TODO: only gzip and bz2 are supported by barebox.
# mark others as none and hope kernel decompresses them
#    *lz4)  compression="lz4"   ;;
#    *lzo)  compression="lzo"   ;;
#    *lzma) compression="lzma"  ;;
#    *xz)   compression="xz"    ;;
  esac

  rm -rf ${B}/initramfs
  mkdir -p ${B}/initramfs
  cp ${ramdisk_image} ${B}/initramfs/
  ramdisk_image=$(basename "${ramdisk_image}")

  load="${FITIMAGE_INITRD_LOADADDR}"
  entry="${FITIMAGE_INITRD_ENTRY}"

  cat << EOF >> ${1}
                ramdisk@1 {
                        description = "ramdisk image";
                        data = /incbin/("initramfs/${ramdisk_image}");
                        type = "ramdisk";
                        arch = "${ARCH}";
                        os = "linux";
                        compression = "${compression}";
                        load = <${load}>;
                        entry = <${entry}>;
                        hash@1 {
                                algo = "${FITIMAGE_HASH_ALGO}";
                        };
                };
EOF
}

fit_write_configs_section() {
  fit_write_section_start "configurations" "${1}"

  cat << EOF >> ${1}
                default = "conf@1";
                conf@1 {
                        description = "Default configuration";
                        kernel = "kernel@1";
EOF

  if [ "x${KERNEL_DEVICETREE}" != "x" ]; then
    echo '                        fdt = "fdt@1";' >> ${1} 
  fi

  # Add optional initramfs
  if [ "x${INITRAMFS_IMAGE}" != "x" ]; then
    echo '                        ramdisk = "ramdisk@1";' >> ${1}
  fi

  # Add hash node
  cat << EOF >> ${1}
                        hash@1 {
                                algo = "${FITIMAGE_HASH_ALGO}";
                        };
                        ${2}
EOF

  echo "                };" >> ${1}


  fit_write_section_end "${1}"
}

inherit linux-kernel-base
KERNEL_VERSION="${@get_kernelversion_file("${STAGING_KERNEL_BUILDDIR}")}"

do_install() {
  fit_its_file="${B}/fitImage.its"
  fit_image="${B}/fitImage.bin"

  rm -f ${fit_its_file}

  if [ "${FITIMAGE_SIGN}" = "1" ]; then
    # Check parameter validity
    [ -d "${BAREBOX_SIGN_KEYDIR}" ] || bbfatal "Trying to sign fitImage without valid keydir '${BAREBOX_SIGN_KEYDIR}'"
    [ -e "${BAREBOX_SIGN_KEYDIR}/${BAREBOX_SIGN_KEYNAME}.key" ] || bbfatal "Trying to sign fitImage with non-existent key '${BAREBOX_SIGN_KEYDIR}/${BAREBOX_SIGN_KEYNAME}.key'"

    conf_signed_images="\"kernel\""
    [ "x${KERNEL_DEVICETREE}" = "x" ] || conf_signed_images="${conf_signed_images}, \"fdt\""
    [ "x${INITRAMFS_IMAGE}" = "x" ] || conf_signed_images="${conf_signed_images}, \"ramdisk\""

    signature_node="signature@1 {
                                algo = \"${FITIMAGE_SIGN_ALGO}\";
                                key-name-hint = \"${BAREBOX_SIGN_KEYNAME}\";
                                sign-images = ${conf_signed_images};
                        };"
  else
    signature_node=""
  fi

  fit_write_header "${fit_its_file}"

  #
  # Write image section
  #

  fit_write_section_start "images" "${fit_its_file}"

  # Add kernel
  fit_write_kernel_image "${fit_its_file}"

  # Add optional FDT
  if [ "x${KERNEL_DEVICETREE}" != "x" ]; then
    fit_write_fdt_image "${fit_its_file}" 
  fi

  # Add optional initramfs
  if [ "x${INITRAMFS_IMAGE}" != "x" ]; then
    fit_write_ramdisk_image "${fit_its_file}"
  fi

  fit_write_section_end "${fit_its_file}"

  #
  # Write config section
  #
  fit_write_configs_section "${fit_its_file}" "${signature_node}"

  echo "};" >> "${fit_its_file}"

  if [ "${FITIMAGE_SIGN}" = "1" ]; then
		uboot-mkimage \
			${@'-D "${UBOOT_MKIMAGE_DTCOPTS}"' if len('${UBOOT_MKIMAGE_DTCOPTS}') else ''} \
			-F -k "${BAREBOX_SIGN_KEYDIR}" \
			-f ${fit_its_file} ${fit_image}
  else
		uboot-mkimage \
			${@'-D "${UBOOT_MKIMAGE_DTCOPTS}"' if len('${UBOOT_MKIMAGE_DTCOPTS}') else ''} \
			-f ${fit_its_file} ${fit_image}
  fi

  install -d ${D}/boot
  install -m 0644 ${fit_image} ${D}/boot/fitImage-${KERNEL_VERSION}
  ln -sf /boot/fitImage-${KERNEL_VERSION} ${D}/boot/fitImage
}

PACKAGES="${PN}"
FILES_${PN}="/boot"

addtask deploy after do_populate_sysroot

do_deploy[vardepsexclude] = "DATETIME"
do_deploy() {
  fit_its_file="${B}/fitImage.its"
  fit_image="${B}/fitImage.bin"

  # Update deploy directory
  echo "Copying fit-image.its source file..."
  its_base_name="fitImage-${PV}-${PR}-${MACHINE}-${DATETIME}"
  its_symlink_name=fitImage.its
  install -m 0644 ${fit_its_file} ${DEPLOY_DIR_IMAGE}/${its_base_name}.its

  linux_bin_base_name="fitImage-${PV}-${PR}-${MACHINE}-${DATETIME}"
  linux_bin_symlink_name=fitImage
  install -m 0644 ${fit_image} ${DEPLOY_DIR_IMAGE}/${linux_bin_base_name}.bin

  cd ${DEPLOY_DIR_IMAGE}
  ln -sf ${its_base_name}.its ${its_symlink_name}
  ln -sf ${linux_bin_base_name}.bin ${linux_bin_symlink_name}
}

PACKAGE_ARCH="${MACHINE_ARCH}"

do_fetch[noexec] = "1"
do_unpack[depends] += "virtual/kernel:do_patch"
do_unpack[noexec] = "1"
do_patch[noexec] = "1"
do_populate_sysroot[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"
