require barebox.inc

SRCREV = "dce8ddb07cd54780a9b12006753383d9ea65476e"
#SRCREV = "${AUTOREV}"

SRC_URI += "file://${DTS_DIR}"

DEPENDS += "lzop-native python-native"
