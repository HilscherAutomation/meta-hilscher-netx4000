require barebox.inc

BRANCH = "v2016.07.0-netx4000"

SRCREV = "60d0d6c9cdceefd4f1da880334b0b6d3b0b79200"
#SRCREV = "${AUTOREV}"

SRC_URI += "file://${DTS_DIR}"

DEPENDS += "lzop-native python-native"
