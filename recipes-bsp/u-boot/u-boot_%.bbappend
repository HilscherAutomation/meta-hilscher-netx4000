FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

require netx4000-patches.inc

# Setup DDR timings
SRC_URI:append:netx4000 = " file://enable_${@d.getVar('DDR_RAM').lower()}.cfg "
# Setup ECC if available
SRC_URI:append:netx4000 = "${@ ' file://enable_ddr_ecc.cfg ' if d.getVar('ENABLE_DDR_ECC') == 'yes' else ''}"

# Add support for external device-tree
DTB_PATH:netx4000 ??= "/boot/devicetree"
DTB_NAME:netx4000 ??= "${MACHINE}.dtb"
EXTRA_OEMAKE:append:netx4000 = "${@'EXT_DTB=${RECIPE_SYSROOT}/${DTB_PATH}/${DTB_NAME}' if (d.getVar('PREFERRED_PROVIDER_virtual/dtb') != '') else '' }"

python __anonymous () {
    #check if there are any dtb providers
    providerdtb = d.getVar("PREFERRED_PROVIDER_virtual/dtb")
    if providerdtb:
       d.appendVarFlag('do_configure', 'depends', ' virtual/dtb:do_populate_sysroot')
}

# Deploy additional artifacts for wic image
do_deploy:append:netx4000() {
    install -m 644 ${B}/u-boot.netx4000 ${DEPLOYDIR}/
    ln -sf u-boot.netx4000 ${DEPLOYDIR}/netx.rom
}

COMPATIBLE_MACHINE:netx4000 = "netx4000"
