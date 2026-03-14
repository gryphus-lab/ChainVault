#!/usr/bin/env bash

set -euo pipefail

readonly LEPT_VERSION="1.86.0"
readonly LEPT_URL="https://github.com/DanBloomberg/leptonica/releases/download/${LEPT_VERSION}/leptonica-${LEPT_VERSION}.tar.gz"
readonly PREFIX="/usr/local"

export LD_LIBRARY_PATH="${PREFIX}/lib${LD_LIBRARY_PATH:+:${LD_LIBRARY_PATH}}"
export JNA_LIBRARY_PATH="${PREFIX}/lib"

log() { echo -e "\n[$(date +'%H:%M:%S')] $*"; }

error_exit() {
    log "ERROR: $1" >&2
    exit 1
}

cleanup() {
    log "Cleaning up temporary files..."
    rm -f "leptonica-${LEPT_VERSION}.tar.gz"
    rm -rf "leptonica-${LEPT_VERSION}"
}
trap cleanup EXIT

main() {
    [[ $EUID -ne 0 ]] && error_exit "This script must be run as root (use sudo)."

    log "Installing Tesseract and system dependencies..."
    apt-get update
    apt-get install -y --no-install-recommends \
        build-essential curl pkg-config libtesseract-dev \
        tesseract-ocr tesseract-ocr-deu tesseract-ocr-eng \
        libpng-dev libjpeg-dev libtiff-dev zlib1g-dev \
        libwebp-dev libopenjp2-7-dev libgif-dev \
        autoconf automake libtool

    rm -rf /var/lib/apt/lists/*

    log "Building Leptonica ${LEPT_VERSION} from source..."
    curl --proto "=https" --tlsv1.2 -sSfLO "$LEPT_URL"
    tar -xf "leptonica-${LEPT_VERSION}.tar.gz"

    (
        cd "leptonica-${LEPT_VERSION}"
        ./autogen.sh
        ./configure --prefix="$PREFIX"
        make -j"$(nproc)"
        make install
    ) || error_exit "Leptonica build failed."

    log "Configuring dynamic linker..."
    ldconfig "${PREFIX}/lib"

    log "Verifying Installation..."
    if ldd "${PREFIX}/lib/libleptonica.so" | grep -q "not found"; then
        error_exit "Library linking issues detected."
    fi

    echo "Leptonica version: $(pkg-config --modversion lept || echo 'N/A')"
    log "Installation complete!"
}

main "$@"
