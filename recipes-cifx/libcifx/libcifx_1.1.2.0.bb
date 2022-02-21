DESCRIPTION = "cifX device driver for Hilscher netX devices"
HOMEPAGE = "http://www.hilscher.com"
LICENSE = "CLOSED"

inherit useradd

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "-r cifx"

SRC_URI = "file://libcifx-${TCLIBC}-${PV}.tar.bz2"

S = "${WORKDIR}/libcifx-${TCLIBC}-${PV}"

do_install() {
  cp -r ${S}/* ${D}
  chmod 0755 -R ${D}
  chown 0:0 -R ${D}

  chgrp -R cifx ${D}/opt/cifx
  chmod 0775 ${D}/opt/cifx
}

FILES:${PN} += "/opt/cifx/deviceconfig /opt/cifx/*.bin"
INSANE_SKIP:${PN} = "already-stripped"

COMPATIBLE_HOST = "arm.*-linux"
