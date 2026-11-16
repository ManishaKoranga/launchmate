package com.launchmate.catalogue.service;

import com.launchmate.business.domain.PlanLimits;
import com.launchmate.business.service.BusinessService;
import com.launchmate.catalogue.domain.Product;
import com.launchmate.catalogue.domain.ProductInput;
import com.launchmate.catalogue.repository.ProductRepository;
import com.launchmate.shared.audit.AuditEvent;
import com.launchmate.shared.audit.AuditService;
import com.launchmate.shared.exception.BusinessRuleException;
import com.launchmate.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Product/service catalogue use cases. All operations are scoped to an already-authorised business. */
@Service
public class ProductService {

    private final ProductRepository products;
    private final BusinessService businesses;
    private final AuditService audit;

    public ProductService(ProductRepository products, BusinessService businesses, AuditService audit) {
        this.products = products;
        this.businesses = businesses;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public Page<Product> list(UUID businessId, int page, int size) {
        return products.findByBusinessId(businessId,
                PageRequest.of(page, size, Sort.by("sortOrder").ascending().and(Sort.by("createdAt").ascending())));
    }

    @Transactional(readOnly = true)
    public List<Product> listVisible(UUID businessId) {
        return products.findByBusinessIdAndVisibleTrueOrderBySortOrderAscCreatedAtAsc(businessId);
    }

    @Transactional(readOnly = true)
    public Product get(UUID businessId, UUID productId) {
        return products.findByIdAndBusinessId(productId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Product"));
    }

    @Transactional(readOnly = true)
    public long count(UUID businessId) {
        return products.countByBusinessId(businessId);
    }

    @Transactional
    public Product create(UUID businessId, UUID actorUserId, ProductInput input) {
        return createAll(businessId, actorUserId, List.of(input)).getFirst();
    }

    @Transactional
    public List<Product> createAll(UUID businessId, UUID actorUserId, List<ProductInput> inputs) {
        int limit = PlanLimits.of(businesses.get(businessId).getPlan()).maxProducts();
        if (products.countByBusinessId(businessId) + inputs.size() > limit) {
            throw new BusinessRuleException("Your plan allows up to " + limit + " products and services");
        }
        int order = products.maxSortOrder(businessId);
        List<Product> created = new java.util.ArrayList<>();
        for (ProductInput input : inputs) {
            Product p = products.save(new Product(businessId, input, ++order));
            audit.record(AuditEvent.byUser(businessId, actorUserId, "product.created", "Product", p.getId(),
                    Map.of("name", p.getName())));
            created.add(p);
        }
        return created;
    }

    @Transactional
    public Product update(UUID businessId, UUID actorUserId, UUID productId, ProductInput input) {
        Product p = get(businessId, productId);
        p.apply(input);
        audit.record(AuditEvent.byUser(businessId, actorUserId, "product.updated", "Product", p.getId(),
                Map.of("name", p.getName())));
        return p;
    }

    @Transactional
    public void delete(UUID businessId, UUID actorUserId, UUID productId) {
        Product p = get(businessId, productId);
        products.delete(p);
        audit.record(AuditEvent.byUser(businessId, actorUserId, "product.deleted", "Product", p.getId(),
                Map.of("name", p.getName())));
    }

    @Transactional
    public Product setImageKey(UUID businessId, UUID productId, String imageKey) {
        Product p = get(businessId, productId);
        p.setImageKey(imageKey);
        return p;
    }
}
