package io.falconFlow.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "ff_functions_history")
public class FunctionsHitoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "funcId", nullable = false)
  private Integer funcId;

  @Column(name = "className", nullable = false, length = 200)
  private String className;

  @Column(name = "classType", nullable = false, length = 20)
  private String classType;

  @Column(name = "fqcn", nullable = false, length = 300)
  private String fqcn;

  // compiled .class bytes
  @Lob
  @Column(name = "classCode", nullable = false)
  private byte[] classCode;

  // original Java source
  @Lob
  @Column(name = "rawClass", nullable = false)
  @JdbcTypeCode(SqlTypes.LONGVARCHAR)
  private String rawClass;

  @Lob
  @Column(name = "rawProcessClass", nullable = false)
  @JdbcTypeCode(SqlTypes.LONGVARCHAR)
  private String rawProcessClass;

  @Column(
      name = "createdTime",
      nullable = false,
      updatable = false,
      columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdTime;

  @Column(name = "modifiedTime")
  private LocalDateTime modifiedTime;

  @Column(name = "compiledTime")
  private LocalDateTime compiledTime;

  @Column(name = "isdeleted", nullable = false)
  private Boolean isDeleted = false;

  @Column(name = "checksum", nullable = false, length = 500)
  private String checksum;

  @Version
  @Column(name = "version", nullable = false)
  private Integer version;

  @PrePersist
  public void prePersist() {
    this.createdTime = LocalDateTime.now(ZoneId.systemDefault());
  }

  @PreUpdate
  public void preUpdate() {
    this.modifiedTime = LocalDateTime.now(ZoneId.systemDefault());
  }

  // --- Getters & Setters ---
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getFqcn() {
    return fqcn;
  }

  public void setFqcn(String fqcn) {
    this.fqcn = fqcn;
  }

  public byte[] getClassCode() {
    return classCode;
  }

  public void setClassCode(byte[] classCode) {
    this.classCode = classCode;
  }

  public String getRawClass() {
    return rawClass;
  }

  public void setRawClass(String rawClass) {
    this.rawClass = rawClass;
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

  public LocalDateTime getCompiledTime() {
    return compiledTime;
  }

  public void setCompiledTime(LocalDateTime compiledTime) {
    this.compiledTime = compiledTime;
  }

  public Boolean getIsDeleted() {
    return isDeleted;
  }

  public void setIsDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  public String getClassType() {
    return classType;
  }

  public void setClassType(String classType) {
    this.classType = classType;
  }

  public String getChecksum() {
    return checksum;
  }

  public void setChecksum(String checksum) {
    this.checksum = checksum;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public String getRawProcessClass() {
    return rawProcessClass;
  }

  public void setRawProcessClass(String rawProcessClass) {
    this.rawProcessClass = rawProcessClass;
  }

  public Integer getFuncId() {
    return funcId;
  }

  public void setFuncId(Integer funcId) {
    this.funcId = funcId;
  }
}
