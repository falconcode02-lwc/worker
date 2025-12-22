package io.falconFlow.controller;

import io.falconFlow.core.DynamicBeanRegistrar;
import io.falconFlow.core.models.ClassDefination;
import io.falconFlow.dto.CompilerResponse;
import io.falconFlow.dto.db.TableDto;
import io.falconFlow.entity.FunctionsEntity;
import io.falconFlow.helpers.Checksum;
import io.falconFlow.services.falconparser.DBModelGenerator;
import io.falconFlow.services.falconparser.SqlGeneratorService;
import io.falconFlow.services.genservice.FunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/api/schema")
public class DBController {

  private final SqlGeneratorService sqlGeneratorService;
  private final JdbcTemplate jdbcTemplate;
  @Autowired FunctionService functionDbService;

  @Autowired GenericApplicationContext ctx;

  public DBController(SqlGeneratorService sqlGeneratorService, JdbcTemplate jdbcTemplate) {
    this.sqlGeneratorService = sqlGeneratorService;
    this.jdbcTemplate = jdbcTemplate;
  }

  @PostMapping(
      value = "/generate",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<CompilerResponse> generateSql(@RequestBody TableDto table) {
    // generate CREATE or ALTER

    FunctionsEntity ett = new FunctionsEntity();
    Optional<FunctionsEntity> ett1 = functionDbService.getAllFunctions(table.getId());

    CompilerResponse c = new CompilerResponse();
    String sql = "";
    try {
      sql = sqlGeneratorService.generateSql(table);
      // execute in DB
      if (!sql.startsWith("--")) { // skip "no changes" case
        jdbcTemplate.execute(sql);
      }

      String classF = DBModelGenerator.getClassFromJson(table);
      Map<String, ClassDefination> sources = new HashMap<>();
      ClassDefination d = new ClassDefination();
      d.setClassName(table.getName());
      d.setClassType(table.getClassType());
      d.setSourceCode(classF);
      // System.out.println("RegisterClasses | found --> " + "| " + classType + " |" + className);
      // System.err.println(code);
      sources.put(table.getName(), d);
      DynamicBeanRegistrar.loadAllClasses(sources, ctx);

      if (ett1.isPresent()) {
        ett = ett1.get();
      }

      ett.setClassCode(new byte[] {});
      ett.setCompiledTime(LocalDateTime.now(ZoneId.systemDefault()));
      ett.setCreatedTime(LocalDateTime.now(ZoneId.systemDefault()));
      String className9 = table.getName();
      ett.setClassName(className9);
      ett.setFqcn("falconFlow.table." + table.getName());
      ett.setRawProcessClass(classF);
      ett.setChecksum(
          Checksum.checksum(classF.getBytes(StandardCharsets.UTF_8), Checksum.Algorithm.SHA_256));
      ett.setClassType(table.getClassType());
      ett.setRawClass(classF);

      functionDbService.saveFunction(ett);

      c.setMessage("done");
      c.setStatus("Success");
      c.setId(ett.getId());

    } catch (Exception ex) {
      c.setMessage(ex.getMessage());
      c.setStatus("Failed");
    }

    return new ResponseEntity<>(c, HttpStatus.OK);
  }

  @GetMapping(
      value = "/getColumns",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<TableDto> getColumns(@Param("tableName") String tableName) {
    // generate CREATE or ALTER
    TableDto dto = sqlGeneratorService.getColumns(tableName);

    return new ResponseEntity<>(dto, HttpStatus.OK);
  }

  @GetMapping(
      value = "/getObjects",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<List<String>> getObjects() {
    // generate CREATE or ALTER
    List<String> dto = sqlGeneratorService.getObjects();
    return new ResponseEntity<>(dto, HttpStatus.OK);
  }
}
