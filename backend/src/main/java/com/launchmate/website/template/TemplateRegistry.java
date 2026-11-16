package com.launchmate.website.template;

import com.launchmate.business.domain.BusinessCategory;
import com.launchmate.website.domain.CornerStyle;
import com.launchmate.website.domain.FontPairing;
import com.launchmate.website.domain.SectionItem;
import com.launchmate.website.domain.SectionType;
import com.launchmate.website.domain.Theme;
import com.launchmate.website.domain.ThemePalette;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.launchmate.website.domain.SectionType.ABOUT;
import static com.launchmate.website.domain.SectionType.CATALOGUE;
import static com.launchmate.website.domain.SectionType.CONTACT;
import static com.launchmate.website.domain.SectionType.CTA_BANNER;
import static com.launchmate.website.domain.SectionType.FAQ;
import static com.launchmate.website.domain.SectionType.HERO;
import static com.launchmate.website.domain.SectionType.HIGHLIGHTS;
import static com.launchmate.website.domain.SectionType.HOURS_LOCATION;
import static com.launchmate.website.domain.SectionType.PROCESS;

/** The built-in templates, one per business vertical. */
@Component
public class TemplateRegistry {

    private static final Set<SectionType> ALL_SECTIONS = EnumSet.allOf(SectionType.class);

    private final Map<String, WebsiteTemplate> templates = new LinkedHashMap<>();

