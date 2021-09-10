# We have a binary patch which is only supported by git (not by quilt which is default)
PATCHTOOL:netx4000 = "git"
SRC_URI:append:netx4000 += " \
    file://0001-netx4000-Basic-architecture-support.patch \
    file://0002-netx4000-Add-MMC-driver.patch \
    file://0003-netx4000-Add-USB-ehci-controller-driver.patch \
    file://0004-netx4000-Add-QSPI-driver-only-working-in-1Bit-mode.patch \
    file://0005-netx4000-Add-GPIO-driver.patch \
"
