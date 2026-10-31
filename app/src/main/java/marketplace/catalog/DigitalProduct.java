package marketplace.catalog;

import java.math.BigDecimal;

public final class DigitalProduct extends Product {
    private final long downloadSizeBytes;

    public DigitalProduct(String id, String name, BigDecimal price, int quantity,
            long downloadSizeBytes) {
        super(id, name, price, quantity);
        if (downloadSizeBytes < 0) {
            throw new IllegalArgumentException("download size cannot be negative");
        }
        this.downloadSizeBytes = downloadSizeBytes;
    }

    public long downloadSizeBytes() {
        return downloadSizeBytes;
    }
}
