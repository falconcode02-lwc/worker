AI Agent module

This module adds a small, modular AI agent system to the project.

Files added:
- `AgentController` - REST endpoints under `/api/ai`
- `AIService` / `AIServiceImpl` - service that manages agents, memory and tools
- `InMemoryMemory` - simple in-memory conversation history
- `N8nTool` - simple adapter to call an n8n webhook
- `AIConfig` - provides a RestTemplate bean

How to use (quick):
1. Create an agent by POST /api/ai/agents with JSON: {"agentId":"a1","model":"gpt-sim","config":{"n8n.webhookUrl":"https://..."}}
2. Send messages: POST /api/ai/agents/a1/message with {"message":"hello"}
3. Inspect memory: GET /api/ai/agents/a1/memory
4. Trigger n8n: POST /api/ai/agents/a1/tools/n8n with any JSON payload

Notes:
- This is a minimal, pluggable skeleton. Integrate real LLM provider adapters by replacing the simulated response in `AIServiceImpl.sendMessage()` with provider-specific clients (OpenAI, Azure, Anthropic, etc.).
- Tools can be registered per-agent; currently only n8n webhook tool is supported.
