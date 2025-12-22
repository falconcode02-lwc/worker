package io.falconFlow.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "ff_class_types",
    uniqueConstraints = {@UniqueConstraint(name = "ff__UN", columnNames = "class_type")})
public class ClassTypeEntity {

  protected ClassTypeEntity() {
    // Default constructor for Hibernate
  }

  @Id
  // @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "class_type", nullable = false, length = 50)
  private String classType;

  @Column(name = "class_type_key", nullable = false, length = 50)
  private String classTypeKey;

  @Column(name = "seq", nullable = true)
  private Integer seq;

  @Column(name = "icon", nullable = true, length = 50)
  private String icon;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getClassType() {
    return classType;
  }

  public void setClassType(String classType) {
    this.classType = classType;
  }

  public Integer getSeq() {
    return seq;
  }

  public void setSeq(Integer seq) {
    this.seq = seq;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getClassTypeKey() {
    return classTypeKey;
  }

  public void setClassTypeKey(String classTypeKey) {
    this.classTypeKey = classTypeKey;
  }

  @PrePersist
  public void prePersist() {
    this.active = true;
  }

  public ClassTypeEntity(
      Integer id, String classTypeKey, String classType, String icon, Integer seq) {
    this.id = id;
    this.classTypeKey = classTypeKey;
    this.classType = classType;
    this.icon = icon;
    this.seq = seq;
  }
}
