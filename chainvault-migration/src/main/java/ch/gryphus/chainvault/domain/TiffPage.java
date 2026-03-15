/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.domain;

import java.util.Arrays;
import java.util.Objects;
import lombok.NonNull;

/**
 * The type Tiff page.
 *
 * @param name
 * @param data
 */
public record TiffPage(String name, byte[] data) {
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TiffPage tiffPage = (TiffPage) obj;
        return Objects.equals(name, tiffPage.name) && Arrays.equals(data, tiffPage.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    @NonNull
    public String toString() {
        return "TiffPage{name=%s}".formatted(name);
    }
}
