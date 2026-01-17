package io.falconFlow.DSL.activity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jknack.handlebars.Handlebars;
import io.falconFlow.DSL.Helpers.BeanFetcher;
import io.falconFlow.DSL.Helpers.Global;
import io.falconFlow.DSL.model.*;
import io.falconFlow.DSL.workflow.model.StateModel;
import io.falconFlow.interfaces.FParam;
import io.falconFlow.interfaces.IFunction;
import io.falconFlow.model.ai.AgentResponse;
import io.falconFlow.services.ai.AIService;
import io.falconFlow.services.isolateservices.StateManagerService;
import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.activity.ActivityInfo;
import io.temporal.spring.boot.ActivityImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ActivityImpl()
public class AIActivityImpl implements AIActivity {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private AIService aiService;
    @Autowired
    private io.falconFlow.services.secret.SecretManager secretManager;
    @Autowired
    Handlebars handlebars;
    @Autowired
    private BeanFetcher serviceFetcher;

    @Override
    public FunctionResponse callAI(FRequest atRequestParser) {
        try {
            ActivityExecutionContext ctx = Activity.getExecutionContext();
            ActivityInfo info = ctx.getInfo();
            atRequestParser.setWorkflowActivityId(info.getActivityId());

            StateManagerService stateManagerService = new StateManagerService(atRequestParser.getState());
            Map<String, Object> meta = (Map<String, Object>) atRequestParser.getMetaData();
            String outputkey = meta.get("outputstatekey") != null ? meta.get("outputstatekey").toString() : "";
            // JSON string of tools
            String aiprompt =  meta.get("prompt") != null ? meta.get("prompt").toString() : "";
            String systemprompt = meta.get("systemprompt") != null ? meta.get("systemprompt").toString() : "";
            String aiagent = meta.get("model") != null ? meta.get("model").toString() : "";

            HashMap<String, Object> map = new HashMap<>();
            map.put("state", stateManagerService.getStateDecrypted().getStateValue());
            map.put("input", atRequestParser.getInput());
            map.put("userInput", atRequestParser.getUserInput());
            String initialPrompt = handlebars.compileInline(aiprompt).apply(map);

            if (aiagent.isEmpty()) {
                throw Activity.wrap(new Exception("Ai agent should not be blank"));
            }

            if (aiprompt.isEmpty()) {
                throw Activity.wrap(new Exception("Ai agent prompt should not be blank"));
            }

            // --- ReAct Loop Setup ---
            // Construct the system/initial message with tool definitions


            String currentPrompt = "You are an AI agent with access to external tools.\n " +
                    atRequestParser.getAiToolDef() + "\n\n " +
                    "To use a tool, output a JSON object ONLY. \n " +
                    "Supported formats:\n " +
                    "{\"tool_calls\": [{\"tool_name\": \"toolName\", \"arguments\": { ... }}]}\n " +
                    "Rules for tool usage:\n " +
                    "1. Only call each tool ONCE per user request.\n " +
                    "2. Before calling a tool, check if it has already been invoked in this turn.\n " +
                    "   If yes, reuse the result or skip the call.\n " +
                    "3. If multiple tools are needed, use parallel execution instead of sequential duplicate calls.\n " +
                    "4. Never call the same tool twice for the same query unless the user explicitly asks for more results.\n " +
                    "5. If the request is vague, ask the user for clarification instead of calling multiple tools.\n "+
                    systemprompt +
                    " \n " +
                    "Goal: " + initialPrompt + "\n ";
            // String finalAnswer = null;

            InputMap im = atRequestParser.getPluginProps();
            if(im.getBool("isAsync", false)){
                ctx.doNotCompleteOnReturn();
                atRequestParser.setAsyncToken(ctx.getTaskToken());
                //cacheService.setCache(info.getWorkflowId(), ctx.getTaskToken(), 3000);
            }

            im.put("prompt", currentPrompt);

            System.out.println("currentPrompt >>>> "+ currentPrompt);

            atRequestParser.setWorkflowActivityId(info.getActivityId());

            Object func  = serviceFetcher.getFunctionByName(Global.getBeanName(atRequestParser.getCall()));
            IFunction function =
                    (IFunction)func;

            FunctionResponse atResponseParser  =  function.invoke(atRequestParser);
            if(atRequestParser.getPluginProps().containsKey("resource")){
                // call dynamic resource method
                String resourceMethodName = atRequestParser.getPluginProps().getStr("resource");
                if(resourceMethodName != null && !resourceMethodName.isEmpty()) {
                    atResponseParser =  invokeResourceMethod(func, resourceMethodName, atRequestParser);
                }
            }

            if (!Objects.requireNonNull(atResponseParser).getStatus().equals(FunctionStatus.SUCCESS)) {
                throw Activity.wrap(
                        new Exception(atResponseParser.getErrorCode() + " - " + atResponseParser.getMessage()));
            }

            return atResponseParser;


        }
         catch (Exception e) {
            throw Activity.wrap(e);
        }

    }


//    public FunctionResponse callAI_old(FRequest req) {
//        try {
//            ActivityExecutionContext ctx = Activity.getExecutionContext();
//            ActivityInfo info = ctx.getInfo();
//            req.setWorkflowActivityId(info.getActivityId());
//
//            StateManagerService stateManagerService = new StateManagerService(req.getState());
//            Map<String, Object> meta = (Map<String, Object>) req.getMetaData();
//            String outputkey = meta.get("outputstatekey") != null ? meta.get("outputstatekey").toString() : "";
//            // JSON string of tools
//            String aitools = meta.get("aitools") != null ? meta.get("aitools").toString() : "[]";
//            String aiprompt = meta.get("aipromt") != null ? meta.get("aipromt").toString() : "";
//            String aiagent = meta.get("model") != null ? meta.get("model").toString() : "";
//
//            HashMap<String, Object> map = new HashMap<>();
//            map.put("state", stateManagerService.getStateDecrypted().getStateValue());
//            map.put("input", req.getInput());
//            map.put("userInput", req.getUserInput());
//            String initialPrompt = handlebars.compileInline(aiprompt).apply(map);
//
//            if (aiagent.isEmpty()) {
//                throw Activity.wrap(new Exception("Ai agent should not be blank"));
//            }
//
//            if (aiprompt.isEmpty()) {
//                throw Activity.wrap(new Exception("Ai agent prompt should not be blank"));
//            }
//
//            // --- ReAct Loop Setup ---
//            // Construct the system/initial message with tool definitions
//            StringBuilder conversation = new StringBuilder();
//            conversation.append("You are an AI assistant. You have access to the following tools:\n");
//            conversation.append(aitools).append("\n\n");
//            conversation.append("To use a tool, output a JSON object ONLY. \n");
//            conversation.append("Supported formats:\n");
//            conversation.append("1. {\"tool\": \"toolName\", \"args\": { ... }}\n");
//            conversation.append("2. {\"tool_calls\": [{\"tool_name\": \"toolName\", \"arguments\": { ... }}]}\n");
//            conversation.append("If you have the final answer, output the answer directly without JSON.\n");
//            conversation.append("Goal: ").append(initialPrompt).append("\n");
//
//
//            String currentPrompt = conversation.toString();
//            String finalAnswer = null;
//            int maxSteps = 10; // Prevent infinite loops
//
//            for (int i = 0; i < maxSteps; i++) {
//                AgentResponse ar = aiService.sendMessage(aiagent, currentPrompt);
//                String responseText = ar.getMessage();
//                System.out.println("AI Response (Step " + i + "): " + responseText);
//
//                // Check for tool calls
//                java.util.List<ToolCall> toolCalls = parseToolCalls(responseText);
//
//                if (toolCalls != null && !toolCalls.isEmpty()) {
//                    System.out.println("Tool Calls Detected: " + toolCalls.size());
//                    StringBuilder stepResult = new StringBuilder();
//
//                    for (ToolCall tc : toolCalls) {
//                        try {
//                             System.out.println("Executing: " + tc.tool);
//                            // Execute Tool
//                            Object result = executeTool(tc.tool, tc.args, req);
//                            String toolResultStr = "Tool '" + tc.tool + "' output: " + mapper.writeValueAsString(result);
//                            stepResult.append(toolResultStr).append("\n");
//                        } catch (Exception e) {
//                            String errorStr = "Tool '" + tc.tool + "' failed: " + e.getMessage();
//                            stepResult.append(errorStr).append("\n");
//                        }
//                    }
//                     // Feed result back to AI
//                     currentPrompt = "Previous step results:\n" + stepResult.toString() + "Continue.";
//
//                } else {
//                    // No tool call -> Final Answer attempt
//                    // But we must be careful not to mistake a failed parse for a final answer if it looks like JSON but wasn't parsed?
//                    // For now, assume if no tool call parsed, it's text.
//                    finalAnswer = responseText;
//                    break;
//                }
//            }
//
//            if (finalAnswer == null) {
//                finalAnswer = "Failed to reach a conclusion after " + maxSteps + " steps.";
//            }
//
//            stateManagerService.set(outputkey, finalAnswer);
//            FunctionResponse fr = new FunctionResponse();
//            fr.setState(stateManagerService.get());
//            fr.setStatus(FunctionStatus.SUCCESS);
//            fr.setMessage(finalAnswer + " >>  Prompt was:" + initialPrompt);
//            return fr;
//
//        } catch (Exception e) {
//            FunctionResponse fr = new FunctionResponse();
//            fr.setStatus(FunctionStatus.FAILED);
//            fr.setErrorCode("AI_ACTIVITY_ERROR");
//            fr.setMessage(e.getMessage());
//            throw Activity.wrap(e);
//        }
//    }

