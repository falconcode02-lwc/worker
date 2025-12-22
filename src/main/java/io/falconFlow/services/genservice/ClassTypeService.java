package io.falconFlow.services.genservice;

import io.falconFlow.dto.ClassTypeProjection;
import io.falconFlow.repository.ClassTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassTypeService {

  @Autowired ClassTypeRepository classTypeRepository;

  public List<ClassTypeProjection> getAllFunctions() {
    return classTypeRepository.findAllActiveWithCount();
  }
}
