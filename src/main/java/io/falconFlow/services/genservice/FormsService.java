package io.falconFlow.services.genservice;

import io.falconFlow.configuration.CacheConfig;
import io.falconFlow.dto.FormsDTO;
import io.falconFlow.entity.FormsEntity;
import io.falconFlow.repository.FormsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FormsService {

    @Autowired
    private FormsRepository formsRepository;

    @CachePut(value = CacheConfig.FORMS_CACHE, key = "#result.id")
    public FormsEntity create(FormsEntity entity) {
        return formsRepository.save(entity);
    }

    @Cacheable(value = CacheConfig.FORMS_CACHE, key = "'all'")
    public List<FormsDTO> findAll() {
        return formsRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.FORMS_CACHE, key = "'active'")
    public List<FormsDTO> findAllActive() {
        return formsRepository.findActiveForms().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.FORMS_CACHE, key = "#id")
    public FormsDTO findById(Integer id) {
        FormsEntity entity = formsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form not found with ID: " + id));
        return mapToDTO(entity);
    }

    @Cacheable(value = CacheConfig.FORMS_CACHE, key = "'code:' + #code")
    public FormsDTO findByCode(String code) {
        FormsEntity entity = formsRepository.findByCode(code);
        if (entity == null) {
            throw new RuntimeException("Form not found with code: " + code);
        }
        return mapToDTO(entity);
    }

    @CacheEvict(value = CacheConfig.FORMS_CACHE, allEntries = true)
    public FormsEntity update(Integer id, FormsEntity entity) {
        FormsEntity existingEntity = formsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form not found with ID: " + id));
        return formsRepository.save(entity);
    }

    @CacheEvict(value = CacheConfig.FORMS_CACHE, allEntries = true)
    public void delete(Integer id) {
        Integer result = formsRepository.updateActive(id, false);
        System.out.println("Form soft deleted. Result: " + result);
    }

    @CacheEvict(value = CacheConfig.FORMS_CACHE, allEntries = true)
    public FormsDTO updateActive(Integer id, boolean active) {
        Integer result = formsRepository.updateActive(id, active);
        System.out.println("Form active status updated. Result: " + result);
        return findById(id);
    }

    private FormsDTO mapToDTO(FormsEntity entity) {
        FormsDTO dto = new FormsDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setFormJson(entity.getFormJson());
        dto.setDescription(entity.getDescription());
        dto.setVersion(entity.getVersion());
        dto.setActive(entity.isActive());
        dto.setCreatedTime(entity.getCreatedTime());
        dto.setModifiedTime(entity.getModifiedTime());
        return dto;
    }
}
