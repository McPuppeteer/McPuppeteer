#!/bin/sh
# Find files I forgot the copyright header on
find . -name "*.java" -type f -exec grep -L "Copyright (C) 2025 - PsychedelicPalimpsest" {} \;
