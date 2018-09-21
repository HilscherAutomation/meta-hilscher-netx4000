# Disable PARALLEL_MAKE to avoid icecc related compile time errors.
# This is required because the function icecc_is_allarch in icecc.bbclass
# has been changed in poky (commit d9ba0219b2f6643ffc825d4b8d3494d07237dd0b).
PARALLEL_MAKE = ""
