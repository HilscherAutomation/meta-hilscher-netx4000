require barebox.inc

BRANCH = "v2017.10.0-netx4000"

SRCREV = "601f01952d5c05f6274adda37ddfa0d68e997444"
SRCREV_netx4000-rlxd = "c71f16de4a40d996df40a50ebba415751824ca46"

SRC_URI += "file://${DTS_DIR}"

DEPENDS += "lzop-native python-native"
