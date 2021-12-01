#!/usr/bin/env bash
set -euo pipefail

prev=
cnt=0
while read -r line; do
    if [ -n $prev ] && (( $line > prev )); then
        cnt=$(($cnt+1))    
    fi 
    prev=$line
done < "$1"
echo "Star 1: $cnt"

num_windows=$(($(wc -l < $1) - 3))
prev=
cnt=0
for ((i=0; i<=num_windows; ++i)); do
    start=$((1+$i))
    end=$((start+2))
    window=$(awk -v start="$start" -v end="$end" 'FNR>=start && FNR<=end {s+=$1} END {print s}' $1)
    if [ -n $prev ] && (( window > prev )); then
        cnt=$(($cnt+1))            
    fi
    prev=$window
done
echo "Star 2: $cnt"
