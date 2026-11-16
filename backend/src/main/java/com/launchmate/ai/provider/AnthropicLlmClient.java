package com.launchmate.ai.provider;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.errors.NoCredentialsException;
import com.anthropic.errors.PermissionDeniedException;
import com.anthropic.errors.UnauthorizedException;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.StructuredOutputConfig;
import com.anthropic.models.messages.StructuredTextBlock;
import com.anthropic.models.messages.TextBlock;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.ToolResultBlockParam;
import com.anthropic.models.messages.ToolUseBlock;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchmate.shared.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Claude adapter built on the official {@code anthropic-java} SDK.
 * <ul>
 *   <li>Credentials: {@code ANTHROPIC_API_KEY} or an {@code ant auth login} profile (resolved by the SDK).</li>
 *   <li>Retries/backoff for 429/5xx/connection errors are handled by the SDK ({@code maxRetries}).</li>
 *   <li>SDK exceptions are translated to {@link ExternalServiceException}; raw provider errors never
 *       reach API clients.</li>
 * </ul>
 */
@Component
public class AnthropicLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(AnthropicLlmClient.class);
    private static final String SERVICE = "The AI service";

    private final AiProperties properties;
    private final ObjectMapper objectMapper;
    private final AnthropicClient client;

    public AnthropicLlmClient(AiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.client = properties.enabled() ? createClient(properties) : null;
    }

    private static AnthropicClient createClient(AiProperties properties) {
        try {
            return AnthropicOkHttpClient.builder()
                    .fromEnv()
                    .timeout(properties.timeout())
                    .maxRetries(properties.maxRetries())
                    .build();
        } catch (RuntimeException e) {
            log.warn("Anthropic client not configured: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean isConfigured() {
        return client != null;
    }

    @Override
    public <T> T structured(StructuredRequest<T> request) {
        AnthropicClient c = requireClient();
        StructuredMessageCreateParams.Builder<T> builder = MessageCreateParams.builder()
                .model(properties.model())
                .maxTokens(request.maxTokens())
                .system(request.system())
                .outputConfig(StructuredOutputConfig.<T>builder()
                        .format(request.outputType())
                        .effort(effort(request.effort()))
                        .build());
        for (ChatTurn turn : request.conversation()) {
            if (turn.role() == ChatTurn.Role.USER) {
                builder.addUserMessage(turn.text());
            } else {
                builder.addAssistantMessage(turn.text());
            }
        }
        StructuredMessage<T> response = call(() -> c.messages().create(builder.build()));
        StopReason stop = response.stopReason().orElse(StopReason.END_TURN);
        if (stop.equals(StopReason.REFUSAL)) {
            throw new ExternalServiceException("The AI could not help with this request.", false, null);
        }
        if (stop.equals(StopReason.MAX_TOKENS)) {
            throw ExternalServiceException.failed(SERVICE, new IllegalStateException("structured output truncated"));
        }
        return response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(StructuredTextBlock::text)
                .findFirst()
                .orElseThrow(() -> ExternalServiceException.failed(SERVICE,
                        new IllegalStateException("no structured content in response")));
    }

    @Override
    public ToolSession startToolSession(ToolSessionRequest request) {
        return new AnthropicToolSession(requireClient(), request);
    }

    /** Keeps the SDK message list; assistant turns are appended exactly as returned (thinking included). */
    private final class AnthropicToolSession implements ToolSession {

        private final AnthropicClient c;
        private final ToolSessionRequest request;
        private final List<Tool> tools;
        private final List<MessageParam> messages = new ArrayList<>();

        AnthropicToolSession(AnthropicClient c, ToolSessionRequest request) {
            this.c = c;
            this.request = request;
            this.tools = request.tools().stream().map(AnthropicLlmClient::toSdkTool).toList();
            for (ChatTurn turn : request.history()) {
                messages.add(turn.role() == ChatTurn.Role.USER
                        ? MessageParam.ofUser(turn.text())
                        : assistantText(turn.text()));
            }
            messages.add(MessageParam.ofUser(request.userMessage()));
        }

        @Override
        public ModelStep next() {
            MessageCreateParams.Builder builder = MessageCreateParams.builder()
                    .model(properties.model())
                    .maxTokens(request.maxTokens())
                    .system(request.system())
                    .outputConfig(OutputConfig.builder().effort(effort(request.effort())).build())
                    .messages(messages);
            tools.forEach(builder::addTool);
            Message response = call(() -> c.messages().create(builder.build()));
            messages.add(response.toParam());

            String text = response.content().stream()
                    .flatMap(b -> b.text().stream())
                    .map(TextBlock::text)
                    .collect(Collectors.joining("\n")).trim();
            List<ToolCall> calls = response.content().stream()
                    .flatMap(b -> b.toolUse().stream())
                    .map(this::toToolCall)
                    .toList();
            StopReason stop = response.stopReason().orElse(StopReason.END_TURN);
            ModelStep.Outcome outcome;
            if (stop.equals(StopReason.REFUSAL)) {
                outcome = ModelStep.Outcome.REFUSED;
            } else if (stop.equals(StopReason.MAX_TOKENS)) {
                outcome = ModelStep.Outcome.TRUNCATED;
            } else if (!calls.isEmpty()) {
                outcome = ModelStep.Outcome.NEEDS_TOOL_RESULTS;
            } else {
                outcome = ModelStep.Outcome.FINISHED;
            }
            return new ModelStep(text, calls, outcome);
        }

        @Override
        public void submitToolResults(List<ToolResult> results) {
            List<ContentBlockParam> blocks = results.stream()
                    .map(r -> ContentBlockParam.ofToolResult(ToolResultBlockParam.builder()
                            .toolUseId(r.toolCallId())
                            .content(r.content())
                            .isError(r.isError())
                            .build()))
                    .toList();
            messages.add(MessageParam.ofUserOfContentBlockParams(blocks));
        }

        private ToolCall toToolCall(ToolUseBlock block) {
            JsonNode args = block._input().convert(JsonNode.class);
            return new ToolCall(block.id(), block.name(), args == null ? objectMapper.createObjectNode() : args);
        }
    }

    private static MessageParam assistantText(String text) {
        return MessageParam.ofAssistant(com.anthropic.models.messages.AssistantMessageParam.builder()
                .content(text).build());
    }

    private static Tool toSdkTool(ToolDefinition def) {
        Map<String, Object> schema = def.inputSchema();
        Tool.InputSchema.Properties.Builder props = Tool.InputSchema.Properties.builder();
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.getOrDefault("properties", Map.of());
        properties.forEach((k, v) -> props.putAdditionalProperty(k, JsonValue.from(v)));
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.getOrDefault("required", List.of());
        return Tool.builder()
                .name(def.name())
                .description(def.description())
                .inputSchema(Tool.InputSchema.builder()
                        .properties(props.build())
                        .required(required)
                        .putAdditionalProperty("additionalProperties", JsonValue.from(false))
                        .build())
                .build();
    }

    private static OutputConfig.Effort effort(ReasoningEffort effort) {
        return switch (effort) {
            case LOW -> OutputConfig.Effort.LOW;
            case MEDIUM -> OutputConfig.Effort.MEDIUM;
            case HIGH -> OutputConfig.Effort.HIGH;
        };
    }

    private AnthropicClient requireClient() {
        if (client == null) {
            throw ExternalServiceException.notConfigured("AI");
        }
        return client;
    }

    private static <R> R call(java.util.function.Supplier<R> request) {
        try {
            return request.get();
        } catch (NoCredentialsException | UnauthorizedException | PermissionDeniedException e) {
            log.warn("Anthropic credentials missing or rejected: {}", e.getClass().getSimpleName());
            throw new ExternalServiceException("AI is not configured on this server", true, e);
        } catch (AnthropicServiceException e) {
            log.warn("Anthropic API error status={}", e.statusCode());
            throw ExternalServiceException.failed(SERVICE, e);
        } catch (AnthropicIoException e) {
            throw ExternalServiceException.failed(SERVICE, e);
        }
    }
}
