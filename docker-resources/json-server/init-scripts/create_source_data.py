import argparse
import glob
import json
import os
import secrets
import tempfile
import zipfile
from concurrent.futures import ProcessPoolExecutor
from datetime import datetime, timedelta

import numpy as np
import tifffile as tf
from PIL import Image, ImageDraw, ImageFont, ImageEnhance

LINE_SEPARATOR = (
    "────────────────────────────────────────────────────────────────────────────────"
)

DEST_DIR = "./static/payloads/"
TOTAL_BUNDLES = 10
PAGES_PER_INVOICE = (1, 5)  # min–max pages per invoice

COMPANIES = [
    "Acme Solutions AG",
    "TechNova GmbH",
    "SwissData Systems AG",
    "InnoTech Consulting GmbH",
    "Prime Solutions Schweiz AG",
    "BlueSky IT Services GmbH",
    "Helvetic Software AG",
    "Zurich Digital Solutions AG",
    "Alpine Logic GmbH",
]

CLIENTS = [
    "Global Pharma AG",
    "Swiss Finance Bank AG",
    "Alpine Logistics GmbH",
    "MedTech Innovations SA",
    "Bern Energy Holding AG",
    "Lucerne Retail Group AG",
    "Geneva Luxury Watches SA",
    "Basel Pharma Distribution AG",
    "Zug Crypto Ventures AG",
]

ITEM_DESCRIPTIONS = [
    ("Beratungsleistungen IT-Strategie Q1 2026", 180.00),
    ("Software-Lizenz (perpetual)", 9800.00),
    ("Cloud Hosting & Support 12 Monate", 3600.00),
    ("Entwicklung Custom Dashboard Modul", 145.00),
    ("Wartung & Updates Jahresvertrag 2026", 4200.00),
    ("Reisekosten & Spesen (Zürich–Genf–Zürich)", 680.00),
    ("Schulung Mitarbeitende (10 Personen × 2 Tage)", 4800.00),
    ("API Integration & Testing Phase 2", 165.00),
    ("Sicherheitsaudit & Penetrationstest", 5200.00),
    ("Datenmigration & Validierung", 95.00),
]

FONT_PATHS = [
    "/System/Library/Fonts/Supplemental/Arial.ttf",
    "/System/Library/Fonts/Supplemental/Arial Bold.ttf",
    "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
    "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
    "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
]

INVOICE_QUALITY_TYPES = [
    "valid",
    "noisy",
    "low_contrast",
    "rotated_heavy",
    "text_overlapping",
    "garbage_chars",
]


def load_font(size, bold=False):
    size = max(8, int(size))  # never go below 8pt
    for path in FONT_PATHS:
        try:
            if bold and ("Bold" in path or "bold" in path.lower()):
                return ImageFont.truetype(path, size)
            if not bold:
                return ImageFont.truetype(path, size)
        except IOError:
            continue
    return ImageFont.load_default()


