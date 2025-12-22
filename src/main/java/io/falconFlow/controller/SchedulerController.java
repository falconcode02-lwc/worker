package io.falconFlow.controller;

import io.falconFlow.entity.SchedulerEntity;
import io.falconFlow.services.genservice.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedulers")
public class SchedulerController {

  @Autowired private SchedulerService schedulerService;

  @GetMapping
  public List<SchedulerEntity> getAllSchedulers() {
    return schedulerService.getAll();
  }

  @GetMapping("/{id}")
  public SchedulerEntity getScheduler(@PathVariable Long id) {
    return schedulerService
        .getById(id)
        .orElseThrow(() -> new RuntimeException("Scheduler not found with id " + id));
  }

  @PostMapping
  public SchedulerEntity createScheduler(@RequestBody SchedulerEntity scheduler) {
    return schedulerService.create(scheduler);
  }

  @PutMapping("/{id}")
  public SchedulerEntity updateScheduler(
      @PathVariable Long id, @RequestBody SchedulerEntity scheduler) {
      scheduler.setId(id);
    return schedulerService.create(scheduler);
  }

  @DeleteMapping("/{id}")
  public void deleteScheduler(@PathVariable Long id) {
    schedulerService.delete(id);
  }

  @PatchMapping("/{id}/toggle")
  public SchedulerEntity toggleScheduler(@PathVariable Long id, @RequestParam boolean enabled) {
    return schedulerService.toggle(id, enabled);
  }
}
