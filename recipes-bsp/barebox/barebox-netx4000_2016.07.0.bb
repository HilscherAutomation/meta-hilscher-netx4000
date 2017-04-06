require barebox.inc

SRCREV = "4dfc2503719ade69d15541200510eeecb0beb4db"
#SRCREV = "${AUTOREV}"

SRC_URI += "file://${DTS_DIR}"

DEPENDS += "lzop-native python-native"
