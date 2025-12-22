package io.falconFlow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.falconFlow.dto.APIDto;
import io.falconFlow.dto.CompilerResponse;
import io.falconFlow.entity.FunctionsEntity;
import io.falconFlow.helpers.Checksum;
import io.falconFlow.services.genservice.FunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Controller
@RequestMapping("/api")
public class APIController {

  ObjectMapper mapper = new ObjectMapper();

  @Autowired FunctionService functionDbService;

  @PostMapping(
      value = "/saveAPI",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<CompilerResponse> saveAPI(@RequestBody APIDto api) {
    // generate CREATE or ALTER

    FunctionsEntity ett = new FunctionsEntity();
    Optional<FunctionsEntity> ett1 = functionDbService.getAllFunctions(api.getId());

    CompilerResponse c = new CompilerResponse();

    try {
      String apiSource = mapper.writeValueAsString(api);
      if (ett1.isPresent()) {
        ett = ett1.get();
      }

      ett.setClassCode(new byte[] {});
      ett.setCompiledTime(LocalDateTime.now(ZoneId.systemDefault()));
      ett.setCreatedTime(LocalDateTime.now(ZoneId.systemDefault()));
      String className9 = api.getName();
      ett.setClassName(className9);
      ett.setFqcn("falconFlow.api." + api.getName());
      ett.setRawProcessClass(apiSource);
      ett.setChecksum(
          Checksum.checksum(
              apiSource.getBytes(StandardCharsets.UTF_8), Checksum.Algorithm.SHA_256));
      ett.setClassType(api.getClassType());
      ett.setRawClass(apiSource);

      ett = functionDbService.saveFunction(ett);
      c.setMessage("done");
      c.setId(ett.getId());
      c.setStatus("Success");

    } catch (Exception ex) {
      c.setMessage(ex.getMessage());
      c.setStatus("Failed");
    }

    return new ResponseEntity<>(c, HttpStatus.OK);
  }

  @GetMapping(
      value = "/getApi",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<FunctionsEntity> getObjects(@Param("id") Integer id) {
    Optional<FunctionsEntity> ett = functionDbService.getAllFunctions(id);
    return new ResponseEntity<>(ett.orElse(null), HttpStatus.OK);
  }
}
