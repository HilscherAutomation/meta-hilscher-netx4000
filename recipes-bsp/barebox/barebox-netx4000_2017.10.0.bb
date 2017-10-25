require barebox.inc

BRANCH = "v2017.10.0-netx4000"

SRCREV = "c71f16de4a40d996df40a50ebba415751824ca46"

SRC_URI += "file://${DTS_DIR}"

DEPENDS += "lzop-native python-native"
