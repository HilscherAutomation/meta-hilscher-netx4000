require barebox.inc

BRANCH = "v2017.10.0-netx4000"

SRCREV = "f1f2f1552c41be42d67099a7de0e78849c37d1a4"

SRC_URI += "file://${DTS_DIR}"

DEPENDS += "lzop-native python-native"
