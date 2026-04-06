#!/usr/bin/env bash
set -euo pipefail

export DEBIAN_FRONTEND=noninteractive
readonly LEPT_VERSION="1.86.0"
readonly LEPT_URL="https://github.com/DanBloomberg/leptonica/releases/download/${LEPT_VERSION}/leptonica-${LEPT_VERSION}.tar.gz"
readonly PREFIX="/usr/local"

main() {
    apt-get update && apt-get install -y --no-install-recommends \
        build-essential curl pkg-config ca-certificates \
        libtesseract-dev tesseract-ocr tesseract-ocr-deu tesseract-ocr-eng \
        libpng-dev libjpeg-dev libtiff-dev zlib1g-dev \
        libwebp-dev libopenjp2-7-dev libgif-dev \
        autoconf automake libtool netcat-openbsd

    mkdir -p /tmp/leptonica-build
    cd /tmp/leptonica-build

    curl --proto "=https" --tlsv1.2 -sSfLO \
        "$LEPT_URL"
    tar -xf "leptonica-${LEPT_VERSION}.tar.gz"

    cd "leptonica-${LEPT_VERSION}"
    ./autogen.sh && ./configure --prefix="$PREFIX" && make -j"$(nproc)" && make install

    ldconfig "$PREFIX/lib"

    cd /
    rm -rf /tmp/leptonica-build
    apt-get purge -y build-essential autoconf automake libtool
    apt-get autoremove -y
    apt-get clean
    rm -rf /var/lib/apt/lists/*
}

main "$@"
