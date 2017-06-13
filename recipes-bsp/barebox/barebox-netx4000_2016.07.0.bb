require barebox.inc

BRANCH = "v2016.07.0-netx4000"

SRCREV = "6298b3aae4ddfde53c643846fbcc5c304129db82"
#SRCREV = "${AUTOREV}"

SRC_URI += "file://${DTS_DIR}"

DEPENDS += "lzop-native python-native"
