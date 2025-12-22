package io.falconFlow.controller;

import io.falconFlow.core.DynamicCompiler;
import io.falconFlow.core.models.ClassDefination;
import io.falconFlow.dto.ClassTypeProjection;
import io.falconFlow.dto.CompilerRequest;
import io.falconFlow.dto.CompilerResponse;
import io.falconFlow.dto.GetFileListProjection;
import io.falconFlow.entity.FunctionsEntity;
import io.falconFlow.entity.FunctionsHitoryEntity;
import io.falconFlow.helpers.Checksum;
import io.falconFlow.helpers.CompilationException;
import io.falconFlow.helpers.ObjectUtil;
import io.falconFlow.services.falconparser.ClassExtractor;
import io.falconFlow.services.falconparser.FalconFlowParser;
import io.falconFlow.services.genservice.ClassTypeService;
import io.falconFlow.services.genservice.FunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Controller()
public class CompileController {

  @Autowired ApplicationContext ctx;

  @Autowired FunctionService functionDbService;

  @Autowired ClassTypeService classTypeService;

  @PostMapping(
      value = "/compile",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<CompilerResponse> dynamic(@RequestBody CompilerRequest reqParser) {
    byte[] decodedBytes = Base64.getDecoder().decode(reqParser.getEncodedFile());
    CompilerResponse res = new CompilerResponse();
    String rawProcessClass = "";
    // ✅ Explicitly use UTF-8
    String rawClass = new String(decodedBytes, StandardCharsets.UTF_8);
    try {

      rawProcessClass = FalconFlowParser.preprocess(rawClass, reqParser.getClassType());
      // System.out.println(rawProcessClass);

      if (reqParser.getCompileType().equals("onlyCompile")) {
        res.setStatus("Failed");
        res.setMessage(rawProcessClass);
        return new ResponseEntity<>(res, HttpStatus.OK);
      }

      String className = ClassExtractor.getFirstClassName(rawProcessClass);

      DynamicCompiler compiler = new DynamicCompiler((GenericApplicationContext) ctx);
      Map<String, ClassDefination> sources = new HashMap<>();
      ClassDefination d = new ClassDefination();
      d.setSourceCode(rawProcessClass);
      d.setClassType(reqParser.getClassType());
      d.setClassName(className);
      sources.put(className, d);

      Map.Entry<String, ClassDefination> entry = compiler.compileAndRegister(sources);

      // for (Map.Entry<String, ClassDefination> entry : compiled.ge) {
      String fqcn = entry.getKey();
      String checkSum = Checksum.checksum(decodedBytes, Checksum.Algorithm.SHA_256);
      FunctionsEntity ett = new FunctionsEntity();
      Optional<FunctionsEntity> ett1 = functionDbService.getAllFunctions(reqParser.getId());
      if (ett1.isPresent()) {
        ett = ett1.get();
        if (checkSum.equals(ett.getChecksum())) {
          res.setStatus("Failed");
          res.setId(-1);
          res.setMessage("No change in file");
          return new ResponseEntity<>(res, HttpStatus.OK);
        } else if (!d.getClassName().equals(ett.getClassName())) {
          res.setStatus("Failed");
          res.setId(0);
          res.setMessage("Not allowed to change the file name!");
          return new ResponseEntity<>(res, HttpStatus.OK);

        } else if (Integer.parseInt(reqParser.getVersion()) < ett.getVersion()) {
          res.setStatus("Failed");
          res.setId(0);
          res.setMessage("Your file version is older than present! Please get new one.");
          return new ResponseEntity<>(res, HttpStatus.OK);
        }
        //        else if () {
        //            res.setStatus("Failed");
        //            res.setId(0);
        //            res.setMessage("Not allowed to change the file name!");
        //            return new ResponseEntity<>(res, HttpStatus.OK);
        //        }
      }

      ett.setClassCode(new byte[] {});
      ett.setCompiledTime(LocalDateTime.now(ZoneId.systemDefault()));
      ett.setCreatedTime(LocalDateTime.now(ZoneId.systemDefault()));

      String className9 = fqcn.substring(fqcn.lastIndexOf('.') + 1);
      ett.setClassName(className9);
      ett.setFqcn(fqcn);
      ett.setRawProcessClass(rawProcessClass);
      ett.setChecksum(checkSum);
      ett.setClassType(reqParser.getClassType());
      ett.setRawClass(reqParser.getEncodedFile());

      FunctionsEntity ert = functionDbService.saveFunction(ett);
      res.setId(ert.getId());
      res.setClassName(ert.getClassName());
      res.setVersion(ert.getVersion());
      res.setCompiledTime(ert.getCompiledTime());
      res.setModifiedTime(ert.getModifiedTime());

      FunctionsHitoryEntity history = new FunctionsHitoryEntity();
      ObjectUtil.copyFields(ert, history);
      history.setId(null);
      history.setFuncId(ert.getId());
      history.setVersion(ert.getVersion());
      functionDbService.saveHistoryFunction(history);

      //        break;
      //      }

    } catch (CompilationException e) {
      res.setStatus("Failed");
      res.setMessage(e.getMessage() + "\n\n\n" + rawProcessClass);
      res.setStakeStake(e.getStackTrace());

      List<String> str = new ArrayList<>();
      e.getDiagnostics()
          .forEach(
              d -> {
                str.add("Code: " + d.getCode() + "--->>> " + d.getSource());
                System.err.println("Code: " + d.getCode());
                System.err.println("Source: " + d.getSource());
              });
      res.setDiagnostic(str);
      return new ResponseEntity<>(res, HttpStatus.OK);
    } catch (Exception e) {

      res.setStatus("Failed");
      res.setMessage(e.getMessage());
      res.setStakeStake(e.getStackTrace());
      return new ResponseEntity<>(res, HttpStatus.OK);
    }

    res.setStatus("Success");

    return new ResponseEntity<>(res, HttpStatus.OK);
  }

  @GetMapping(
      value = "/getFiles",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<List<GetFileListProjection>> getFiles(@Param("classType") String classType) {
    List<GetFileListProjection> lstFiles = functionDbService.getListOfFiles(classType);
    return new ResponseEntity<>(lstFiles, HttpStatus.OK);
  }

  @GetMapping(
      value = "/getFolders",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<List<ClassTypeProjection>> getFolders() {
    List<ClassTypeProjection> lstFiles = classTypeService.getAllFunctions();
    return new ResponseEntity<>(lstFiles, HttpStatus.OK);
  }

  @GetMapping(
      value = "/getById",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<FunctionsEntity> getById(@Param("id") Integer id) throws Exception {
    Optional<FunctionsEntity> file = functionDbService.getAllFunctions(id);
    if (!file.isPresent()) {
      throw new Exception("No data found");
    }
    return new ResponseEntity<>(file.get(), HttpStatus.OK);
  }

  @GetMapping(
      value = "/getByClassTypeAndFqcn",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<FunctionsEntity> getByClassTypeAndFqcn(@Param("classType") String classType, @Param("fqcn") String fqcn) throws Exception {
    Optional<FunctionsEntity> file = functionDbService.getByClassTypeAndFqcn(classType, fqcn);
    if (!file.isPresent()) {
      throw new Exception("No data found");
    }
    return new ResponseEntity<>(file.get(), HttpStatus.OK);
  }
}
