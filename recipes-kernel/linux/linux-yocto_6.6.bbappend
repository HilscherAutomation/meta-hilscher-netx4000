FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

KERNEL_EXTRA_FEATURES:netx4000 ?= " \
    features/netfilter/netfilter.scc \
    features/leds/leds.scc \
    features/firmware/firmware.scc \
    cfg/usb-mass-storage.scc \
    ${@ 'features/nfsd/nfsd-enable.scc' if '${DISTRO}' == 'poky-lsb' else '' } \
"

KMACHINE:netx4000 = "netx4000"
SRC_URI:append:netx4000 = " \
    file://netx4000-standard.scc \
    file://netx4000.scc \
    file://netx4000.cfg \
"

require netx4000-patches.inc

COMPATIBLE_MACHINE:netx4000 = "netx4000"
