package io.falconFlow.DSL.activity;

import io.falconFlow.DSL.Helpers.BeanFetcher;
import io.falconFlow.DSL.Helpers.Global;
import io.falconFlow.DSL.model.*;
import io.falconFlow.interfaces.IFunction;
import io.falconFlow.services.cache.ICacheService;
import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.activity.ActivityInfo;
import io.temporal.spring.boot.ActivityImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Objects;

import io.falconFlow.interfaces.FParam;

@Component
@ActivityImpl()
public class FunctionActivityImpl implements FunctionActivity {

  @Autowired private BeanFetcher serviceFetcher;
  @Autowired ICacheService cacheService;


  @Override
  public FunctionResponse callFunction(FRequest atRequestParser) {
    try {

      ActivityExecutionContext ctx = Activity.getExecutionContext();
      ActivityInfo info = ctx.getInfo();

       InputMap im = atRequestParser.getPluginProps();
      if(im.getBool("isAsync", false)){
          ctx.doNotCompleteOnReturn();
          atRequestParser.setAsyncToken(ctx.getTaskToken());
          //cacheService.setCache(info.getWorkflowId(), ctx.getTaskToken(), 3000);
      }

      atRequestParser.setWorkflowActivityId(info.getActivityId());

      Object func  = serviceFetcher.getFunctionByName(Global.getBeanName(atRequestParser.getCall()));
      IFunction function =
          (IFunction)func;

      FunctionResponse atResponseParser  =  function.invoke(atRequestParser);
        if(atRequestParser.getPluginProps().containsKey("resource")){
                // call dynamic resource method
                String resourceMethodName = atRequestParser.getPluginProps().getStr("resource");
                if(resourceMethodName != null && !resourceMethodName.isEmpty()) {
                    atResponseParser = invokeResourceMethod(func, resourceMethodName, atRequestParser);
                }
        }

        if (!Objects.requireNonNull(atResponseParser).getStatus().equals(FunctionStatus.SUCCESS)) {
        throw Activity.wrap(
            new Exception(atResponseParser.getErrorCode() + " - " + atResponseParser.getMessage()));
      }

      return atResponseParser;

    } catch (Exception e) {
      throw Activity.wrap(e);
    }
  }

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
