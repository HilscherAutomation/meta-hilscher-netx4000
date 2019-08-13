KBRANCH = "v4.9-netx4000-rt"
LINUX_KERNEL_TYPE = "preempt-rt"

# netX4000
LINUX_VERSION = "4.9.146-rt125"
SRCREV_machine="40c52c1bc4d698b4d0fe8bfbb95da1924c2d6665"

SRCREV_meta="9aed9998adc8848b33ade296422f7df7642bbc04"

require linux-hilscher-common.inc

SRC_URI += "${@bb.utils.contains('MACHINE_FEATURES', '3g', 'file://enable_3g.cfg file://net-next-qmi_wwan-apply-SET_DTR-quirk-to-the-SIMCOM-shared-device-ID.patch', '', d)}"
