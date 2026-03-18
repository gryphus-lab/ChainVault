/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.utils;

import ch.gryphus.chainvault.domain.TiffPage;
import java.awt.image.BufferedImage;
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
     * @throws IOException        the io exception
     * @throws TesseractException the tesseract exception
     */
    public static List<String> getOcrResults(List<TiffPage> pages, Tesseract tesseract)
            throws IOException, TesseractException {
        List<String> results = new ArrayList<>();
        if (pages != null && !pages.isEmpty()) {
            for (TiffPage page : pages) {
                try (ByteArrayInputStream bis = new ByteArrayInputStream(page.data())) {
                    BufferedImage image = ImageIO.read(bis);
                    if (!isImageValid(image)) {
                        results.add("");
                        continue;
                    }
                    // Pre-process (grayscale + contrast)
                    BufferedImage processed = preprocessImage(image);

                    String text = tesseract.doOCR(processed);
                    results.add(text.trim());
                }
            }
        }
        return results;
    }

    private static boolean isImageValid(BufferedImage image) {
        if (image == null
                || image.getWidth() <= 0
                || image.getHeight() <= 0) { // check nulls and zero size
            log.warn("Skipping invalid TIFF page: zero size");
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
        gray.getGraphics().drawImage(original, 0, 0, null);

        // Optional: binarization / threshold (use OpenCV or JavaCV if needed)
        // or simple contrast stretch
        return gray;
    }
}
