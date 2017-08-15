OVERLAY_DEST="/boot/dt-overlays"

PACKAGES += "kernel-devicetree-overlays"

do_compile_append() {
    oe_runmake dtbs
}

dtbo_install_files() {
    dest=$1

    install -d ${dest}

    for overlay in ${KERNEL_DEVICETREE_OVERLAYS}; do
        if [ $(echo ${overlay} | grep ":") ]; then
            src_file=$(echo ${overlay} | cut -d ":" -f1)
            dst_file=$(echo ${overlay} | cut -d ":" -f2)
        else
            src_file=${overlay}
            dst_file=$(basename ${src_file})
        fi
        install -m 0644 ${B}/arch/${ARCH}/boot/dts/${src_file} ${dest}/${dst_file}
    done
}

do_install_append() {
    dtbo_install_files ${D}${OVERLAY_DEST}
}

do_deploy_append() {
    dtbo_install_files ${DEPLOYDIR}/dt-overlays
}

FILES_kernel-devicetree-overlays = "${OVERLAY_DEST}"
