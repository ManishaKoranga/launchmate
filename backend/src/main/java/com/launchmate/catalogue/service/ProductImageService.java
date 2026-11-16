package com.launchmate.catalogue.service;

import com.launchmate.catalogue.domain.Product;
import com.launchmate.shared.audit.AuditEvent;
import com.launchmate.shared.audit.AuditService;
import com.launchmate.shared.storage.ImageValidator;
import com.launchmate.shared.storage.ObjectStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProductImageService {

    private final ProductService products;
    private final ObjectStorage storage;
    private final AuditService audit;

    public ProductImageService(ProductService products, ObjectStorage storage, AuditService audit) {
        this.products = products;
        this.storage = storage;
        this.audit = audit;
    }

    @Transactional
    public Product upload(UUID businessId, UUID actorUserId, UUID productId, byte[] data) {
        Product product = products.get(businessId, productId); // tenant check before touching storage
        ImageValidator.ImageType type = ImageValidator.detect(data);
        String key = "products/" + productId + "/" + UUID.randomUUID() + "." + type.extension();
        storage.put(key, data, type.contentType());
        String previous = product.getImageKey();
        products.setImageKey(businessId, productId, key);
        if (previous != null) {
            storage.delete(previous);
        }
        audit.record(AuditEvent.byUser(businessId, actorUserId, "product.image_uploaded", "Product", productId, null));
        return product;
    }

    @Transactional
    public Product remove(UUID businessId, UUID actorUserId, UUID productId) {
        Product product = products.get(businessId, productId);
        if (product.getImageKey() != null) {
            storage.delete(product.getImageKey());
            products.setImageKey(businessId, productId, null);
            audit.record(AuditEvent.byUser(businessId, actorUserId, "product.image_removed", "Product", productId, null));
        }
        return product;
    }
}
