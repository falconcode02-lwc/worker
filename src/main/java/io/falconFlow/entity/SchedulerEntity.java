package io.falconFlow.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "ff_scheduler",
        uniqueConstraints = {@UniqueConstraint(name = "ff_scheduler_UN", columnNames = "name")})
public class SchedulerEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String name;

  private String description;

  @Column(nullable = false)
  private String frequency; // hourly, daily, weekly, monthly, custom

  private String cron; // generated cron expression

  private Integer hourInterval; // used if frequency = hourly
  private Integer dayOfMonth; // used if monthly
  private String daysOfWeek; // comma-separated (e.g. "1,3,5")
  private String months; // comma-separated (e.g. "1,4,7,10")
  private String scheduleRefId;
  private Boolean enabled = true;

  private LocalDateTime nextRun;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Column(nullable = true, columnDefinition = "JSON")
  private String request;

  @PrePersist
  public void onCreate() {
    this.createdAt = LocalDateTime.now(ZoneId.systemDefault());
    this.updatedAt = LocalDateTime.now(ZoneId.systemDefault());
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = LocalDateTime.now(ZoneId.systemDefault());
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getFrequency() {
    return frequency;
  }

  public void setFrequency(String frequency) {
    this.frequency = frequency;
  }

  public String getCron() {
    return cron;
  }

  public void setCron(String cron) {
    this.cron = cron;
  }

  public Integer getHourInterval() {
    return hourInterval;
  }

  public void setHourInterval(Integer hourInterval) {
    this.hourInterval = hourInterval;
  }

  public Integer getDayOfMonth() {
    return dayOfMonth;
  }

  public void setDayOfMonth(Integer dayOfMonth) {
    this.dayOfMonth = dayOfMonth;
  }

  public String getDaysOfWeek() {
    return daysOfWeek;
  }

  public void setDaysOfWeek(String daysOfWeek) {
    this.daysOfWeek = daysOfWeek;
  }

  public String getMonths() {
    return months;
  }

  public void setMonths(String months) {
    this.months = months;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public LocalDateTime getNextRun() {
    return nextRun;
  }

  public void setNextRun(LocalDateTime nextRun) {
    this.nextRun = nextRun;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getScheduleRefId() {
        return scheduleRefId;
    }

    public void setScheduleRefId(String scheduleRefId) {
        this.scheduleRefId = scheduleRefId;
    }
}

