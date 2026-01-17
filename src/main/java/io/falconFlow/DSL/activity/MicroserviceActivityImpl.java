package io.falconFlow.DSL.activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.falconFlow.DSL.model.FRequest;
import io.falconFlow.DSL.model.FunctionResponse;
import io.falconFlow.DSL.model.FunctionStatus;
import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.activity.ActivityInfo;
import io.temporal.spring.boot.ActivityImpl;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@ActivityImpl()
public class MicroserviceActivityImpl implements MicroserviceActivity {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public FunctionResponse callMicroservice(FRequest atRequestParser) {
    try {
      ActivityExecutionContext ctx = Activity.getExecutionContext();
      ActivityInfo info = ctx.getInfo();
      Map<String, Object> input = (Map<String, Object>) atRequestParser.getInput();
      String urlStr = (String) input.get("url");
      String method = (String) input.get("method");
      atRequestParser.setWorkflowActivityId(info.getActivityId());

      /* HTTP CALL */
      URL url = new URL(urlStr);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod(method);
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setDoOutput(true);

      String jsonBody = objectMapper.writeValueAsString(atRequestParser);
      System.out.println("=====>>>>>" + jsonBody);
      // Send request
      try (OutputStream os = conn.getOutputStream()) {
        os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
      }

      int responseCode = conn.getResponseCode();
      System.out.println(
          "=============================================================> Response Code > "
              + responseCode);

      if (responseCode != 200) {
        throw Activity.wrap(new Exception("Failed from API"));
      }
      FunctionResponse activityResponseParser;
      try (BufferedReader br =
          new BufferedReader(
              new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
        activityResponseParser = objectMapper.readValue(br, FunctionResponse.class);
        if (!activityResponseParser.getStatus().equals(FunctionStatus.SUCCESS)) {
          Activity.wrap(new Exception("Activity failed to perform"));
        }
      }

      System.out.println("=============================================================");
      return activityResponseParser;

    } catch (Exception e) {
      throw Activity.wrap(e);
    }
  }
}
