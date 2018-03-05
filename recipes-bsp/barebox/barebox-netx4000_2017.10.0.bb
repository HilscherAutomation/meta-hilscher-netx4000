require barebox.inc

BRANCH = "v2017.10.0-netx4000"

SRCREV = "887fa9155310e681221ef096950e47f2278954da"
SRCREV_netx4000-rlxd = "7b652a0e371dac27e4454db7c1d6c583dbbc39db"

SRC_URI += "file://${DTS_DIR}"

DEPENDS += "lzop-native python-native"
