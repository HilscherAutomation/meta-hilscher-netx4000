inherit kernel
require recipes-kernel/linux/linux-yocto.inc

LINUX_VERSION_EXTENSION = "-netx4000"

SRC_URI += "${@bb.utils.contains('MACHINE_FEATURES', 'tpm2', 'file://enable_tpm2.cfg', '', d)}"

KMETA = "kernel-meta"
KCONF_BSP_AUDIT_LEVEL = "2"

PR = "r0"
PV = "${LINUX_VERSION}+git${SRCPV}"

KERNEL_EXTRA_FEATURES ?= "features/netfilter/netfilter.scc features/leds/leds.scc"
KERNEL_FEATURES_append = "${KERNEL_EXTRA_FEATURES}"

do_kernel_metadata_prepend() {
    # Using yocto kernel-cache requires a bsp to be present, otherwise
    # a fall back to arm-versatile is used, which does not work for us
    mkdir -p ${WORKDIR}/${KMETA}/bsp/netx4000
    cat <<EOF>${WORKDIR}/${KMETA}/bsp/netx4000/netx4000.scc
define KMACHINE ${KMACHINE}
define KTYPE ${LINUX_KERNEL_TYPE}
define KARCH arm

include ktypes/${LINUX_KERNEL_TYPE}/${LINUX_KERNEL_TYPE}.scc nopatch
EOF
}

COMPATIBLE_MACHINE = "netx4000"
