package ch.gryphus.chainvault.domain;

import java.util.Arrays;
import java.util.Objects;

public record TiffPage(String name, byte[] data) implements AbstractPage {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TiffPage tiffPage = (TiffPage) o;
        return Objects.equals(name, tiffPage.name) && Arrays.equals(data, tiffPage.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {
        return "TiffPage{name=%s}".formatted(name);
    }
}
