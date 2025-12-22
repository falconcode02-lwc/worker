package io.falconFlow.services.genservice;

import io.falconFlow.dto.GetFileListProjection;
import io.falconFlow.entity.FunctionsEntity;
import io.falconFlow.entity.FunctionsHitoryEntity;
import io.falconFlow.repository.FunctionHistoryRepository;
import io.falconFlow.repository.FunctionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class FunctionService {

  private final FunctionRepository repository;
  private final FunctionHistoryRepository repositoryHistory;

  public FunctionService(
      FunctionRepository repository, FunctionHistoryRepository repositoryHistory) {

    this.repository = repository;
    this.repositoryHistory = repositoryHistory;
  }

  // CREATE or UPDATE
  public FunctionsEntity saveFunction(FunctionsEntity entity) {
    entity.setModifiedTime(LocalDateTime.now(ZoneId.systemDefault()));

    return repository.save(entity);
  }

  public FunctionsHitoryEntity saveHistoryFunction(FunctionsHitoryEntity entity) {
    entity.setModifiedTime(LocalDateTime.now(ZoneId.systemDefault()));

    return repositoryHistory.save(entity);
  }

  // READ all
  public List<FunctionsEntity> getAllFunctions() {
    return repository.findAll();
  }

  public Optional<FunctionsEntity> getAllFunctions(Integer id) {
    return repository.findById(id);
  }

  // READ by classType and FQCN
  public Optional<FunctionsEntity> getByClassTypeAndFqcn(String classType, String fqcn) {
    return repository.findByClassTypeAndFqcn(classType, fqcn);
  }

  // DELETE by Id
  public void deleteById(Integer id) {
    repository.deleteById(id);
  }

  // DELETE by FQCN
  public void deleteByFqcn(String fqcn) {
    repository.deleteByFqcn(fqcn);
  }

  // SOFT DELETE (update flag instead of removing)
  public void softDelete(String fqcn) {
    // Remove or update this method if findByFqcn is not available
    // repository.findByFqcn(fqcn).ifPresent(entity -> {
    //   entity.setIsDeleted(true);
    //   entity.setModifiedTime(LocalDateTime.now(ZoneId.systemDefault()));
    //   repository.save(entity);
    // });
  }

  // get all
  public List<GetFileListProjection> getListOfFiles(String classType) {
    return repository.findAllFileList(classType);
  }

  public List<GetFileListProjection> getListOfFunctions() {
    return repository.findAllListOfFunctions();
  }

  public List<GetFileListProjection> getListOfFolders() {
    return repository.findAllListOfFolders();
  }
}
