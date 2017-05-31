require barebox.inc

SRCREV = "8e0ff887df059d89bc296ba133a6e20439a47690"
#SRCREV = "${AUTOREV}"

SRC_URI += "file://${DTS_DIR}"

DEPENDS += "lzop-native python-native"
