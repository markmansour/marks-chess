#!/bin/bash

MYSELF="$(readlink -f "$0")"
MYDIR="${MYSELF%/*}"

for filename in ./log/*.xml; do
    [ -e "$filename" ] || continue
    PV=$(ruby $MYDIR/xpath-to-leaf.rb $filename)
    echo "$filename: ${PV}"
done

