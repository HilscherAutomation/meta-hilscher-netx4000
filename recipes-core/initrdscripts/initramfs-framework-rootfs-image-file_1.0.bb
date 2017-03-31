SUMMARY = "Use an image file as rootfs"
HOMEPAGE = "http://www.hilscher.com"

LICENSE = "Hilscher-SLA"
LIC_FILES_CHKSUM = "file://${HILSCHER_LICENSE_PATH}/Hilscher-SLA;md5=07eea1d7f15431bf435e8e9a83c21006"

inherit allarch

SRC_URI = "file://rootfs_image_file"

S = "${WORKDIR}"

do_install () {
  install -d ${D}/init.d
  install -m 500 ${S}/rootfs_image_file ${D}/init.d/89-rootfs_image_file
}

PACKAGES = "${PN}"
FILES_${PN} = "/init.d/*"
