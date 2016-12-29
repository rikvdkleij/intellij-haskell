#!/bin/bash

# The MIT License (MIT)

# Copyright (c) 2015 Aleksey Kladov, Evgeny Kurbatsky, Alexey Kudinkin and contributors

# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:

# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.

# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

# Copied from intellij-rust, many thanks for their greate work.
# Batch rasterize SVG icon file to PNG.
# Requires rsvg-convert command.
# Please re-run this script after each source file change.

INPUT_FILE='icon_intellij_haskell.svg'
FILE_NAME=$(basename "$INPUT_FILE" .svg)

for SIZE in 16 24 32 48 64 128 256 512; do
    OUTPUT_FILE="${FILE_NAME}_${SIZE}.png"
    rsvg-convert $INPUT_FILE -o $OUTPUT_FILE -w $SIZE -h $SIZE
done

