require barebox.inc

BRANCH = "v2017.10.0-netx4000"

SRCREV = "e7648dc494d04f2bb7e02ab1bf5c7be67ae88a8b"
SRCREV_netx4000-rlxd = "7b652a0e371dac27e4454db7c1d6c583dbbc39db"

SRC_URI += "file://${DTS_DIR}"

DEPENDS += "lzop-native python-native"
