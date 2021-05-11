FILESEXTRAPATHS_prepend:="${THISDIR}/files:"

KERNEL_EXTRA_FEATURES_netx4000 ?= "features/netfilter/netfilter.scc features/leds/leds.scc"

# Prevent automatically inclusion of kernel-image into rootfs/image
RDEPENDS_${KERNEL_PACKAGE_NAME}-base_netx4000 = ""

KMACHINE_netx4000 = "netx4000"
SRC_URI_append_netx4000 += " \
    file://netx4000-standard.scc \
    file://netx4000.scc \
    file://netx4000.cfg \
"

require netx4000-patches.inc

COMPATIBLE_MACHINE_netx4000 = "netx4000"