    // Helper class for parsed tool calls
    public static class ToolCall {
        String tool;
        Map<String, Object> args;

        public String getTool() {
            return tool;
        }

        public void setTool(String tool) {
            this.tool = tool;
        }

        public Map<String, Object> getArgs() {
            return args;
        }

        public void setArgs(Map<String, Object> args) {
            this.args = args;
        }
    }


    public static List<ToolCall> parseToolCalls(String text) {
        List<ToolCall> results = new ArrayList<>();

        try {
            String jsonToParse = extractJson(text);

            // 🔹 Handle map-style tool response
            if (isMapStyleToolCall(jsonToParse)) {
                jsonToParse = convertMapStyleToJson(jsonToParse);
            }

            JsonNode node;
            try {
                node = mapper.readTree(jsonToParse);
            } catch (Exception e) {
                return results;
            }

            if (node == null) return results;

            // 0️⃣ Check for nested response.tool_calls format
            if (node.has("response") && node.get("response").isObject()) {
                JsonNode responseNode = node.get("response");
                if (responseNode.has("tool_calls") && responseNode.get("tool_calls").isArray()) {
                    for (JsonNode callNode : responseNode.get("tool_calls")) {
                        if (callNode.has("tool_name") && callNode.has("arguments")) {
                            ToolCall tc = new ToolCall();
                            tc.tool = callNode.get("tool_name").asText();
                            tc.args = mapper.convertValue(callNode.get("arguments"), Map.class);
                            results.add(tc);
                        }
                    }
                    return results;
                }
            }

            // 1️⃣ Single tool format
            if (node.has("tool") && node.has("args")) {
                ToolCall tc = new ToolCall();
                tc.tool = node.get("tool").asText();
                tc.args = mapper.convertValue(node.get("args"), Map.class);
                results.add(tc);
                return results;
            }

            // 2️⃣ OpenAI / Groq tool_calls format (root level)
            if (node.has("tool_calls") && node.get("tool_calls").isArray()) {
                for (JsonNode callNode : node.get("tool_calls")) {
                    if (callNode.has("tool_name") && callNode.has("arguments")) {
                        ToolCall tc = new ToolCall();
                        tc.tool = callNode.get("tool_name").asText();
                        tc.args = mapper.convertValue(callNode.get("arguments"), Map.class);
                        results.add(tc);
                    }
                }
                return results;
            }

            // 3️⃣ Array fallback
            if (node.isArray()) {
                for (JsonNode item : node) {
                    if (item.has("tool") && item.has("args")) {
                        ToolCall tc = new ToolCall();
                        tc.tool = item.get("tool").asText();
                        tc.args = mapper.convertValue(item.get("args"), Map.class);
                        results.add(tc);
                    }
                }
            }

        } catch (Exception ignored) {}

        return results;
    }


