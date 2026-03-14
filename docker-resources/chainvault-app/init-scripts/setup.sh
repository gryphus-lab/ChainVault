#!/bin/bash

echo "Installing Tesseract"
apt-get update
apt-get --no-install-recommends install -y \
  build-essential \
  curl \
  pkg-config \
  libtesseract-dev \
  tesseract-ocr \
  tesseract-ocr-deu \
  tesseract-ocr-eng
rm -rf /var/lib/apt/lists/*
echo "Tesseract installed"

echo "Installing Leptonica Dependencies..."
apt-get install -y libpng-dev libjpeg-dev libtiff-dev zlib1g-dev \
  libwebp-dev libopenjp2-7-dev libgif-dev \
  autoconf automake libtool pkg-config
echo "Leptonica dependencies installed"

echo "Build Leptonica 1.86.0 from sources..."
curl --proto "=https" --tlsv1.2 -sSfLO \
    https://github.com/user-attachments/files/22412990/leptonica-1.86.0.tar.gz
tar -xf leptonica-1.86.0.tar.gz
cd leptonica-1.86.0 || exit 1
./autogen.sh
./configure --prefix=/usr/local
make -j"$(nproc)"
make install
ldconfig /usr/local/lib
echo "Build Leptonica completed"

export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
export JNA_LIBRARY_PATH=/usr/local/lib

echo "Verifying Leptonica Version..."
ldd /usr/local/lib/libleptonica.so | grep "not found" || echo "Linker OK"
pkg-config --modversion lept

