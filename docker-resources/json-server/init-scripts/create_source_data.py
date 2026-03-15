import json
import os
import random
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


def load_font(size, bold=False):
    for path in FONT_PATHS:
        try:
            if bold and ("Bold" in path or "bold" in path.lower()):
                return ImageFont.truetype(path, size)
            if not bold:
                return ImageFont.truetype(path, size)
        except IOError:
            continue
    return ImageFont.load_default()


def add_realistic_noise(image: Image.Image) -> Image.Image:
    img_array = np.array(image.convert("L"))  # grayscale for noise

    # Gaussian noise (moderate)
    gauss_noise = np.random.normal(0, 10, img_array.shape).astype(np.int16)
    noisy = img_array.astype(np.int16) + gauss_noise
    noisy = np.clip(noisy, 0, 255).astype(np.uint8)

    # Salt and pepper (very light)
    salt_pepper = np.random.rand(*img_array.shape) < 0.0015
    noisy[salt_pepper] = 255 if np.random.rand() > 0.5 else 0

    noisy_img = Image.fromarray(noisy)

    # Light vignette (darker edges)
    mask = Image.new("L", noisy_img.size, 255)
    draw = ImageDraw.Draw(mask)
    w, h = mask.size
    draw.rectangle((0, 0, w, h), fill=255)
    draw.ellipse((-w // 5, -h // 5, w * 6 // 5, h * 6 // 5), fill=200)
    vignette = ImageEnhance.Brightness(noisy_img).enhance(0.94)
    vignette.putalpha(mask)

    return vignette.convert("RGB")


def apply_random_transforms(image: Image.Image) -> Image.Image:
    """Apply small rotation, scaling, slight perspective distortion"""
    # Random rotation ±1.5 degrees
    angle = secrets.randbelow(3001) / 1000 - 1.5  # uniform [-1.5, 1.5]
    rotated = image.rotate(angle, resample=Image.BICUBIC, expand=True)

    # Random scale 98–102%
    scale = 0.98 + secrets.randbelow(401) / 10000  # [0.98, 1.02]
    scaled_w = int(rotated.width * scale)
    scaled_h = int(rotated.height * scale)
    scaled = rotated.resize((scaled_w, scaled_h), Image.LANCZOS)

    # Light perspective (page curl simulation) – only sometimes
    if secrets.randbelow(100) < 35:  # ~35% chance
        coeffs = [
            1.0,
            0.0,
            0.0,
            0.0,
            1.0,
            0.0,
            secrets.randbelow(7) / 10000 + 0.0002,  # x warp
            secrets.randbelow(5) / 10000 + 0.0001,  # y warp
        ]
        scaled = scaled.transform(scaled.size, Image.PERSPECTIVE, coeffs, Image.BICUBIC)

    return scaled


def create_invoice_pages():
    """Generate 1–5 pages of realistic invoice content"""
    num_pages = random.randrange(*PAGES_PER_INVOICE)  # random is fine for range here
    pages = []

    invoice_date = datetime.now() - timedelta(days=secrets.randbelow(60))
    due_date = invoice_date + timedelta(days=30)
    invoice_nr = (
        f"INV-{invoice_date.strftime('%Y%m')}-{secrets.randbelow(90000) + 10000}"
    )
    sender = secrets.choice(COMPANIES)
    client = secrets.choice(CLIENTS)

    # Header & master data (appears on every page)
    header_lines = [
        sender.upper(),
        "Musterstrasse 12, 8001 Zürich",
        f"MwSt-Nr: CHE-{secrets.randbelow(900) + 100}.{secrets.randbelow(900) + 100}.{secrets.randbelow(900) + 100} MWST",
        "",
        f"Rechnungs-Nr: {invoice_nr}",
        f"Rechnungsdatum: {invoice_date.strftime('%d.%m.%Y')}",
        f"Fälligkeitsdatum: {due_date.strftime('%d.%m.%Y')}",
        "",
        f"Kunde: {client}",
        "Musterweg 45, 3000 Bern",
    ]

    # Line items (5–15 positions total, distributed across pages)
    total_items = secrets.randbelow(11) + 5  # 5–15
    # FIXED: use choices() or list comprehension
    sys_random = secrets.SystemRandom()
    items = sys_random.choices(ITEM_DESCRIPTIONS, k=total_items)
    # Alternative one-liner:
    # items = [secrets.choice(ITEM_DESCRIPTIONS) for _ in range(total_items)]

    subtotal = 0
    line_items = []

    for desc, unit_price in items:
        qty = secrets.choice([1, 1, 2, 4, 8, 10, 20, 40, 80, 120])
        amount = qty * unit_price
        subtotal += amount
        line_items.append((desc, qty, unit_price, amount))

    vat = subtotal * 0.081
    total = subtotal + vat

    # Split items across pages
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

        pages.append(page_text)

    return pages, invoice_nr, sender, client


def render_page(text_lines, page_num, total_pages):
    img = Image.new("RGB", (1240, 1754), color=(250, 250, 245))  # light off-white paper
    draw = ImageDraw.Draw(img)

    font_header = load_font(36, bold=True)
    font_subheader = load_font(24, bold=True)
    font_body = load_font(16)
    font_small = load_font(13)

    y = 180

    # Header
    draw.text((200, y), "INVOICE", font=font_header, fill=(0, 51, 102))
    y += 140

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

        draw.text((220, y), line, fill=fill, font=font)
        y += font.getbbox(line)[3] + 12  # dynamic line spacing

    # Page number
    page_txt = f"Seite {page_num} von {total_pages}"
    draw.text((1100, 1650), page_txt, fill=(140, 140, 140), font=font_small)

    return img


def create_invoice_tiff_bundle(bundle_index):
    suffix = f"{bundle_index:03d}"
    doc_id = f"DOC-INV-2026-{suffix}"
    zip_filename = f"invoice_{suffix}.zip"

    with tempfile.TemporaryDirectory() as tmp_dir:
        pages, invoice_nr, sender, _ = create_invoice_pages()

        tiff_files = []
        for i, page_text in enumerate(pages, 1):
            img = render_page(page_text, i, len(pages))
            img = apply_random_transforms(img)
            img = add_realistic_noise(img)

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
        "title": f"Rechnung {invoice_nr} - {sender}",
        "creationDate": datetime.now().isoformat(),
        "clientId": f"CHE-{secrets.randbelow(900) + 100}.{secrets.randbelow(900) + 100}.{secrets.randbelow(900) + 100}",
        "accountNo": f"CH{secrets.randbelow(90) + 10}007620{''.join(str(secrets.randbelow(10)) for _ in range(10))}",
        "documentType": "INVOICE",
        "department": "Buchhaltung",
        "status": "ARCHIVED",
        "originalSizeBytes": os.path.getsize(zip_path),
        "pageCount": len(pages),
        "tags": ["rechnung", "2026", "finance"],
        "payloadUrl": f"/payloads/{zip_filename}",
    }

    return metadata


def main():
    os.makedirs(DEST_DIR, exist_ok=True)
    all_metadata = []

    print(f"Generating {TOTAL_BUNDLES} realistic invoice bundles...")

    with ProcessPoolExecutor(max_workers=4) as executor:
        results = list(
            executor.map(create_invoice_tiff_bundle, range(1, TOTAL_BUNDLES + 1))
        )

    for meta in results:
        all_metadata.append(meta)
        print(f"Generated: {meta['payloadUrl']} ({meta['pageCount']} pages)")

    with open("db.json", "w", encoding="utf-8") as f:
        json.dump({"documents": all_metadata}, f, indent=2, ensure_ascii=False)

    print("\nDone. Check ./static/payloads/ and db.json")


if __name__ == "__main__":
    main()
