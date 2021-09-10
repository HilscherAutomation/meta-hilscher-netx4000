SUMMARY = "DT-Overlay utils"
HOMEPAGE = "www.hilscher.com"

LICENSE = "Hilscher-SLA"
LIC_FILES_CHKSUM = "file://${HILSCHER_LICENSE_PATH}/Hilscher-SLA;md5=37f1a875d1f12ae4f3d9c5ef16ea4030"

inherit allarch

SRC_URI = " \
	file://dto \
	file://dto-start-stop \
	file://dto.service \
	file://etc-default-dto \
"

RDEPENDS:${PN} = "dtc"

S = "${WORKDIR}"

do_install () {
	install -d ${D}${sbindir}
	install -m 755 ${S}/dto ${D}${sbindir}/dto

	if [ -s ${S}/etc-default-dto ]; then
		install -d ${D}${sysconfdir}/default
		install -m 644 ${S}/etc-default-dto ${D}${sysconfdir}/default/dto
	fi

	if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
		install -m 755 ${S}/dto-start-stop ${D}${sbindir}
		install -d ${D}${systemd_unitdir}/system
		install -m 644 ${S}/dto.service ${D}${systemd_unitdir}/system/dto.service
	else
		install -d ${D}${sysconfdir}/init.d
		install -m 755 ${S}/dto-start-stop ${D}${sysconfdir}/init.d/dto
	fi
}

CONFFILES:${PN} += "${sysconfdir}/default/dto"

FILES:${PN} = "\
	${sbindir} \
	${sysconfdir} \
	${systemd_unitdir} \
"

PACKAGES = "${PN}"
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit systemd update-rc.d

INITSCRIPT_NAME   = "dto"
INITSCRIPT_PARAMS = "defaults 0 99"

SYSTEMD_SERVICE:${PN} = "dto.service"
