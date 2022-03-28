SUMMARY = "Hilscher BSP device trees"
DESCRIPTION = "Hilscher BSP device trees from within layer."
SECTION = "bsp"

# the device trees from within the layer are licensed as MIT, kernel includes are GPL
LICENSE = "MIT & GPL-2.0-only"
LIC_FILES_CHKSUM = " \
	file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302 \
	file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6 \
"

inherit devicetree

S = "${WORKDIR}/src"

devicetree_do_install:append() {
	default_dtb=${MACHINE}

	install -d ${D}/boot/dt-overlays
	for DTB_FILE in `ls *.dtbo`; do
		mv ${D}/boot/devicetree/${DTB_FILE} ${D}/boot/dt-overlays
	done

	find ${D}/boot/devicetree ! -name "${default_dtb}.dtb" -type f -exec rm -f {} +
}

devicetree_do_deploy() {
	cp -a ${D}/boot/* ${DEPLOYDIR}
}

FILES:${PN} += "boot/dt-overlays"

# --------------------------------------
# nxhx4000-jtag-plus-revx boards

COMPATIBLE_MACHINE:nxhx4000-jtag-plus-revx = ".*"
SRC_URI:append:nxhx4000-jtag-plus-revx = " \
	file://src/nxhx4000-jtag-plus-revx.dts \
	file://src/adc0.dtso \
	file://src/adc1.dtso \
	file://src/i2c1.dtso \
	file://src/nxhx4000-rev3-spi0.dtso \
	file://src/can0.dtso \
	file://src/uart1.dtso \
	file://src/qspi_flash.dtso \
	file://src/xceth0.dtso \
	file://src/xceth1.dtso \
"

SRC_URI:append:nxhx4000-jtag-plus-rev4 = " \
	file://src/nxhx4000-jtag-plus-rev4.dts \
"

SRC_URI:append:nxhx4000-jtag-plus-rev5 = " \
	file://src/nxhx4000-jtag-plus-rev5.dts \
"

# --------------------------------------
# ndeb4000-revx based boards

COMPATIBLE_MACHINE:ndeb4000-revx = ".*"

SRC_URI:append:ndeb4000-rev1 = "\
	file://src/ndcm4000-rev1.dts \
	file://src/ndeb4000-rev1.dts \
"

# --------------------------------------
# ndcm4000-revx based boards

COMPATIBLE_MACHINE:ndcm4000-revx = ".*"
SRC_URI:append:ndcm4000-revx = " \
        file://src/uart1.dtso \
        file://src/can0.dtso \
        file://src/gmac0.dtso \
        file://src/gmac1.dtso \
        file://src/pcie-rc.dtso \
        file://src/adc0.dtso \
        file://src/adc1.dtso \
        file://src/xceth0.dtso \
        file://src/xceth1.dtso \
"

SRC_URI:append:ndcm4000-rev1 = "\
	file://src/ndcm4000-rev1.dts \
"
