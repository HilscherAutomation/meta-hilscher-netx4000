do_kernel_metadata_prepend() {
  # If WORKDIR already contains a defconfig and KBUILD_DEFCONFIG is used
  # this defconfig is not added to sccs thus being ignored
  # This might happen when adding patches / fragments without cleaning
  # the recipe before, producing a completely broken kernel configuration
  if [ -n "${KBUILD_DEFCONFIG}" ]; then
    if [ -f "${WORKDIR}/defconfig" ]; then
      rm -f ${WORKDIR}/defconfig
    fi
  fi

  # Using yocto kernel-cache requires a bsp to be present, otherwise
  # a fall back to arm-versatile is used, which does not work for us
  mkdir -p ${WORKDIR}/${KMETA}/bsp/netx4000
  cat <<EOF>${WORKDIR}/${KMETA}/bsp/netx4000/netx4000.scc
define KMACHINE ${KMACHINE}
define KTYPE ${LINUX_KERNEL_TYPE}
define KARCH arm

include ktypes/${LINUX_KERNEL_TYPE}/${LINUX_KERNEL_TYPE}.scc
EOF
}
