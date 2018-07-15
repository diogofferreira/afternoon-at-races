#!/bin/bash

FILE="/Users/pbm/Documents/UA/4_year/sd/afternoon-at-races/afternoon-at-races-distributed/states/${1}-status.info"
i=0;

while : ; do
    java -cp out-${1} ${2} ${i}
    i=$((i+1))
    [[ -f $FILE && $i < 4 ]] || break
done
