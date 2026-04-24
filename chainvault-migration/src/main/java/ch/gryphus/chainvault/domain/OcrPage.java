/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.domain;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Ocr page.
 */
@Getter
@Setter
public class OcrPage {

    private String name;

    private byte[] data;

    private String mimeType;

    private OcrSettings settings;

    /**
     * Instantiates a new Ocr page.
     *
     * @param name the name
     * @param data the data
     */
    public OcrPage(String name, byte[] data) {
        this(name, data, "image/tiff", new OcrSettings());
    }

    /**
     * Instantiates a new Ocr page.
     *
     * @param name     the name
     * @param data     the data
     * @param mimeType the mime type
     * @param settings the settings
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public OcrPage(String name, byte[] data, String mimeType, OcrSettings settings) {
        this.name = Objects.requireNonNull(name);
        this.data = Arrays.copyOf(Objects.requireNonNull(data), data.length);
        this.mimeType = Objects.requireNonNullElse(mimeType, "image/tiff");
        this.settings =
                settings != null
                        ? new OcrSettings(
                                settings.getLanguage(),
                                settings.getPageSegMode(),
                                settings.getOcrEngineMode(),
                                settings.getDpi(),
                                settings.isPreprocessEnabled())
                        : new OcrSettings();
    }

    /**
     * Is supported image boolean.
     *
     * @return the boolean
     */
    public boolean isSupportedImage() {
        return (mimeType.startsWith("image/") || "application/pdf".equals(mimeType));
    }

    public String toString() {
        return "OcrPage{name=%s, mimeType=%s}".formatted(getName(), getMimeType());
    }
}