    public TemplateRegistry() {
        register(new WebsiteTemplate("bakery", "Sweet Crumb", BusinessCategory.BAKERY_FOOD,
                "Warm, appetising layout for bakeries, home kitchens and food businesses.",
                new Theme("#9F3A50", "#E9B872", ThemePalette.WARM, FontPairing.ELEGANT, CornerStyle.ROUNDED),
                List.of(HERO, HIGHLIGHTS, CATALOGUE, ABOUT, PROCESS, FAQ, HOURS_LOCATION, CONTACT),
                ALL_SECTIONS,
                "Warm, inviting and sensory. Talk about freshness, ingredients, celebrations and ordering ahead.",
                new StarterCopy(
                        "Freshly baked treats made to order in {city}.",
                        "Order now",
                        "About {name}",
                        "{name} bakes everything fresh to order. Tell us about your occasion and we'll make something special for you.",
                        "Why customers choose us",
                        List.of(new SectionItem("Baked fresh to order", "Nothing sits on a shelf — your order is baked for you."),
                                new SectionItem("Made for your occasion", "Birthdays, anniversaries and festivals, customised the way you like."),
                                new SectionItem("Easy ordering", "Send an inquiry and we'll confirm the details with you.")),
                        "Our menu", "Ask us about flavours, sizes and custom designs.",
                        "How ordering works",
                        List.of(new SectionItem("Send an inquiry", "Tell us what you'd like and when you need it."),
                                new SectionItem("We confirm details", "We'll get back to you to confirm design, size and price."),
                                new SectionItem("Pick up or delivery", "Collect your order or arrange delivery.")),
                        "Questions",
                        List.of(new SectionItem("How early should I order?", "Please send your inquiry as early as possible so we can plan your order."),
                                new SectionItem("Can you customise designs?", "Yes — share your idea in the inquiry form and we'll discuss it with you.")),
                        "Planning a celebration?", "Tell us what you have in mind and we'll get back to you.",
                        "Place an inquiry", "Share your order details and we'll reply shortly.")));

        register(new WebsiteTemplate("salon", "Glow Studio", BusinessCategory.BEAUTY_SALON,
                "Elegant layout for salons, beauty parlours, spas and makeup artists.",
                new Theme("#7A3E6E", "#E7C6D8", ThemePalette.LIGHT, FontPairing.ELEGANT, CornerStyle.SOFT),
                List.of(HERO, CATALOGUE, HIGHLIGHTS, ABOUT, FAQ, HOURS_LOCATION, CONTACT),
                ALL_SECTIONS,
                "Calm, confident and caring. Emphasise hygiene, personal attention and booking an appointment.",
                new StarterCopy(
                        "Beauty and care services in {city}.",
                        "Book an appointment",
                        "About {name}",
                        "At {name} we take time to understand what you want and give every client personal attention.",
                        "What to expect",
                        List.of(new SectionItem("Personal attention", "Every appointment is tailored to you."),
                                new SectionItem("Hygiene first", "Clean tools and a clean space, every time."),
                                new SectionItem("Easy booking", "Send a request and we'll confirm your slot.")),
                        "Services", "Ask us about packages for weddings and events.",
                        "How booking works",
                        List.of(new SectionItem("Request a slot", "Choose a service and a preferred time."),
                                new SectionItem("We confirm", "We'll confirm your appointment."),
                                new SectionItem("Relax", "Arrive and enjoy your session.")),
                        "Questions",
                        List.of(new SectionItem("Do you offer bridal packages?", "Send us an inquiry with your date and requirements."),
                                new SectionItem("Do I need an appointment?", "Appointments are recommended so we can give you our full attention.")),
                        "Ready for your next appointment?", "Send a booking request in under a minute.",
                        "Book an appointment", "Tell us the service and a time that suits you.")));

        register(new WebsiteTemplate("home-services", "FixRight", BusinessCategory.HOME_SERVICES,
                "Trust-focused layout for electricians, plumbers, cleaners, repair and pest control.",
                new Theme("#1F5FA8", "#F2A93B", ThemePalette.COOL, FontPairing.MODERN, CornerStyle.SOFT),
                List.of(HERO, CATALOGUE, PROCESS, HIGHLIGHTS, FAQ, HOURS_LOCATION, CONTACT),
                ALL_SECTIONS,
                "Clear, reliable and practical. Emphasise responsiveness, service areas and transparent quotes.",
                new StarterCopy(
                        "Reliable home services in {city}.",
                        "Request a visit",
                        "About {name}",
                        "{name} provides dependable home services. Tell us the problem and we'll get back to you with a visit time and quote.",
                        "Why choose us",
                        List.of(new SectionItem("Clear quotes", "We tell you the cost before starting work."),
                                new SectionItem("Respectful service", "We work carefully and leave your home clean."),
                                new SectionItem("Quick response", "Send a request and we'll reply as soon as we can.")),
                        "Services", "Not sure what you need? Describe the problem in the form below.",
                        "How it works",
                        List.of(new SectionItem("Describe the problem", "Tell us what needs fixing and where."),
                                new SectionItem("Get a quote", "We'll confirm the visit time and cost."),
                                new SectionItem("Job done", "We complete the work and you pay for what was agreed.")),
                        "Questions",
                        List.of(new SectionItem("Which areas do you cover?", "Send us your locality in the inquiry form and we'll confirm."),
                                new SectionItem("Do you charge for a visit?", "We'll confirm any visit charges before coming over.")),
                        "Something needs fixing?", "Describe the problem and we'll get back to you.",
                        "Request a service", "Tell us what you need and your locality.")));

        register(new WebsiteTemplate("tutoring", "BrightPath", BusinessCategory.EDUCATION_TUTORING,
                "Friendly layout for tutors, coaching classes, music and skill teachers.",
                new Theme("#2F5D50", "#F4C95D", ThemePalette.LIGHT, FontPairing.FRIENDLY, CornerStyle.ROUNDED),
                List.of(HERO, CATALOGUE, HIGHLIGHTS, ABOUT, PROCESS, FAQ, CONTACT),
                ALL_SECTIONS,
                "Encouraging and clear, addressed to both students and parents. Emphasise approach and outcomes without inventing results.",
                new StarterCopy(
                        "Classes and tutoring in {city}.",
                        "Enquire about classes",
                        "About {name}",
                        "{name} helps students learn with patience and clear explanations. Get in touch to discuss your goals.",
                        "Our approach",
                        List.of(new SectionItem("Small groups", "More attention for every student."),
                                new SectionItem("Concept clarity", "We focus on understanding, not memorising."),
                                new SectionItem("Regular feedback", "Parents stay informed about progress.")),
                        "Courses", "Ask us about batches, timings and fees.",
                        "Getting started",
                        List.of(new SectionItem("Send an enquiry", "Tell us the class, subject and goals."),
                                new SectionItem("Talk to us", "We'll discuss batches and timings."),
                                new SectionItem("Start learning", "Join a batch that suits you.")),
                        "Questions",
                        List.of(new SectionItem("Do you offer a demo class?", "Ask about a demo class in your enquiry."),
                                new SectionItem("Are classes online or offline?", "Mention your preference and we'll let you know the options.")),
                        "Want to know more?", "Send an enquiry and we'll get back to you.",
                        "Enquire now", "Tell us the class or subject you're interested in.")));

        register(new WebsiteTemplate("handmade", "Maker's Table", BusinessCategory.HANDMADE_RETAIL,
                "Crafted layout for handmade goods, boutiques, jewellery and small retail.",
                new Theme("#8A4B2A", "#D9A441", ThemePalette.WARM, FontPairing.CLASSIC, CornerStyle.SOFT),
                List.of(HERO, CATALOGUE, ABOUT, HIGHLIGHTS, FAQ, CONTACT),
                ALL_SECTIONS,
                "Personal and crafted. Tell the maker's story, emphasise materials, care and custom orders.",
                new StarterCopy(
                        "Handmade with care in {city}.",
                        "Shop the collection",
                        "The story behind {name}",
                        "Every piece from {name} is made by hand with care. Get in touch for custom orders and gifting.",
                        "Why handmade",
                        List.of(new SectionItem("Made by hand", "Each piece is crafted individually."),
                                new SectionItem("Custom orders", "Personalise colours, sizes and designs."),
                                new SectionItem("Thoughtful gifting", "Ask us about gift packing.")),
                        "Collection", "Interested in something? Send an inquiry to order.",
                        "How to order",
                        List.of(new SectionItem("Choose a piece", "Browse the collection or describe a custom idea."),
                                new SectionItem("We confirm", "We'll confirm availability, price and delivery."),
                                new SectionItem("Delivered to you", "Your order is packed with care.")),
                        "Questions",
                        List.of(new SectionItem("Do you take custom orders?", "Yes — describe your idea in the inquiry form."),
                                new SectionItem("Do you deliver?", "Tell us your location and we'll confirm delivery options.")),
                        "Looking for something special?", "Tell us your idea and we'll make it for you.",
                        "Order or ask a question", "We'll get back to you to confirm the details.")));

        register(new WebsiteTemplate("professional", "Clarity", BusinessCategory.PROFESSIONAL_SERVICES,
                "Clean layout for freelancers, consultants, designers, accountants and agencies.",
                new Theme("#1E293B", "#3B82F6", ThemePalette.LIGHT, FontPairing.MODERN, CornerStyle.SHARP),
                List.of(HERO, CATALOGUE, ABOUT, PROCESS, FAQ, CTA_BANNER, CONTACT),
                ALL_SECTIONS,
                "Professional, concise and confident. Focus on the client's problem, the service and the next step.",
                new StarterCopy(
                        "Professional services from {city}, for clients anywhere.",
                        "Start a project",
                        "About {name}",
                        "{name} helps clients get work done well and on time. Get in touch to discuss what you need.",
                        "Working with us",
                        List.of(new SectionItem("Clear scope", "We agree on deliverables up front."),
                                new SectionItem("Reliable timelines", "You know when to expect results."),
                                new SectionItem("Direct communication", "Talk to the person doing the work.")),
                        "Services", "Every engagement is tailored to your needs.",
                        "How we work",
                        List.of(new SectionItem("Tell us your needs", "Share your goals and timelines."),
                                new SectionItem("Get a proposal", "We send scope, timeline and price."),
                                new SectionItem("Delivery", "We do the work and keep you updated.")),
                        "Questions",
                        List.of(new SectionItem("Do you work with clients remotely?", "Yes — mention your location and preferences in your message."),
                                new SectionItem("How is pricing decided?", "Pricing depends on scope; we'll send a clear proposal.")),
                        "Have a project in mind?", "Tell us about it and get a proposal.",
                        "Get in touch", "Describe your project and we'll respond with next steps.")));
    }

    private void register(WebsiteTemplate t) {
        templates.put(t.id(), t);
    }

    public List<WebsiteTemplate> all() {
        return List.copyOf(templates.values());
    }

    public Optional<WebsiteTemplate> find(String id) {
        return Optional.ofNullable(id == null ? null : templates.get(id));
    }

    public WebsiteTemplate require(String id) {
        return find(id).orElseThrow(() -> new IllegalArgumentException("Unknown template " + id));
    }

    public WebsiteTemplate forCategory(BusinessCategory category) {
        return templates.values().stream().filter(t -> t.category() == category).findFirst()
                .orElseThrow(() -> new IllegalStateException("No template for " + category));
    }
}
