SUMMARY = "Resize root filesystem on first boot to use whole device (SD/MMC)"
HOMEPAGE = "http://www.hilscher.com"

LICENSE = "Hilscher-SLA"
LIC_FILES_CHKSUM = "file://${HILSCHER_LICENSE_PATH}/Hilscher-SLA;md5=37f1a875d1f12ae4f3d9c5ef16ea4030"

inherit allarch

RDEPENDS_${PN} = "e2fsprogs-e2fsck e2fsprogs-resize2fs parted"

SRC_URI = "file://resize_rootfs"

S = "${WORKDIR}"

do_install () {
  install -d ${D}/init.d
  install -m 500 ${S}/resize_rootfs ${D}/init.d/10-resize_rootfs
}

PACKAGES = "${PN}"
FILES_${PN} = "/init.d/*"
