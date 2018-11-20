#!/bin/bash

# Check pre-requisites (qemu mapping for executables
if [ ! -d /proc/sys/fs/binfmt_misc ]; then
    if ! /sbin/modprobe binfmt_misc ; then
	echo "Error loading binfmt_support"
	exit 1
    fi
fi

if [ ! -f /proc/sys/fs/binfmt_misc/register ]; then
    if ! mount binfmt_misc -t binfmt_misc /proc/sys/fs/binfmt_misc ; then
	echo "Error mounting binfmt_misc"
	exit 1
    fi
fi

if [ ! -e /proc/sys/fs/binfmt_misc/qemu-arm ]; then
	if [ ! -w /proc/sys/fs/binfmt_misc/register ] ; then
		echo "Error registering qemu emulation"
		exit 1
	fi
	arm_magic='\x7fELF\x01\x01\x01\x00\x00\x00\x00\x00\x00\x00\x00\x00\x02\x00\x28\x00'
	arm_mask='\xff\xff\xff\xff\xff\xff\xff\x00\xff\xff\xff\xff\xff\xff\xff\xff\xfe\xff\xff\xff'
	arm_family=arm
	qemu=/usr/bin/qemu-arm-static
	echo ":qemu-$arm_family:M::$arm_magic:$arm_mask:$qemu:" > /proc/sys/fs/binfmt_misc/register
	binfmt_unregister=1
else
	# We need to check if our qemu-arm-static location matches host
	qemu_location=$(grep interpreter /proc/sys/fs/binfmt_misc/qemu-arm | cut -d " " -f2)
	if [ "$qemu_location" != "/usr/bin/qemu-arm-static" ]; then
		qemu_dir=$(dirname $qemu_location)
		mkdir -p $qemu_dir
		ln -sf /usr/bin/qemu-arm-static $qemu_location
	fi
fi

echo "Building root filesystem"

export DEBIAN_FRONTEND=noninteractive
export LANG=en_US.UTF-8
export TERM="xterm-color"

DEFAULT_PACKAGES="ubuntu-standard,openssh-server,apt-utils"
PACKAGE_LIST=${1:-"${DEFAULT_PACKAGES}"}

debootstrap --foreign --include=${PACKAGE_LIST} --arch=armhf bionic rootfs http://ports.ubuntu.com
# Copy qemu binary required for debootstrap --second-stage
cp /usr/bin/qemu-arm-static rootfs/usr/bin
chroot rootfs /debootstrap/debootstrap --second-stage

# Insert ubuntu repositories into apt.conf
cat <<EOF > rootfs/etc/apt/sources.list
deb http://ports.ubuntu.com bionic main restricted universe multiverse
deb http://ports.ubuntu.com bionic-updates main restricted universe multiverse
deb http://ports.ubuntu.com bionic-security main restricted universe multiverse
EOF

# Setup default hostname
echo "ubuntu-netx4000" > rootfs/etc/hostname
# Enable root login (password=root)
chroot rootfs sh -c 'echo "root:root" | chpasswd'
# Enable ssh root login by password
sed -i 's/^PermitRootLogin.*/PermitRootLogin yes/g' rootfs/etc/ssh/sshd_config
# Enable serial console (UART0) login
ln -s /lib/systemd/system/serial-getty@.service /etc/systemd/system/getty.target.wants/serial-getty@ttyAMA0.service

# Setup default networking
# configuration (eth0=dhcp, eth1=static 192.168.254.1)
cat <<EOF > rootfs/etc/network/interfaces
auto lo

allow-hotplug eth0
iface eth0 inet dhcp

allow-hotplug eth1
iface eth1 inet static
        address 192.168.254.1
        netmask 255.255.255.0
EOF

# Update all installed packages
chroot rootfs apt-get update
chroot rootfs apt-get -f -y install
chroot rootfs apt-get dist-upgrade -y
chroot rootfs apt-get clean

# Install kernel and cifX packages
cp /usr/share/kernel-package/examples/etc/kernel/postinst.d/symlink_hook rootfs/etc/kernel/postinst.d
cp kernel/*.deb rootfs
cp ../cifx/*.deb rootfs
packages=$(find rootfs -maxdepth 1 -name '*.deb' -exec basename {} \; | tr "\n" " ")
chroot rootfs dpkg -i $packages
rm rootfs/*.deb

# Remove qemu binary
rm rootfs/usr/bin/qemu-arm-static

tar cjf netx4000-ubuntu-18.04-rootfs.tar.bz2 -C rootfs .
rm -rf rootfs

if [ ! -z "${binfmt_unregister}" ]; then
	echo -1 > /proc/sys/fs/binfmt_misc/qemu-$arm_family
fi

# Create manifest file
rm -f netx4000-ubuntu-18.04-rootfs.manifest
for package in $(echo $PACKAGE_LIST | sed 's/,/ /g'); do
	echo $package >> netx4000-ubuntu-18.04-rootfs.manifest
done
