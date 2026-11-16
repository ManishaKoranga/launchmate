package com.launchmate.catalogue.service;

import com.launchmate.catalogue.domain.Product;
import com.launchmate.catalogue.dto.ProductResponse;
import org.springframework.stereotype.Component;

/** Maps products to API responses, resolving stored image keys to public URLs. */
@Component
public class ProductImageUrls {

    public ProductResponse toResponse(Product p) {
        String url = p.getImageKey() == null ? null : "/api/public/assets/" + p.getImageKey();
        return ProductResponse.from(p, url);
    }
}
