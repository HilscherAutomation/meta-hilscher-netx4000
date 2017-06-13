SUMMARY = "Barebox (formerly known as u-boot-v2) is a bootloader that inherits the best of U-Boot and the Linux kernel"
HOMEPAGE = "http://barebox.org/"
SECTION = "bootloaders"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM ?= "file://COPYING;md5=057bf9e50e1ca857d0eb97bfe4ba8e5d"

inherit kernel-arch deploy cml1 barebox-sign

PROVIDES = "virtual/bootloader barebox netx.rom u-boot"

COMPATIBLE_MACHINE = "netx4000"

GIT_BAREBOX_REPO ?= "github.com/hilscher/netx4000-barebox.git;protocol=https"

SRC_URI = "git://${GIT_BAREBOX_REPO};branch=${BRANCH} \
           ${@bb.utils.contains('BAREBOX_SIGN_ENFORCE', '1', 'file://enforce_signature_verification.cfg', '',d)}"

S = "${WORKDIR}/git"

PACKAGE_ARCH = "${MACHINE_ARCH}"
PACKAGES = "${PN}"

EXTRA_OEMAKE = "CROSS_COMPILE=${TARGET_PREFIX}"

DEPENDS += "kern-tools-native"

# returns all the elements from the src uri that are .cfg files
def find_cfgs(d):
    sources=src_patches(d, True)
    sources_list=[]
    for s in sources:
        if s.endswith('.cfg'):
            sources_list.append(s)

    return sources_list

do_configure_prepend() {
	[ -r ${WORKDIR}/defconfig ] && {
		bbnote "Copying/Replacing the netx4000_defconfig"
		cp ${WORKDIR}/defconfig ${S}/arch/arm/configs/netx4000_defconfig
	}

	if [ "${ENABLE_DDR_ECC}" = "yes" ]; then
		bbnote "Enable DDR ECC"
		sed -i -e 's,.*\(CONFIG_ENABLE_DDR_ECC\).*,\1=y,g' ${S}/arch/arm/configs/netx4000_defconfig
	else
		bbnote "Disable DDR ECC"
		sed -i -e 's,.*\(CONFIG_ENABLE_DDR_ECC\).*,# \1 is not set,g' ${S}/arch/arm/configs/netx4000_defconfig
	fi

	oe_runmake netx4000_defconfig

	[ -n "$(ls ${DTS_DIR})" ] && {
		bbnote "Copying/Replacing device tree files"
		cp -r ${WORKDIR}/${DTS_DIR}/* ${S}/arch/arm/dts
	}

	# Change used device tree in config
	sed -i -e 's,\(CONFIG_BUILTIN_DTB_NAME=\).*,\1"${@os.path.splitext(os.path.basename("${BAREBOX_DEVICETREE}"))[0]}",g' ${S}/.config

	# Apply possible configuration fragments
	merge_config.sh -m .config ${@" ".join(find_cfgs(d))}
}

do_compile_prepend() {
  unset LDFLAGS
}

BUILD_ID="${DATETIME}"
BUILD_ID[vardepsexclude] = "DATETIME"

do_deploy () {
  install ${B}/barebox ${DEPLOYDIR}/${PN}-${PV}-${BUILD_ID}.elf
  ln -sf ${PN}-${PV}-${BUILD_ID}.elf ${DEPLOYDIR}/${PN}-${PV}.elf

  if [ -e "${B}/barebox.netx4000" ]; then
    install ${B}/barebox.netx4000 ${DEPLOYDIR}/${PN}-${PV}-${BUILD_ID}
    ln -sf ${PN}-${PV}-${BUILD_ID} ${DEPLOYDIR}/netx.rom
  fi
}
addtask deploy before do_build after do_compile
