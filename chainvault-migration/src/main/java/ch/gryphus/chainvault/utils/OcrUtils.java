/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.utils;

import ch.gryphus.chainvault.domain.OcrPage;
import ch.gryphus.chainvault.domain.OcrSettings;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.RescaleOp;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 * The type Ocr utils.
 */
@Slf4j
public final class OcrUtils {
    private OcrUtils() {
        /* This utility class should not be instantiated */
    }

    /**
     * Gets ocr results.
     *
     * @param pages     the pages
     * @param tesseract the tesseract
     * @return the ocr results
     * @throws TesseractException the tesseract exception
     * @throws IOException        the io exception
     */
    public static List<String> getOcrResults(List<? extends OcrPage> pages, Tesseract tesseract)
            throws TesseractException, IOException {
        List<String> results = new ArrayList<>();

        if (pages != null) {
            for (OcrPage page : pages) {
                if (!page.isSupportedImage()) {
                    log.warn(
                            "Unsupported format for OCR: {} ({})",
                            page.getName(),
                            page.getMimeType());
                    results.add("");
                    continue;
                }

                try (ByteArrayInputStream bis = new ByteArrayInputStream(page.getData())) {
                    BufferedImage image = ImageIO.read(bis);

                    // Defensive checks
                    if (isValidImageSize(image)) {
                        OcrSettings settings = page.getSettings();
                        tesseract.setLanguage(settings.getLanguage());
                        tesseract.setPageSegMode(settings.getPageSegMode());
                        tesseract.setOcrEngineMode(settings.getOcrEngineMode());
                        tesseract.setVariable(
                                "user_defined_dpi", String.valueOf(settings.getDpi()));

                        // Pre-processing if enabled
                        BufferedImage processed =
                                settings.isPreprocessEnabled() ? preprocessImage(image) : image;

                        String text = tesseract.doOCR(processed);
                        results.add(text.trim());
                    }
                }
            }
        }

        return results;
    }

    private static boolean isValidImageSize(BufferedImage image) {
        if (image == null
                || image.getWidth() <= 0
                || image.getHeight() <= 0) { // check nulls and zero size
            log.warn("Skipping invalid page: zero size");
            return false;
        }
        if (image.getWidth() < 10 || image.getHeight() < 10) { // check width and height < 10
            log.warn("Skipping tiny image: {}x{}", image.getWidth(), image.getHeight());
            return false;
        }
        return true;
    }

    private static BufferedImage preprocessImage(BufferedImage original) {
        // Grayscale
        BufferedImage gray =
                new BufferedImage(
                        original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        ColorConvertOp op = new ColorConvertOp(null);
        op.filter(original, gray);

        // Contrast enhancement
        RescaleOp rescale = new RescaleOp(1.8f, -40.0f, null);
        rescale.filter(gray, gray);

        return gray;
    }
}
