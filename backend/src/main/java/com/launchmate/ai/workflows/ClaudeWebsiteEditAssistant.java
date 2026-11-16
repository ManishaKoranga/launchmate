package com.launchmate.ai.workflows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchmate.ai.prompts.WebsitePrompts;
import com.launchmate.ai.provider.ChatTurn;
import com.launchmate.ai.provider.LlmClient;
import com.launchmate.ai.provider.ReasoningEffort;
import com.launchmate.ai.provider.StructuredRequest;
import com.launchmate.business.domain.Business;
import com.launchmate.website.domain.SiteConfig;
import com.launchmate.website.service.WebsiteEditAssistant;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
class ClaudeWebsiteEditAssistant implements WebsiteEditAssistant {

    private final LlmClient llm;
    private final ObjectMapper json;

    ClaudeWebsiteEditAssistant(LlmClient llm, ObjectMapper json) {
        this.llm = llm;
        this.json = json;
    }

    @Override
    public SiteConfig propose(Business business, SiteConfig current, String instruction, List<String> correction,
                              SiteConfig previousAttempt) {
        List<ChatTurn> turns = new ArrayList<>();
        turns.add(ChatTurn.user("""
                Business: %s (%s)

                <current_website>
                %s
                </current_website>

                <owner_instruction>
                %s
                </owner_instruction>
                """.formatted(business.getName(), business.getCategory(), toJson(current), instruction)));
        if (previousAttempt != null) {
            turns.add(ChatTurn.assistant(toJson(previousAttempt)));
            turns.add(ChatTurn.user("That configuration failed validation:\n- " + String.join("\n- ", correction)
                    + "\nReturn the complete corrected configuration."));
        }
        return llm.structured(new StructuredRequest<>(WebsitePrompts.EDIT_SYSTEM, turns, SiteConfig.class,
                ReasoningEffort.LOW, 16000));
    }

    private String toJson(Object o) {
        try {
            return json.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
