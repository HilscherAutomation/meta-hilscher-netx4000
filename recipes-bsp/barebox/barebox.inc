SUMMARY = "Barebox (formerly known as u-boot-v2) is a bootloader that inherits the best of U-Boot and the Linux kernel"
HOMEPAGE = "http://barebox.org/"
SECTION = "bootloaders"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM ?= "file://COPYING;md5=057bf9e50e1ca857d0eb97bfe4ba8e5d"

# Check for mandatory parameters
python() {
    if d.getVar('DDR_RAM', True) is None:
        bb.error("Unspecified 'DDR_RAM' parameter.")
        bb.error("Please define it in machine configuration.")
        bb.error("  DDR_RAM = 'IS43TR16256AL_1GIB'")
        bb.fatal("  DDR_RAM = 'AS4C512M16D3L_12_2GIB'")

    if d.getVar('ENABLE_DDR_ECC', True) is None:
        bb.error("Unspecified 'ENABLE_DDR_ECC' parameter.")
        bb.error("Please define it in machine configuration.")
        bb.fatal("  ENABLE_DDR_ECC = 'yes' or 'no'")
}

BAREBOX_SIGN_ENFORCE ?= "${PLATFORM_SIGN}"
BAREBOX_SIGN_KEYNAME ?= "${PLATFORM_KEYNAME}"

inherit kernel-arch deploy cml1

export KBUILD_BUILD_USER = "oe-user"
export KBUILD_BUILD_HOST = "oe-host"

PROVIDES = "virtual/bootloader netx.rom"

COMPATIBLE_MACHINE = "netx4000"

GIT_BAREBOX_REPO ?= "github.com/hilscher/netx4000-barebox.git;protocol=https"

SRC_URI = " \
    git://${GIT_BAREBOX_REPO};branch=${BRANCH} \
    ${@bb.utils.contains('BAREBOX_SIGN_ENFORCE', '1', 'file://enforce_signature_verification.cfg', '',d)} \
    ${@bb.utils.contains('BAREBOX_ENABLE_NAND',  '1', 'file://enable_nand.cfg', '',d)} \
    ${@bb.utils.contains('DDR_RAM', 'IS43TR16256AL_1GIB', 'file://enable_is43tr16256a_1gib.cfg', '',d)} \
    ${@bb.utils.contains('DDR_RAM', 'AS4C512M16D3L_12_2GIB', 'file://enable_as4c512m16d3l_12_2gib.cfg', '',d)} \
    file://cr7-wfe-loop.tar.gz \
"

S = "${WORKDIR}/git"

PACKAGE_ARCH = "${MACHINE_ARCH}"
PACKAGES = "${PN}"

EXTRA_OEMAKE = "CROSS_COMPILE=${TARGET_PREFIX}"

DEPENDS += "kern-tools-native"

do_unpack[vardeps] += "BAREBOX_SIGN_ENFORCE BAREBOX_SIGN_KEYNAME"

# returns all the elements from the src uri that are .cfg files
def find_cfgs(d):
    sources=src_patches(d, True)
    sources_list=[]
    for s in sources:
        if s.endswith('.cfg'):
            sources_list.append(s)

    return sources_list

