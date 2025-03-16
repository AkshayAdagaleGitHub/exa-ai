package com.mcp.mcp.server;

import com.mcp.mcp.server.service.WeatherService;
import java.util.List;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpServerApplication {


	private static final Logger logger = LoggerFactory.getLogger(McpServerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(McpServerApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider weatherTools(WeatherService weatherService) {
		return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
	}

	@Bean
	public ToolCallbackProvider myToolCallbacks() {
//		List<ToolCallback> tools = ;
//		return ToolCallbackProvider.from(tools);
		return null;
	}

	@Bean
	public List<io.modelcontextprotocol.server.McpServerFeatures.SyncPromptRegistration> myPrompts() {
		var prompt = new McpSchema.Prompt("greeting", "A friendly greeting prompt",
				List.of(new McpSchema.PromptArgument("name", "The name to greet", true)));

		var promptRegistration = new io.modelcontextprotocol.server.McpServerFeatures.SyncPromptRegistration(prompt, getPromptRequest -> {
			String nameArgument = (String) getPromptRequest.arguments().get("name");
			if (nameArgument == null) { nameArgument = "friend"; }
			var userMessage = new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent("Hello " + nameArgument + "! How can I assist you today?"));
			return new McpSchema.GetPromptResult("A personalized greeting message", List.of(userMessage));
		});

		return List.of(promptRegistration);
	}
}
