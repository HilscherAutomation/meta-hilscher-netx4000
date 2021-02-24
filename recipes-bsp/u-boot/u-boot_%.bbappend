FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
require uboot-update.inc

# Setup DDR timings
SRC_URI_append += "file://enable_${@d.getVar('DDR_RAM').lower()}.cfg"
# Setup ECC if available
SRC_URI_append += "${@ 'file://enable_ddr_ecc.cfg' if d.getVar('ENABLE_DDR_ECC') == 'yes' else ''}"

# Add support for external device-tree
DTB_PATH ??= "/boot/devicetree"
DTB_NAME ??= "${MACHINE}.dtb"
python __anonymous () {
    #check if there are any dtb providers
    providerdtb = d.getVar("PREFERRED_PROVIDER_virtual/dtb")
    if providerdtb:
       d.appendVarFlag('do_configure', 'depends', ' virtual/dtb:do_populate_sysroot')
}

EXTRA_OEMAKE += "${@'EXT_DTB=${RECIPE_SYSROOT}/${DTB_PATH}/${DTB_NAME}' if (d.getVar('DTB_NAME') != '') else '' }"

# Deploy additional artifacts for wic image
do_deploy_append() {
    install -m 644 ${B}/u-boot.netx4000 ${DEPLOYDIR}/
    ln -sf u-boot.netx4000 ${DEPLOYDIR}/netx.rom
}

COMPATIBLE_MACHINE = "netx4000"
