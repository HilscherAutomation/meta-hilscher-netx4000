require barebox.inc

SRCREV = "268c353cf53e18fc7f7f1eac3ef7b92bce29117c"
#SRCREV = "${AUTOREV}"

SRC_URI += "file://${DTS_DIR}"

DEPENDS += "lzop-native python-native"