patch_builtin_dtb_name() {
	conf=$1
	barebox_devicetree="${@d.getVar('BAREBOX_DEVICETREE') or ""}"
	external_devicetree="${@d.getVar('PREFERRED_PROVIDER_virtual/dtb') or ""}"

	if [ -n "$barebox_devicetree" ] && [ -z "$external_devicetree" ]; then
		devicetree="$(ls ${S}/arch/arm/dts/${barebox_devicetree%.*}.dts || true)"
	fi
	if [ -n "$external_devicetree" ]; then
		devicetree_count="$(ls ${WORKDIR}/recipe-sysroot/boot/devicetree/*.dtb | wc -l)"
		if [ "$devicetree_count" -gt "1" -a -z "$barebox_devicetree" ]; then
			bberror "There are multiple devicetrees provided by virtual/dtb '${PREFERRED_PROVIDER_virtual/dtb}'!"
			bbfatal "Please select a specific one by adding 'BAREBOX_DEVICETREE' to machine configuration."
		elif [ "$devicetree_count" -eq "1" -a -n "$barebox_devicetree" ]; then
			bbwarn "There is only one devicetree provided by virtual/dtb '${PREFERRED_PROVIDER_virtual/dtb}'!"
			bbwarn "Therfore 'BAREBOX_DEVICETREE' is not required and should be removed from configuration."
		fi
		if [ -n "$barebox_devicetree" ]; then
			devicetree="$(ls ${WORKDIR}/recipe-sysroot/boot/devicetree/${barebox_devicetree})"
		else
			devicetree="$(ls ${WORKDIR}/recipe-sysroot/boot/devicetree/*.dtb)"
		fi
	fi
	[ -z "$devicetree" ] && bbfatal "Invalid or missing devicetree, please check 'PREFERRED_PROVIDER_virtual/dtb' and 'BAREBOX_DEVICETREE'!"

	# Remove extension from devicetree filename and patch .config
	devicetree="$(realpath --relative-to ${S}/arch/arm/dts ${devicetree%.*})"
	sed -i -e 's,\(CONFIG_BUILTIN_DTB_NAME=\).*,\1"'$devicetree'",g' $conf

	bbwarn "Use '$devicetree.dtb' as devicetree."
}

do_configure[depends] = "virtual/dtb:do_populate_sysroot"
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

    [ -d ${WORKDIR}/env ] && {
        bbnote "Copying/Replacing env directory"
        rm -rf ${S}/arch/arm/boards/netx4000/env
        cp -r ${WORKDIR}/env ${S}/arch/arm/boards/netx4000
    }

    if [ "${@bb.utils.contains('BAREBOX_SIGN_ENFORCE', '1', 'true', 'false', d)}" = "true" ]; then
        bbnote "Add key-node to env directory"
        echo "/signature/key-${BAREBOX_SIGN_KEYNAME}" > ${S}/arch/arm/boards/netx4000/env/key-node
    fi

    # Patch CONFIG_BUILTIN_DTB_NAME in .config
	patch_builtin_dtb_name ${S}/.config

    # Apply possible configuration fragments
    merge_config.sh -m .config ${@" ".join(find_cfgs(d))}

    # Patch xml-file used by hboot-image-compiler
    for xmlPatch in $(ls ${WORKDIR}/*.xmlpatch); do
        bbwarn "hboot-image: Applying patch file '$(basename ${xmlPatch})'"
        sed -n -i -e '/<\/Chunks>/r '${xmlPatch} -e 1x -e '2,${x;p}' -e '${x;p}' ${S}/scripts/netx4000/ca9_boot.xml && rm ${xmlPatch}
    done
}

do_compile_prepend() {
    unset LDFLAGS
}

BUILD_ID="${DATETIME}"
BUILD_ID[vardepsexclude] = "DATETIME"

do_deploy[cleandirs] = "${DEPLOYDIR}"
do_deploy () {
    install ${B}/barebox ${DEPLOYDIR}/${PN}-${PV}-${MACHINE}-${BUILD_ID}.elf
    ln -sf ${PN}-${PV}-${MACHINE}-${BUILD_ID}.elf ${DEPLOYDIR}/${PN}-${PV}.elf

    if [ -e "${B}/barebox.netx4000" ]; then
        install ${B}/barebox.netx4000 ${DEPLOYDIR}/${PN}-${PV}-${MACHINE}-${BUILD_ID}
        ln -sf ${PN}-${PV}-${MACHINE}-${BUILD_ID} ${DEPLOYDIR}/netx.rom
    fi
}
addtask deploy before do_build after do_compile
