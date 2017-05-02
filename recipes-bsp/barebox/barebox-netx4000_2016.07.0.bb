require barebox.inc

SRCREV = "424ae7559871b1a38af4288b3f9a489ebda07291"
#SRCREV = "${AUTOREV}"

SRC_URI += "file://${DTS_DIR}"

DEPENDS += "lzop-native python-native"
