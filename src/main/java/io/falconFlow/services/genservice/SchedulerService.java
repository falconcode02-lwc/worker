package io.falconFlow.services.genservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.falconFlow.dto.WorkFlowManagerRequest;
import io.falconFlow.entity.SchedulerEntity;
import io.falconFlow.model.ScheduleRequest;
import io.falconFlow.repository.SchedulerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SchedulerService {


  @Autowired
    ObjectMapper mapper;

  private final SchedulerRepository repo;
  private final TemporalSchedulerClient temporalClient;

  public SchedulerService(SchedulerRepository repo, TemporalSchedulerClient temporalClient) {
    this.repo = repo;
    this.temporalClient = temporalClient;
  }

  public List<SchedulerEntity> getAll() {
    return repo.findAll();
  }

  public Optional<SchedulerEntity> getById(Long id) {
    return repo.findById(id);
  }

  public SchedulerEntity create(SchedulerEntity scheduler) {
    // Step 1: Save to DB first
      Optional<SchedulerEntity> existing = null;
      if(scheduler.getId() !=null){
          existing = repo.findById(scheduler.getId());
      }





    ScheduleRequest request = new ScheduleRequest();
    request.setCron(scheduler.getCron());
    request.setEnable(scheduler.getEnabled());
    //request.setScheduleId(scheduler.getId());

     String inputReq  = scheduler.getRequest();
     if(inputReq != null && !inputReq.isEmpty()){
         WorkFlowManagerRequest inr = null;
         try {
             inr = mapper.readValue(inputReq, WorkFlowManagerRequest.class);
         } catch (JsonProcessingException e) {
             throw new RuntimeException(e);
         }
         request.setRequest(inr);
     }
     SchedulerEntity schSaved = null;
     boolean isNew = false;
     if(existing == null){
         isNew = true;
     }else{
         if(existing.isEmpty()){
             isNew = true;
         }
     }

      if(isNew){
          boolean isEnabled = scheduler.getEnabled();
          scheduler.setEnabled(false);
          scheduler.setId(null);
          schSaved = repo.save(scheduler);
          repo.flush();

          request.setScheduleId(scheduler.getName() + "-" + schSaved.getId());
          // Step 2: Create schedule in Temporal
          String scheduleId =
                  temporalClient.createSchedule(request);
          // schSaved = repo.findById(schSaved.getId()).orElseThrow();
          schSaved.setEnabled(isEnabled);
          //schSaved.setEnabled(scheduler.getEnabled());
          // Step 3: Update entity with Temporal schedule ID
          schSaved.setScheduleRefId(scheduleId);
          schSaved = repo.save(schSaved);
      }else{
          scheduler.setName(existing.get().getName());
          scheduler.setId(existing.get().getId());
          request.setEnable(scheduler.getEnabled());
          request.setScheduleId(existing.get().getName() + "-" +existing.get().getId().toString());
          temporalClient.updateCron(request);
          schSaved = repo.save(scheduler);
      }

    return schSaved;
  }

  public SchedulerEntity update(Long id, SchedulerEntity scheduler) {
    SchedulerEntity existing =
        repo.findById(id).orElseThrow(() -> new RuntimeException("Scheduler not found: " + id));

    scheduler.setId(existing.getId());

      ScheduleRequest request = new ScheduleRequest();
      request.setCron(scheduler.getCron());
      request.setEnable(scheduler.getEnabled());
    //  request.setRequest(scheduler.getRequest());
    repo.save(scheduler);

    // Sync Temporal state
    if (existing.getId() != null) {
      temporalClient.updateCron(request);
    }

    return existing;
  }

  public void delete(Long id) {
    SchedulerEntity existing =
        repo.findById(id).orElseThrow(() -> new RuntimeException("Scheduler not found: " + id));
    if (existing.getId() != null) {
     // temporalClient.deleteSchedule(existing.getId());
    }
    repo.delete(existing);
  }

  public SchedulerEntity toggle(Long id,  boolean enabled) {
    SchedulerEntity existing =
        repo.findById(id).orElseThrow(() -> new RuntimeException("Scheduler not found: " + id));

    existing.setEnabled(enabled);
    repo.save(existing);

    if (existing.getId() != null) {
      temporalClient.toggleSchedule(existing.getName() + "-"+existing.getId().toString(), enabled);
    }

    return existing;
  }
}
