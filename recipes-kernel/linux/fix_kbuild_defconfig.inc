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
}
