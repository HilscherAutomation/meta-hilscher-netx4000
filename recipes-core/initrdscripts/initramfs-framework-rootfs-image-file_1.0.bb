SUMMARY = "Use an image file as rootfs"
HOMEPAGE = "http://www.hilscher.com"

LICENSE = "Hilscher-SLA"
LIC_FILES_CHKSUM = "file://${HILSCHER_LICENSE_PATH}/Hilscher-SLA;md5=37f1a875d1f12ae4f3d9c5ef16ea4030"

inherit allarch

SRC_URI = "file://rootfs_image_file"

S = "${WORKDIR}"

do_install () {
  install -d ${D}/init.d
  install -m 500 ${S}/rootfs_image_file ${D}/init.d/89-rootfs_image_file
}

PACKAGES = "${PN}"
FILES_${PN} = "/init.d/*"
