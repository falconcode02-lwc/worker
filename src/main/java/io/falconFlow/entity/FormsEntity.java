package io.falconFlow.entity;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(
    name = "ff_forms",
    uniqueConstraints = {@UniqueConstraint(name = "ff_forms_UN", columnNames = "code")}
)
public class FormsEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "code", length = 150, nullable = false)
  private String code;

  @Column(name = "name", length = 150, nullable = false)
  private String name;

  @Lob
  @Column(name = "formJson", columnDefinition = "JSON", nullable = false)
  private String formJson;

  @Column(name = "description", length = 2000, nullable = true)
  private String description;

  @Column(name = "version", nullable = false)
  private Integer version;

  @Column(name = "active", nullable = false)
  private boolean active = true;

  @Column(
      name = "createdTime",
      nullable = false,
      updatable = false,
      columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdTime;

  @Column(name = "modifiedTime")
  private LocalDateTime modifiedTime;

  @PrePersist
  public void prePersist() {
    this.createdTime = LocalDateTime.now(ZoneId.systemDefault());
    this.version = 1;
    this.active = true;
  }

  @PreUpdate
  public void preUpdate() {
    this.modifiedTime = LocalDateTime.now(ZoneId.systemDefault());
    if (this.version == null) {
      this.version = 1;
    } else {
      this.version++;
    }
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFormJson() {
    return formJson;
  }

  public void setFormJson(String formJson) {
    this.formJson = formJson;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public LocalDateTime getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(LocalDateTime createdTime) {
    this.createdTime = createdTime;
  }

  public LocalDateTime getModifiedTime() {
    return modifiedTime;
  }

  public void setModifiedTime(LocalDateTime modifiedTime) {
    this.modifiedTime = modifiedTime;
  }
}