    private static String extractJson(String text) {
        // ```json block
        Pattern codeBlock = Pattern.compile("```json\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL);
        Matcher codeMatcher = codeBlock.matcher(text);
        if (codeMatcher.find()) {
            return codeMatcher.group(1);
        }

        // any { ... }
        Pattern brace = Pattern.compile("(?s)\\{.*\\}");
        Matcher braceMatcher = brace.matcher(text);
        if (braceMatcher.find()) {
            return braceMatcher.group();
        }

        return text;
    }
    private static boolean isMapStyleToolCall(String text) {
        return (text.contains("tool=") && text.contains("args=")) || 
               (text.contains("tool_calls=") && text.contains("tool_name="));
    }

    private static String convertMapStyleToJson(String input) {
        try {
            // Check if this is a tool_calls array format
            if (input.contains("tool_calls=")) {
                return convertMapStyleArrayToJson(input);
            }
            
            // Original single tool format: {tool=TelegramMessagePlugin:sendTextMessage, args={text=You are 36 years old.}}
            Pattern toolPattern = Pattern.compile("tool=([^,}]+)");
            Pattern argsPattern = Pattern.compile("args=\\{(.*)\\}");

            Matcher toolMatcher = toolPattern.matcher(input);
            Matcher argsMatcher = argsPattern.matcher(input);

            if (!toolMatcher.find()) return input;

            String tool = toolMatcher.group(1).trim();
            Map<String, String> args = new HashMap<>();

            if (argsMatcher.find()) {
                String argsBody = argsMatcher.group(1);

                // split key=value
                String[] pairs = argsBody.split(",");
                for (String pair : pairs) {
                    String[] kv = pair.split("=", 2);
                    if (kv.length == 2) {
                        args.put(kv[0].trim(), kv[1].trim());
                    }
                }
            }

            ObjectNode root = mapper.createObjectNode();
            root.put("tool", tool);
            root.set("args", mapper.valueToTree(args));

            return root.toString();
        } catch (Exception e) {
            return input;
        }
    }

    /**
     * Converts map-style array format to JSON
     * Input: {tool_calls=[{tool_name=Slack:sendMessageToChannel, arguments={text=...}}, {tool_name=Telegram...}]}
     * Output: {"tool_calls": [{"tool_name": "Slack:sendMessageToChannel", "arguments": {"text": "..."}}, ...]}
     */
    private static String convertMapStyleArrayToJson(String input) {
        try {
            // Extract the array content between tool_calls=[ and the final ]
            Pattern arrayPattern = Pattern.compile("tool_calls=\\[(.+)\\]");
            Matcher arrayMatcher = arrayPattern.matcher(input);
            
            if (!arrayMatcher.find()) {
                return input;
            }
            
            String arrayContent = arrayMatcher.group(1);
            List<Map<String, Object>> toolCallsList = new ArrayList<>();
            
            // Split by }, { to get individual tool calls
            // We need to be careful with nested braces
            int braceDepth = 0;
            int start = 0;
            
            for (int i = 0; i < arrayContent.length(); i++) {
                char c = arrayContent.charAt(i);
                if (c == '{') {
                    braceDepth++;
                } else if (c == '}') {
                    braceDepth--;
                    if (braceDepth == 0) {
                        // Found a complete tool call
                        String toolCallStr = arrayContent.substring(start, i + 1);
                        Map<String, Object> toolCall = parseMapStyleToolCall(toolCallStr);
                        if (toolCall != null) {
                            toolCallsList.add(toolCall);
                        }
                        // Skip the comma and space
                        start = i + 2;
                        if (start < arrayContent.length() && arrayContent.charAt(start) == ' ') {
                            start++;
                        }
                    }
                }
            }
            
            // Build JSON structure
            ObjectNode root = mapper.createObjectNode();
            root.set("tool_calls", mapper.valueToTree(toolCallsList));
            
            return root.toString();
        } catch (Exception e) {
            return input;
        }
    }

    /**
     * Parse a single map-style tool call
     * Input: {tool_name=Slack:sendMessageToChannel, arguments={text=Hello}}
     * Output: Map with tool_name and arguments
     */
    private static Map<String, Object> parseMapStyleToolCall(String toolCallStr) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Extract tool_name
            Pattern toolNamePattern = Pattern.compile("tool_name=([^,}]+)");
            Matcher toolNameMatcher = toolNamePattern.matcher(toolCallStr);
            if (toolNameMatcher.find()) {
                result.put("tool_name", toolNameMatcher.group(1).trim());
            }
            
            // Extract arguments
            Pattern argsPattern = Pattern.compile("arguments=\\{([^}]+)\\}");
            Matcher argsMatcher = argsPattern.matcher(toolCallStr);
            if (argsMatcher.find()) {
                String argsBody = argsMatcher.group(1);
                Map<String, String> args = new HashMap<>();
                
                // Parse key=value pairs
                String[] pairs = argsBody.split(",(?![^{]*})"); // Split by comma not inside braces
                for (String pair : pairs) {
                    String[] kv = pair.split("=", 2);
                    if (kv.length == 2) {
                        args.put(kv[0].trim(), kv[1].trim());
                    }
                }
                result.put("arguments", args);
            }
            
            return result.isEmpty() ? null : result;
        } catch (Exception e) {
            return null;
        }
    }



