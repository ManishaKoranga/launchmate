package com.launchmate.catalogue.controller;

import com.launchmate.catalogue.domain.ProductInput;
import com.launchmate.catalogue.dto.ProductResponse;
import com.launchmate.catalogue.service.ProductImageService;
import com.launchmate.catalogue.service.ProductImageUrls;
import com.launchmate.catalogue.service.ProductService;
import com.launchmate.shared.tenant.TenantContext;
import com.launchmate.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/businesses/{businessId}/products")
@Tag(name = "Products & services")
public class ProductController {

    private final ProductService productService;
    private final ProductImageUrls imageUrls;
    private final TenantContext tenant;
    private final ProductImageService imageService;

    public ProductController(ProductService productService, ProductImageUrls imageUrls, TenantContext tenant,
                             ProductImageService imageService) {
        this.imageService = imageService;
        this.productService = productService;
        this.imageUrls = imageUrls;
        this.tenant = tenant;
    }

    @Operation(summary = "List products and services")
    @GetMapping
    public PageResponse<ProductResponse> list(@PathVariable UUID businessId,
                                              @RequestParam(defaultValue = "0") @Min(0) int page,
                                              @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
        return PageResponse.of(productService.list(tenant.businessId(), page, size), imageUrls::toResponse);
    }

    @Operation(summary = "Get a product")
    @GetMapping("/{productId}")
    public ProductResponse get(@PathVariable UUID businessId, @PathVariable UUID productId) {
        return imageUrls.toResponse(productService.get(tenant.businessId(), productId));
    }

    @Operation(summary = "Create a product or service")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@PathVariable UUID businessId, @Valid @RequestBody ProductInput input) {
        return imageUrls.toResponse(productService.create(tenant.businessId(), tenant.userId(), input));
    }

    @Operation(summary = "Update a product or service")
    @PutMapping("/{productId}")
    public ProductResponse update(@PathVariable UUID businessId, @PathVariable UUID productId,
                                  @Valid @RequestBody ProductInput input) {
        return imageUrls.toResponse(productService.update(tenant.businessId(), tenant.userId(), productId, input));
    }

    @Operation(summary = "Delete a product or service")
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID businessId, @PathVariable UUID productId) {
        productService.delete(tenant.businessId(), tenant.userId(), productId);
    }

    @Operation(summary = "Upload or replace the product image (JPEG, PNG or WebP, max 5 MB)")
    @PostMapping(value = "/{productId}/image", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponse uploadImage(@PathVariable UUID businessId, @PathVariable UUID productId,
                                       @org.springframework.web.bind.annotation.RequestPart("file")
                                       org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        return imageUrls.toResponse(imageService.upload(tenant.businessId(), tenant.userId(), productId, file.getBytes()));
    }

    @Operation(summary = "Remove the product image")
    @DeleteMapping("/{productId}/image")
    public ProductResponse removeImage(@PathVariable UUID businessId, @PathVariable UUID productId) {
        return imageUrls.toResponse(imageService.remove(tenant.businessId(), tenant.userId(), productId));
    }
}
