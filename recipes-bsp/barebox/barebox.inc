SUMMARY = "Barebox (formerly known as u-boot-v2) is a bootloader that inherits the best of U-Boot and the Linux kernel"
HOMEPAGE = "http://barebox.org/"
SECTION = "bootloaders"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM ?= "file://COPYING;md5=057bf9e50e1ca857d0eb97bfe4ba8e5d"

FITIMAGE_SIGN = "${PLATFORM_SIGN}"
BAREBOX_SIGN_ENFORCE ?= "${PLATFORM_SIGN}"
BAREBOX_SIGN_KEYDIR ?= "${PLATFORM_KEYDIR}"
BAREBOX_SIGN_KEYNAME ?= "${PLATFORM_KEYNAME}"

inherit kernel-arch deploy cml1 barebox-sign

export KBUILD_BUILD_USER = "oe-user"
export KBUILD_BUILD_HOST = "oe-host"

PROVIDES = "virtual/bootloader barebox netx.rom u-boot"

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

inherit hilscher-helpers
python () {
    dtsDirsHash = dir_dep_hash(d, d.getVar('DTS_DIRS'))
    bb.debug(1, "DTS_DIRS (overall hash): %s" % dtsDirsHash)

    d.setVar('DTS_DIRS_HASH', dtsDirsHash)
    d.appendVarFlag('do_unpack', 'vardeps', ' DTS_DIRS_HASH')
}

do_unpack[vardeps] += "FITIMAGE_SIGN BAREBOX_SIGN_ENFORCE BAREBOX_SIGN_KEYDIR BAREBOX_SIGN_KEYNAME"

# returns all the elements from the src uri that are .cfg files
def find_cfgs(d):
    sources=src_patches(d, True)
    sources_list=[]
    for s in sources:
        if s.endswith('.cfg'):
            sources_list.append(s)

    return sources_list

do_copy_dts() {
    [ -n "${DTS_DIRS}" ] && {
        bbnote "Copying/Replacing device tree files"
        mkdir -p ${S}/arch/arm/dts/netx4000
        for dtsDir in ${DTS_DIRS}; do
            bbnote "src:$dtsDir"
            cp -r $dtsDir/* ${S}/arch/arm/dts/netx4000/
        done
    }
}
addtask do_copy_dts after do_patch before do_configure

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

    if [ "${@bb.utils.contains('FITIMAGE_SIGN', '1', 'true', 'false', d)}" = "true" ]; then
        bbnote "Add key-node to env directory"
        echo "/signature/key-${BAREBOX_SIGN_KEYNAME}" > ${S}/arch/arm/boards/netx4000/env/key-node
    fi

    # Change used device tree in config
    sed -i -e 's,\(CONFIG_BUILTIN_DTB_NAME=\).*,\1"${@os.path.splitext("${BAREBOX_DEVICETREE}")[0]}",g' ${S}/.config

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
