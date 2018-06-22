ROOTFS_BOOTSTRAP_INSTALL=""

# Remove x86 specific stuff
PACKAGE_INSTALL_remove += " \
	initramfs-live-install \
	initramfs-live-install-efi \
	initramfs-live-boot \
	initramfs-module-install \
"

# Remove unneeded stuff
PACKAGE_INSTALL_remove += " \
	base-passwd \
	udev \
"

# Add initrd framework
PACKAGE_INSTALL_append += " \
	initramfs-framework-base \
	initramfs-framework-rootfs-image-file \
	${@ 'initramfs-framework-resize-rootfs' if d.getVar('ROOTFS_PART_AUTORESIZE') == '1' else '' } \
"

COMPATIBLE_HOST = "arm-.*-linux"
