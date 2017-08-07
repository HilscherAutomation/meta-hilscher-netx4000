require barebox.inc

BRANCH = "v2016.07.0-netx4000"

SRCREV = "8c0be8652894a78d998981d0ad0149aa5efbf69e"
#SRCREV = "${AUTOREV}"

SRC_URI += "file://${DTS_DIR}"

DEPENDS += "lzop-native python-native"
