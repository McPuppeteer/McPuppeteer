#!/bin/sh

find . -name "*.java" -type f -exec grep -L "Copyright (C) 2025 - PsychedelicPalimpsest" {} \;
