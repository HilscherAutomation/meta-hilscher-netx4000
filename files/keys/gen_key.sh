#!/bin/sh -e

keyname="${1}"
if [ -z "${keyname}" ]; then
  keyname="debug-sample"
fi

keylen="$2"
if [ -z "${keylen}" ]; then
  keylen="4096"
fi

openssl genrsa -F4 -out "${keyname}".key ${keylen}

echo "Generated key '${keyname}' with ${keylen} bits"
