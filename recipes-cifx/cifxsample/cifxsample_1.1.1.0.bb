SUMMARY = "cifX device driver example applications for Hilscher netX devices"
HOMEPAGE = "www.hilscher.com"
LICENSE = "CLOSED"

DEPENDS = "libcifx"

SRC_URI = "file://cifxlinuxsample.c"

inherit useradd

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "-r cifx"

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"

FILES:${PN} = "/opt/cifx/demo"

do_compile() {
  ${CC} ${LDFLAGS} cifxlinuxsample.c -o cifxsample -I=/usr/include/cifx -lcifx
}

do_install() {
  install -d ${D}/opt/cifx/example
  install cifxsample ${D}/opt/cifx/example

  chgrp -R cifx ${D}/opt/cifx
  chmod 0775 ${D}/opt/cifx
}

FILES:${PN} = "/opt/cifx"
