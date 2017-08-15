require barebox.inc

BRANCH = "v2016.07.0-netx4000"

SRCREV = "e3bb09aad0737ccba6f16f91659aa8657d1088b4"

SRC_URI += "file://${DTS_DIR}"

DEPENDS += "lzop-native python-native"
