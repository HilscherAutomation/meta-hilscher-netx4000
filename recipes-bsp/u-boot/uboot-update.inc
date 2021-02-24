# Use v2021.01 from netx4000 repository
SRC_URI_remove += "git://git.denx.de/u-boot.git \
                   file://remove-redundant-yyloc-global.patch"

GIT_UBOOT_REPO ?= "github.com/HilscherAutomation/netx4000-uboot.git;protocol=https"

SRC_URI_append += "git://${GIT_UBOOT_REPO};branch=v2021.01-netx4000"

PV="2021.01"
SRCREV="fadb9e035a3e7a6aebbeadf8be3ad815e010954a"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=5a7450c57ffe5ae63fd732446b988025"