def add_realistic_noise(image: Image.Image, quality: str = "valid") -> Image.Image:
    img_array = np.array(image.convert("L"))

    if quality == "noisy":
        gauss = np.random.normal(0, 35, img_array.shape).astype(np.int16)
        noisy = img_array.astype(np.int16) + gauss
        noisy = np.clip(noisy, 0, 255).astype(np.uint8)
        salt_pepper = np.random.rand(*img_array.shape) < 0.015
        noisy[salt_pepper] = 255 if np.random.rand() > 0.5 else 0
    else:
        gauss = np.random.normal(0, 8, img_array.shape).astype(np.int16)
        noisy = img_array.astype(np.int16) + gauss
        noisy = np.clip(noisy, 0, 255).astype(np.uint8)

    noisy_img = Image.fromarray(noisy)

    mask = Image.new("L", noisy_img.size, 255)
    draw = ImageDraw.Draw(mask)
    w, h = mask.size
    draw.ellipse((-w // 5, -h // 5, w * 6 // 5, h * 6 // 5), fill=210)
    vignette = ImageEnhance.Brightness(noisy_img).enhance(0.95)
    vignette.putalpha(mask)

    return vignette.convert("RGB")


def apply_random_transforms(image: Image.Image, quality: str = "valid") -> Image.Image:
    angle_min, angle_max = (-1.5, 1.5) if quality != "rotated_heavy" else (-8.0, 8.0)
    angle = (
        secrets.randbelow(int((angle_max - angle_min) * 1000 + 1)) / 1000 + angle_min
    )
    rotated = image.rotate(angle, resample=Image.BICUBIC, expand=True)

    scale = (
        0.92 + secrets.randbelow(1001) / 10000
        if quality == "rotated_heavy"
        else 0.98 + secrets.randbelow(401) / 10000
    )
    scaled = rotated.resize(
        (int(rotated.width * scale), int(rotated.height * scale)), Image.LANCZOS
    )

    if secrets.randbelow(100) < 35:
        coeffs = [
            1.0,
            0.0,
            0.0,
            0.0,
            1.0,
            0.0,
            secrets.randbelow(7) / 10000 + 0.0002,
            secrets.randbelow(5) / 10000 + 0.0001,
        ]
        scaled = scaled.transform(scaled.size, Image.PERSPECTIVE, coeffs, Image.BICUBIC)

    return scaled


def create_invoice_pages(quality: str = "valid"):
    num_pages = (
        secrets.randbelow(PAGES_PER_INVOICE[1] - PAGES_PER_INVOICE[0] + 1)
        + PAGES_PER_INVOICE[0]
    )
    pages = []

    invoice_date = datetime.now() - timedelta(days=secrets.randbelow(60))
    due_date = invoice_date + timedelta(days=30)
    invoice_nr = (
        f"INV-{invoice_date.strftime('%Y%m')}-{secrets.randbelow(90000) + 10000}"
    )
    sender = secrets.choice(COMPANIES)
    client = secrets.choice(CLIENTS)

    header_lines = [
        sender.upper(),
        "Musterstrasse 12, 8001 Zürich",
        f"MwSt-Nr: CHE-{secrets.randbelow(900)+100}.{secrets.randbelow(900)+100}.{secrets.randbelow(900)+100} MWST",
        "",
        f"Rechnungs-Nr: {invoice_nr}",
        f"Rechnungsdatum: {invoice_date.strftime('%d.%m.%Y')}",
        f"Fälligkeitsdatum: {due_date.strftime('%d.%m.%Y')}",
        "",
        f"Kunde: {client}",
        "Musterweg 45, 3000 Bern",
    ]

    total_items = secrets.randbelow(11) + 5
    items = [secrets.choice(ITEM_DESCRIPTIONS) for _ in range(total_items)]
    subtotal = 0
    line_items = []

    for desc, unit_price in items:
        qty = secrets.choice([1, 1, 2, 4, 8, 10, 20, 40, 80, 120])
        amount = qty * unit_price
        subtotal += amount
        line_items.append((desc, qty, unit_price, amount))

    vat = subtotal * 0.081
    total = subtotal + vat

    items_per_page = max(4, total_items // num_pages + 1)
    page_groups = [
        line_items[i : i + items_per_page]
        for i in range(0, len(line_items), items_per_page)
    ]

    for page_idx, page_items in enumerate(page_groups, 1):
        page_text = header_lines.copy()

        page_text += [
            "",
            "Pos.  Beschreibung                                      Anz.   Einzelpreis     Betrag",
            LINE_SEPARATOR,
        ]

        start_pos = 1 + (page_idx - 1) * items_per_page
        for pos_offset, (desc, qty, unit_price, amount) in enumerate(
            page_items, start=start_pos
        ):
            page_text.append(
                f"{pos_offset:2d}    {desc:<50} {qty:4d}   {unit_price:10.2f}   {amount:12.2f}"
            )

        if page_idx < len(page_groups):
            page_text += ["", "(Fortsetzung auf nächster Seite)"]
        else:
            page_text += [
                LINE_SEPARATOR,
                f"Zwischensumme{' ':>58}{subtotal:12.2f} CHF",
                f"MwSt 8.1%{' ':>64}{vat:12.2f} CHF",
                LINE_SEPARATOR,
                f"Gesamtbetrag{' ':>60}{total:12.2f} CHF",
                "",
                "Zahlungsbedingungen: 30 Tage netto",
                "IBAN: CH93 0483 5150 1234 5678 9",
                "BIC/SWIFT: CRESCHZZ80A",
                "",
                "Vielen Dank für Ihr Vertrauen!",
            ]

        if quality == "garbage_chars" and secrets.randbelow(3) == 0:
            garbage = "".join(
                secrets.choice("!@#$%^&*()_+{}[]|;:,.<>?/") for _ in range(30)
            )
            page_text.insert(5, f"   {garbage}")

        if quality == "text_overlapping":
            page_text.insert(8, LINE_SEPARATOR)

        pages.append(page_text)

    return pages, invoice_nr, sender, client


def render_page(
    text_lines, page_num, total_pages, quality: str = "valid", scale: float = 1.0
):
    # Scale all dimensions
    base_width = int(2480 * scale)
    base_height = int(3508 * scale)
    margin = int(200 * scale)
    header_y = int(180 * scale)
    line_spacing = int(45 * scale)
    table_y_offset = int(80 * scale)

    img = Image.new("RGB", (base_width, base_height), color=(250, 250, 245))
    draw = ImageDraw.Draw(img)

    font_header_size = int(72 * scale)
    font_subheader_size = int(48 * scale)
    font_body_size = int(32 * scale)
    font_small_size = int(26 * scale)

    font_header = load_font(font_header_size, bold=True)
    font_subheader = load_font(font_subheader_size, bold=True)
    font_body = load_font(font_body_size)
    font_small = load_font(font_small_size)

    y = header_y

    draw.text((margin, y), "INVOICE", font=font_header, fill=(0, 51, 102))
    y += int(140 * scale)

    for line in text_lines:
        if "INVOICE" in line or "Rechnungs-Nr" in line:
            font = font_subheader
            fill = (0, 51, 102)
        elif "Gesamtbetrag" in line:
            font = font_subheader
            fill = (0, 0, 0)
        elif "Fortsetzung" in line:
            font = font_small
            fill = (120, 120, 120)
        else:
            font = font_body
            fill = (40, 40, 40)

        draw.text((margin, y), line, fill=fill, font=font)
        y += font.getbbox(line)[3] + line_spacing

    draw.text(
        (base_width - margin - int(300 * scale), base_height - int(200 * scale)),
        f"Seite {page_num} von {total_pages}",
        fill=(140, 140, 140),
        font=font_small,
    )

    if quality == "low_contrast":
        enhancer = ImageEnhance.Contrast(img)
        contrast_factor = 0.3 + (secrets.randbelow(3001) / 10000.0)
        img = enhancer.enhance(contrast_factor)

    return img


def create_invoice_tiff_bundle(bundle_index, scale: float = 1.0):
    suffix = f"{bundle_index:03d}"
    doc_id = f"DOC-INV-2026-{suffix}"
    zip_filename = f"invoice_{suffix}.zip"

    quality = secrets.choice(INVOICE_QUALITY_TYPES)

    with tempfile.TemporaryDirectory() as tmp_dir:
        pages, invoice_nr, sender, _ = create_invoice_pages(quality=quality)

        tiff_files = []
        for i, page_text in enumerate(pages, 1):
            img = render_page(page_text, i, len(pages), quality=quality, scale=scale)
            img = apply_random_transforms(img, quality=quality)
            img = add_realistic_noise(img, quality=quality)

            tiff_path = os.path.join(tmp_dir, f"{doc_id}_page{i:02d}.tiff")
            tf.imwrite(tiff_path, np.array(img), compression="zlib")
            tiff_files.append(tiff_path)

        zip_path = os.path.join(DEST_DIR, zip_filename)
        with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED) as zipf:
            for tiff in tiff_files:
                zipf.write(tiff, os.path.basename(tiff))

    metadata = {
        "id": doc_id,
        "docId": doc_id,
        "title": f"Rechnung {invoice_nr} - {sender} ({quality})",
        "creationDate": datetime.now().isoformat(),
        "clientId": f"CHE-{secrets.randbelow(900)+100}.{secrets.randbelow(900)+100}.{secrets.randbelow(900)+100}",
        "accountNo": f"CH{secrets.randbelow(90)+10}007620{''.join(str(secrets.randbelow(10)) for _ in range(10))}",
        "documentType": "INVOICE",
        "department": "Buchhaltung",
        "status": "ARCHIVED",
        "originalSizeBytes": os.path.getsize(zip_path),
        "pageCount": len(pages),
        "tags": ["rechnung", "2026", "finance", quality],
        "payloadUrl": f"/payloads/{zip_filename}",
        "quality_type": quality,
        "scale": scale,
    }

    return metadata


def create_bundle_with_scale(args):
    """Wrapper function for ProcessPoolExecutor – must be picklable"""
    bundle_index, scale = args
    return create_invoice_tiff_bundle(bundle_index, scale=scale)


def main():
    parser = argparse.ArgumentParser(
        description="Generate realistic invoice TIFF bundles"
    )
    parser.add_argument(
        "--scale",
        type=float,
        default=1.0,
        help="Scale factor for image size and font sizes (e.g. 0.5 = half size)",
    )

    args = parser.parse_args()

    scale = max(0.1, args.scale)  # prevent tiny/zero values

    os.makedirs(DEST_DIR, exist_ok=True)
    all_metadata = []

    print(
        f"Generating {TOTAL_BUNDLES} realistic invoice bundles "
        f"(scale={scale:.2f}, negative cases included)..."
    )

    with ProcessPoolExecutor(max_workers=4) as executor:
        # Use a picklable function instead of lambda
        tasks = [(i, scale) for i in range(1, TOTAL_BUNDLES + 1)]
        results = list(executor.map(create_bundle_with_scale, tasks))

    for meta in results:
        all_metadata.append(meta)
        print(
            f"Generated: {meta['payloadUrl']} ({meta['pageCount']} pages) "
            f"- Quality: {meta['quality_type']} - Scale: {meta['scale']}"
        )

    with open("db.json", "w", encoding="utf-8") as f:
        json.dump({"documents": all_metadata}, f, indent=2, ensure_ascii=False)

    print("\nDone. Check ./static/payloads/ and db.json")
    print(
        "Negative cases included: noisy, low_contrast, rotated_heavy, "
        "text_overlapping, garbage_chars"
    )


if __name__ == "__main__":
    main()
