package com.fleetai.agents.mcp;

/**
 * Placeholder for Model Context Protocol (MCP) tool registration.
 *
 * In a full deployment, the Approval Agent and Conversation Agent are the two
 * consumers most likely to need MCP tools — e.g. an MCP server exposing the
 * client's policy engine, ticketing system, or fleet-ops database as callable
 * tools, so agents can look up live data instead of relying solely on prompt
 * context or stale RAG documents.
 *
 * With spring-ai-mcp-client-spring-boot-starter on the classpath, register
 * servers via spring.ai.mcp.client.stdio / .sse connections in application.yml,
 * then inject the resulting ToolCallbackProvider into ChatClient.builder()
 * .defaultTools(...) inside ApprovalAgent / ConversationAgent. No MCP servers
 * are wired by default here since they are client-environment-specific.
 */
class McpToolConfig {
}
