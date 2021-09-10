SUMMARY = "Build xc-firmware binary of hethmac"
HOMEPAGE = "www.hilscher.com"
LICENSE = "Hilscher-SLA"
LIC_FILES_CHKSUM = "file://${HILSCHER_LICENSE_PATH}/Hilscher-SLA;md5=37f1a875d1f12ae4f3d9c5ef16ea4030"

SRC_URI = " \
	file://Makefile;subdir=src \
	file://rpec_ethmac_rpec0.c;subdir=src \
	file://rpec_ethmac_rpec1.c;subdir=src \
	file://rpec_ethmac_rpec2.c;subdir=src \
	file://rpec_ethmac_rpec3.c;subdir=src \
	file://rpu_ethmac0.c;subdir=src \
	file://rpu_ethmac1.c;subdir=src \
	file://rpu_ethmac2.c;subdir=src \
	file://rpu_ethmac3.c;subdir=src \
	file://tpec_ethmac_tpec0.c;subdir=src \
	file://tpec_ethmac_tpec1.c;subdir=src \
	file://tpec_ethmac_tpec2.c;subdir=src \
	file://tpec_ethmac_tpec3.c;subdir=src \
	file://tpu_ethmac0.c;subdir=src \
	file://tpu_ethmac1.c;subdir=src \
	file://tpu_ethmac2.c;subdir=src \
	file://tpu_ethmac3.c;subdir=src \
"

PACKAGE_ARCH="${MACHINE_ARCH}"

DEPENDS = "dtc-native"

S = "${WORKDIR}/src"

# Disable compiler optimization
# Enable ELF section for each variable
# Enable big-endian
CFLAGS = "-O0 -fdata-sections -mbig-endian"

do_create_dts_file() {
	xc=$1

	echo "/dts-v1/;"
	echo ""
	echo "/ {"
	echo "    firmware = \"${PN}-xc${xc}\";"
	echo "    version = \"${PV}\";"
	echo ""
	echo "    xc$xc {"
	for unit in rpec tpec rpu tpu; do
		binfile="$(ls ${S}/*${xc}.bin | grep $unit)"
		echo "        $unit$xc = /incbin/(\"$binfile\");"
	done
	echo "    };"
	echo "};"
}

do_compile_firmware() {
	xc=$1

	# Creating devicetree source file for firmware binary
	do_create_dts_file $xc > ${WORKDIR}/${PN}-xc${xc}_${PV}.dts

	# Compiling devicetree source file
	dtc ${WORKDIR}/${PN}-xc${xc}_${PV}.dts -O dtb -o ${WORKDIR}/${PN}-xc${xc}_${PV}.bin
}

do_compile_append() {
	for xc in 0 1 2 3; do
		do_compile_firmware $xc
	done
}

do_install() {
  install -d ${D}${nonarch_base_libdir}/firmware
  install -m 400 ${WORKDIR}/*.bin  ${D}${nonarch_base_libdir}/firmware/

  #we need a unique name for the kernel driver so we simply add a link per firmware
  for file in ${D}${nonarch_base_libdir}/firmware/*.bin;  do
    file=$(basename ${file})
    number=$(echo ${file} | cut -d "-" -f 2  | cut -d "_" -f 1 | grep -oE '[0-9]')
    linkname="hethmac-xc${number}.bin"
    ln -s ${file} ${D}${nonarch_base_libdir}/firmware/${linkname}
  done
}

FILES_${PN} = "${nonarch_base_libdir}/firmware/*.bin"
