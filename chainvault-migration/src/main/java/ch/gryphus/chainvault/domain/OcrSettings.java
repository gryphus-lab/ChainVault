/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Per-page OCR configuration for Tesseract.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OcrSettings {

    private String language = "eng+deu"; // default languages
    private int pageSegMode = 3; // PSM_AUTO
    private int ocrEngineMode = 3; // LSTM + legacy
    private int dpi = 300; // default DPI
    private boolean preprocessEnabled = true; // grayscale + contrast
}
