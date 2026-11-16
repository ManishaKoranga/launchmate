package com.launchmate.website.generation;

import com.launchmate.business.domain.Business;
import com.launchmate.business.service.BusinessService;
import com.launchmate.catalogue.domain.Product;
import com.launchmate.catalogue.service.ProductService;
import com.launchmate.shared.exception.ExternalServiceException;
import com.launchmate.website.domain.DraftSource;
import com.launchmate.website.domain.SiteConfig;
import com.launchmate.website.service.WebsiteService;
import com.launchmate.website.template.StarterSiteFactory;
import com.launchmate.website.template.TemplateRegistry;
import com.launchmate.website.template.WebsiteTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

import static com.launchmate.website.generation.GenerationJobService.STEP_CONTENT;
import static com.launchmate.website.generation.GenerationJobService.STEP_PROFILE;
import static com.launchmate.website.generation.GenerationJobService.STEP_SAVE;
import static com.launchmate.website.generation.GenerationJobService.STEP_VALIDATE;

/**
 * Executes a generation job off the request thread. Each step's status is committed as it happens,
 * which is what the progress screen polls. AI output that fails validation gets one corrective retry
 * with the problems fed back to the model.
 */
@Component
class WebsiteGenerationRunner {

    private static final Logger log = LoggerFactory.getLogger(WebsiteGenerationRunner.class);
    private static final int MAX_AI_ATTEMPTS = 2;

    private final GenerationJobService jobService;
    private final GenerationProgress progress;
    private final BusinessService businesses;
    private final ProductService products;
    private final TemplateRegistry templates;
    private final StarterSiteFactory starterFactory;
    private final WebsiteService websites;
    private final WebsiteContentGenerator generator;

    WebsiteGenerationRunner(GenerationJobService jobService, GenerationProgress progress, BusinessService businesses,
                            ProductService products, TemplateRegistry templates, StarterSiteFactory starterFactory,
                            WebsiteService websites, WebsiteContentGenerator generator) {
        this.jobService = jobService;
        this.progress = progress;
        this.businesses = businesses;
        this.products = products;
        this.templates = templates;
        this.starterFactory = starterFactory;
        this.websites = websites;
        this.generator = generator;
    }

    @Async("generationExecutor")
    @TransactionalEventListener
    public void onRequested(GenerationRequested event) {
        GenerationJob job = jobService.get(event.businessId(), event.jobId());
        try {
            run(job, event);
        } catch (ExternalServiceException e) {
            log.warn("Website generation {} failed: {}", job.getId(), e.getMessage());
            progress.fail(job.getId(), e.isNotConfigured()
                    ? "AI is not available on this server. You can start from template content instead."
                    : "The AI service is temporarily unavailable. Please try again, or start from template content.");
        } catch (GenerationFailedException e) {
            log.warn("Website generation {} produced invalid output: {}", job.getId(), e.getMessage());
            progress.fail(job.getId(), e.getMessage());
        } catch (RuntimeException e) {
            log.error("Website generation {} crashed", job.getId(), e);
            progress.fail(job.getId(), "Something went wrong while building your website. Please try again.");
        }
    }

    private void run(GenerationJob job, GenerationRequested event) {
        progress.start(job.getId(), STEP_PROFILE);
        Business business = businesses.get(event.businessId());
        WebsiteTemplate template = websites.find(event.businessId())
                .flatMap(w -> templates.find(w.getTemplateId()))
                .orElseGet(() -> templates.forCategory(business.getCategory()));
        List<String> catalogue = products.listVisible(event.businessId()).stream()
                .map(WebsiteGenerationRunner::describe).toList();
        progress.complete(job.getId(), STEP_PROFILE);

        SiteConfig config;
        DraftSource source;
        if (job.getMode() == GenerationMode.AI) {
            config = generateWithAi(job, business, template, catalogue);
            source = DraftSource.AI_GENERATED;
        } else {
            progress.start(job.getId(), STEP_CONTENT);
            config = starterFactory.create(template, business);
            progress.complete(job.getId(), STEP_CONTENT);
            progress.start(job.getId(), STEP_VALIDATE);
            websites.requireValid(config);
            progress.complete(job.getId(), STEP_VALIDATE);
            source = DraftSource.TEMPLATE_STARTER;
        }

        progress.start(job.getId(), STEP_SAVE);
        websites.saveGeneratedDraft(event.businessId(), event.userId(), config, source);
        progress.complete(job.getId(), STEP_SAVE);
        progress.succeed(job.getId());
    }

    private SiteConfig generateWithAi(GenerationJob job, Business business, WebsiteTemplate template, List<String> catalogue) {
        SiteConfig candidate = null;
        List<String> problems = List.of();
        for (int attempt = 1; attempt <= MAX_AI_ATTEMPTS; attempt++) {
            progress.start(job.getId(), STEP_CONTENT);
            candidate = generator.generate(business, template, catalogue, candidate, problems);
            progress.complete(job.getId(), STEP_CONTENT);

            progress.start(job.getId(), STEP_VALIDATE);
            problems = websites.problemsWith(candidate);
            if (problems.isEmpty()) {
                progress.complete(job.getId(), STEP_VALIDATE);
                return candidate;
            }
            log.info("Generated site for job {} failed validation (attempt {}): {}", job.getId(), attempt, problems);
        }
        throw new GenerationFailedException(
                "The AI produced a website that didn't pass our quality checks. Please try again or start from template content.");
    }

    /** e.g. "Customised Cakes — ₹650 (starting from, per kg)"; only facts the owner entered. */
    private static String describe(Product p) {
        if (p.getPrice() == null) {
            return p.getName();
        }
        String price = "₹" + p.getPrice().stripTrailingZeros().toPlainString();
        return p.getName() + " — " + price + (p.getPriceNote() == null ? "" : " (" + p.getPriceNote() + ")");
    }

    static class GenerationFailedException extends RuntimeException {
        GenerationFailedException(String message) {
            super(message);
        }
    }
}
