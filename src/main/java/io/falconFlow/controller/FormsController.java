package io.falconFlow.controller;

import io.falconFlow.dto.FormsDTO;
import io.falconFlow.entity.FormsEntity;
import io.falconFlow.services.genservice.FormsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/forms")
public class FormsController {

    @Autowired
    private FormsService formsService;

    @PostMapping
    public FormsEntity createForm(@RequestBody FormsEntity dto) {
        if (dto.getId() != null && dto.getId() > 0) {
            return formsService.update(dto.getId(), dto);
        } else {
            dto.setId(null);
        }
        return formsService.create(dto);
    }

    @GetMapping
    public List<FormsDTO> getAllForms() {
        return formsService.findAll();
    }

    @GetMapping("/code/{code}")
    public FormsDTO getFormByCode(@PathVariable String code) {
        return formsService.findByCode(code);
    }

    @GetMapping("/{id}")
    public FormsDTO getFormById(@PathVariable Integer id) {
        return formsService.findById(id);
    }

    @PutMapping("/{id}/active")
    public FormsDTO updateActive(@PathVariable Integer id, @RequestParam boolean active) {
        return formsService.updateActive(id, active);
    }

    @PutMapping("/{id}")
    public FormsEntity updateForm(@PathVariable Integer id, @RequestBody FormsEntity dto) {
        return formsService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteForm(@PathVariable Integer id) {
        formsService.delete(id);
    }

    @GetMapping("/active")
    public List<FormsDTO> getActiveForms() {
        return formsService.findAllActive();
    }
}
