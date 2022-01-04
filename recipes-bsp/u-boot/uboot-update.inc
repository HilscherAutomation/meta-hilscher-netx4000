# Use v2021.01 from netx4000 repository
SRC_URI_remove += "git://git.denx.de/u-boot.git;branch=master \
                   file://remove-redundant-yyloc-global.patch \
                   file://CVE-2020-8432.patch \
                   file://CVE-2020-10648-1.patch \
                   file://CVE-2020-10648-2.patch \
"

GIT_UBOOT_REPO ?= "github.com/HilscherAutomation/netx4000-uboot.git;protocol=https"

SRC_URI_append += "git://${GIT_UBOOT_REPO};branch=v2021.01-netx4000"

PV="2021.01"
SRCREV="4485ff255d97ed1e97e8c1ae6fa57cb43c9bd704"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=5a7450c57ffe5ae63fd732446b988025"
