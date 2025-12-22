package io.falconFlow.services.prereq;

import io.falconFlow.entity.ClassTypeEntity;
import io.falconFlow.repository.ClassTypeRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Priority(1)
@Service
public class SetupService {

  @Autowired ClassTypeRepository classTypeRepository;

  @PostConstruct
  @Transactional
  void init() {


    List<ClassTypeEntity> lst =
        List.of(
            new ClassTypeEntity(1, "function", "Functions", "code-sandbox", 1),
            new ClassTypeEntity(2, "condition", "Conditions", "branches", 2),
            new ClassTypeEntity(3, "api", "API", "api", 5),
            new ClassTypeEntity(4, "model", "Model", "file-text", 3),
            new ClassTypeEntity(5, "object", "Objects", "table", 4),
            new ClassTypeEntity(6, "controller", "Controller", "delete-row", 3),
            new ClassTypeEntity(7, "plugin", "Plugin", "appstore-add", 6));

    for (ClassTypeEntity entity : lst) {
      classTypeRepository
          .findById(entity.getId())
          .ifPresentOrElse(
              existing -> {
                existing.setClassTypeKey(entity.getClassTypeKey());
                existing.setClassType(entity.getClassType());
                existing.setIcon(entity.getIcon());
                existing.setSeq(entity.getSeq());
                classTypeRepository.save(existing);
              },
              () -> classTypeRepository.save(entity));
    }

    System.out.println("✅ Class types seeded successfully");
  }
}
