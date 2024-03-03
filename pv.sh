#!/bin/bash

for filename in ./log/*.xml; do
    [ -e "$filename" ] || continue
    PV=$(ruby ./src/main/ruby/xpath-to-leaf.rb $filename)
    echo "$filename: ${PV}"
done

