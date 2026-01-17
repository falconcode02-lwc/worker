package io.falconFlow.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

// import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "ff_workflows",
    uniqueConstraints = {@UniqueConstraint(name = "ff_functions_UN", columnNames = "code")}
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class WorkFlowsEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "code", length = 150, nullable = false)
  private String code;

  @Column(name = "name", length = 150, nullable = false)
  private String name;

    @Column(name = "controller", length = 200, nullable = true)
    private String controller;

  @Column(name = "workflowJson", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private String workflowJson;


    @Column(name = "workflowJsonRaw", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String workflowJsonRaw;

  @Column(name = "description", length = 2000, nullable = true)
  private String description;

  //@Version
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id")
  @JsonIgnoreProperties({"workflows", "hibernateLazyInitializer", "handler"})
  private ProjectEntity project;

  @PrePersist
  public void prePersist() {
    this.createdTime = LocalDateTime.now(ZoneId.systemDefault());
    this.version = 1; // start versioning at 1
    this.active = true;
  }

  @PreUpdate
  public void preUpdate() {
    this.modifiedTime = LocalDateTime.now(ZoneId.systemDefault());
    if (this.version == null) {
      this.version = 1;
    } else {
      this.version++; // custom trigger-like behavior
    }
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getWorkflowJson() {
    return workflowJson;
  }

  public void setWorkflowJson(String workflowJson) {
    this.workflowJson = workflowJson;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getWorkflowJsonRaw() {
        return workflowJsonRaw;
    }

    public void setWorkflowJsonRaw(String workflowJsonRaw) {
        this.workflowJsonRaw = workflowJsonRaw;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }
}
