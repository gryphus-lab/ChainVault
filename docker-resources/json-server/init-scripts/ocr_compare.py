#!/usr/bin/env python3
"""
Compare Tesseract CLI vs tesserocr (using PyTessBaseAPI for compatibility)
"""

import argparse
import subprocess
import tempfile
from pathlib import Path

import tesserocr


def run_tesseract_cli(
    image_path: Path, lang: str = "eng", psm: int = 3, oem: int = 3
) -> str:
    with tempfile.NamedTemporaryFile(suffix=".txt", delete=False) as tmp:
        output_base = tmp.name[:-4]

    cmd = [
        "tesseract",
        str(image_path),
        output_base,
        "-l",
        lang,
        "--psm",
        str(psm),
        "--oem",
        str(oem),
        "txt",
    ]
    subprocess.run(cmd, check=True, capture_output=True)

    txt_path = Path(output_base + ".txt")
    text = txt_path.read_text(encoding="utf-8").strip()
    txt_path.unlink(missing_ok=True)
    return text


def run_tesserocr(
    image_path: Path, lang: str = "eng", psm: int = 3, oem: int = 3
) -> str:
    """Use PyTessBaseAPI (your current version has this)"""
    api = tesserocr.PyTessBaseAPI(lang=lang, psm=psm, oem=oem)
    try:
        api.SetImageFile(str(image_path))
        text = api.GetUTF8Text().strip()
    finally:
        api.End()
    return text


def main():
    parser = argparse.ArgumentParser(
        description="Tesseract CLI vs tesserocr comparison"
    )
    parser.add_argument("image", type=Path, help="Path to TIFF/PNG image")
    parser.add_argument("--lang", default="eng")
    parser.add_argument("--psm", type=int, default=3)
    parser.add_argument("--oem", type=int, default=3)
    args = parser.parse_args()

    print(f"\nTesting image: {args.image}")
    print(f"Settings: lang={args.lang}, psm={args.psm}, oem={args.oem}")
    print(f"tesserocr version: {tesserocr.__version__}\n")

    print("=== Tesseract CLI ===")
    cli_text = run_tesseract_cli(args.image, args.lang, args.psm, args.oem)
    print(cli_text)
    print("-" * 80)

    print("=== tesserocr (PyTessBaseAPI) ===")
    py_text = run_tesserocr(args.image, args.lang, args.psm, args.oem)
    print(py_text)
    print("-" * 80)

    print(
        f"Lines:     CLI = {len(cli_text.splitlines())}, Python = {len(py_text.splitlines())}"
    )
    print(f"Chars:     CLI = {len(cli_text)}, Python = {len(py_text)}")
    print(f"Char diff: {abs(len(cli_text) - len(py_text))}")


if __name__ == "__main__":
    main()
