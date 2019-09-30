DEPENDS_append += "openssl-native"

python do_apply_verification_keys() {
  import subprocess
  import sys
  import re

  # http://www.algorithmist.com/index.php/Modular_inverse
  def recursive_egcd(a, b):
      """Returns a triple (g, x, y), such that ax + by = g = gcd(a,b).
         Assumes a, b >= 0, and that at least one of them is > 0.
         Bounds on output values: |x|, |y| <= max(a, b)."""
      if a == 0:
          return (b, 0, 1)
      else:
          g, y, x = recursive_egcd(b % a, a)
          return (g, x - (b // a) * y, y)

  def modinv(a, m):
      g, x, y = recursive_egcd(a, m)
      if g != 1:
          return None
      else:
          return x % m

  key_file=os.path.join(d.getVar("DTS_SIGN_KEY_DIR"), d.getVar("DTS_SIGN_KEY_NAME") + ".key")

  # Extract modulus and N0inv
  modulus = subprocess.check_output(["openssl", "rsa", "-in", key_file, "-modulus", "-noout"])
  modulus = re.sub(b'Modulus=', b'', modulus).rstrip()
  N       = int(modulus, 16)
  keylen  = N.bit_length()

  B = 0x100000000
  N0inv = B - modinv(N, B)

  # Extract public exponent (defaults to 0x100001)
  exponent = subprocess.check_output(["openssl", "rsa", "-in", key_file, "-text", "-noout"])
  exponent = int(re.search(b'publicExponent: ([0-9]+)', exponent).group(1))

  str_modulus=modulus.decode("utf-8").lower()
  dtc_modulus=["0x" + str_modulus[i:i+8] for i in range(0, len(str_modulus), 8)]
  dtc_modulus=" ".join(dtc_modulus)

  R = pow(2, keylen)
  RR = pow(R, 2, int(modulus, 16))
  str_RR=hex(RR)[2:]
  rr=["0x" + str_RR[i:i+8] for i in range(0, len(str_RR), 8)]
  rr=" ".join(rr)

  signature_block="""
/ {
    signature {
        key-%s {
            required = "conf";
            rsa,modulus = <%s>;
            rsa,exponent = <0x00000000 0x%08x>;
            rsa,n0-inverse = <0x%08x>;
            rsa,num-bits = <0x%08x>;
            rsa,r-squared = <%s>;
        };
    };
};
""" % ( d.getVar("DTS_SIGN_KEY_NAME"),
        dtc_modulus, int(exponent),
        N0inv, keylen, rr)

  # Add include to main dts file
  dts_file = d.getVar("DTS_TO_SIGN")
  dts_path = os.path.dirname(dts_file)
  with open(os.path.join(dts_path, "signature_check.dtsi"), 'w+') as sig_check:
    sig_check.write(signature_block)

  with open(dts_file, 'a') as dts:
    dts.write('\n#include "signature_check.dtsi"\n')
}

python() {
  sign_image = d.getVar("DTS_SIGN_ENFORCE") or "0"
  if sign_image != "0":
    bb.build.addtask('do_apply_verification_keys', 'do_compile', 'do_configure', d)
}