//    public static java.util.List<ToolCall> parseToolCalls(String text) {
//        java.util.List<ToolCall> results = new ArrayList<>();
//        try {
//            JsonNode node = null;
//            // Find JSON block: Look for { ... }
//            Pattern pattern = Pattern.compile("(?s)\\{.*\\}");
//            Matcher matcher = pattern.matcher(text);
//
//            // Try extracting valid JSON
//            String jsonToParse = text;
//            if (matcher.find()) {
//                 jsonToParse = matcher.group();
//            }
//             // Fallback for json embedded in text like ```json ... ```
//             Pattern codeBlock = Pattern.compile("```json\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL);
//             Matcher codeMatcher = codeBlock.matcher(text);
//             if(codeMatcher.find()){
//                  jsonToParse = codeMatcher.group(1);
//             }
//
//            try {
//                node = mapper.readTree(jsonToParse);
//            } catch (Exception e) {
//                // parsing failed, maybe not JSON
//                return results;
//            }
//
//            if (node == null) return results;
//
//            // 1. Single tool format: {"tool": "...", "args": {...}}
//             if (node.has("tool") && node.has("args")) {
//                 ToolCall tc = new ToolCall();
//                 tc.tool = node.get("tool").asText();
//                 tc.args = mapper.convertValue(node.get("args"), Map.class);
//                 results.add(tc);
//                 return results;
//             }
//
//             // 2. Tool calls list format (OpenAI/Standard): {"tool_calls": [{"tool_name": "...", "arguments": {...}}]}
//             if (node.has("tool_calls") && node.get("tool_calls").isArray()) {
//                 for (JsonNode callNode : node.get("tool_calls")) {
//                     if (callNode.has("tool_name") && callNode.has("arguments")) {
//                         ToolCall tc = new ToolCall();
//                         tc.tool = callNode.get("tool_name").asText();
//                         tc.args = mapper.convertValue(callNode.get("arguments"), Map.class);
//                         results.add(tc);
//                     }
//                 }
//                 return results;
//             }
//
//             // 3. Simple list format just in case: [{"tool": ...}]
//             if (node.isArray()) {
//                  for (JsonNode item : node) {
//                       if (item.has("tool") && item.has("args")) {
//                         ToolCall tc = new ToolCall();
//                         tc.tool = item.get("tool").asText();
//                         tc.args = mapper.convertValue(item.get("args"), Map.class);
//                         results.add(tc);
//                       }
//                  }
//             }
//
//        } catch (Exception e) {
//            // Not a valid tool call or JSON parsing failed
//        }
//        return results;
//    }
    /**
     * Dynamically invokes a resource method on the given bean object.
     * The method parameters annotated with @MCPParam will be populated from pluginProps.
     *
     * @param bean The bean object containing the resource method
     * @param methodName The name of the method to invoke
     * @param request The FRequest containing pluginProps with parameter values
     * @return FunctionResponse from the invoked method
     * @throws Exception if method not found or invocation fails
     */
    private FunctionResponse invokeResourceMethod(Object bean, String methodName, FRequest request) throws Exception {
        Class<?> beanClass = bean.getClass();
        Method targetMethod = null;

        // Find the method by name
        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                targetMethod = method;
                break;
            }
        }

        if (targetMethod == null) {
            throw new NoSuchMethodException("Method '" + methodName + "' not found in bean: " + beanClass.getSimpleName());
        }

        // Build parameter array based on @MCPParam annotations
        Parameter[] parameters = targetMethod.getParameters();
        ArrayList<Object> args = new ArrayList<>();

        for (Parameter param : parameters) {
            // Check if parameter is FRequest type (first parameter)
            if (param.getType().equals(FRequest.class)) {
                args.add(request);
            } else {
                // Check for @FParam annotation
                FParam FParam = param.getAnnotation(FParam.class);
                if (FParam != null) {
                    String paramName = FParam.value();
                    Object value = request.getPluginProps().get(paramName);

                    // Validate required parameters
                    if (FParam.required() && (value == null || value.toString().isEmpty())) {
                        throw new IllegalArgumentException(
                            "Required parameter '" + paramName + "' is missing or empty for method '" + methodName + "'"
                        );
                    }

                    // Convert value to the expected parameter type
                    Object convertedValue = convertToType(value, param.getType());
                    args.add(convertedValue);
                } else {
                    // If no annotation, try to get by parameter name (optional)
                    Object value = request.getPluginProps().get(param.getName());
                    Object convertedValue = convertToType(value, param.getType());
                    args.add(convertedValue);
                }
            }
        }

        // Invoke the method
        Object result = targetMethod.invoke(bean, args.toArray());

        // Return the result as FunctionResponse
        if (result instanceof FunctionResponse) {
            return (FunctionResponse) result;
        } else {
            throw new IllegalStateException("Resource method must return FunctionResponse, but returned: " +
                    (result != null ? result.getClass().getName() : "null"));
        }
    }

    /**
     * Converts a value to the specified target type.
     * Supports String, Integer, Long, Boolean, Double, Float types.
     *
     * @param value The value to convert
     * @param targetType The target type
     * @return The converted value
     */
    private Object convertToType(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        // If already the correct type, return as-is
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        // Convert to target type
        String strValue = value.toString();

        if (targetType == String.class) {
            return strValue;
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(strValue);
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(strValue);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(strValue);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(strValue);
        } else if (targetType == Float.class || targetType == float.class) {
            return Float.parseFloat(strValue);
        } else {
            // Return as-is for other types
            return value;
        }
    }


}
