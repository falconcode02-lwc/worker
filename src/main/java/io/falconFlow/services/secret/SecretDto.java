package io.falconFlow.services.secret;

import io.falconFlow.entity.SecretEntity;
import java.time.Instant;

public class SecretDto {
    private Long id;
    private String name;
    private String type;
    private String value;
    private String metadata;
    private Instant createdAt;
    private Instant updatedAt;

    public SecretDto() {}

    public static SecretDto fromEntity(SecretEntity e) {
        if (e == null) return null;
        SecretDto d = new SecretDto();
        d.id = e.getId();
        d.name = e.getName();
        d.type = e.getType();
        d.value = e.getValue();
        d.metadata = e.getMetadata();
        d.createdAt = e.getCreatedAt();
        d.updatedAt = e.getUpdatedAt();
        return d;
    }

    public SecretEntity toEntity() {
        SecretEntity e = new SecretEntity();
        e.setId(this.id);
        e.setName(this.name);
        e.setType(this.type);
        e.setValue(this.value);
        e.setMetadata(this.metadata);
        e.setCreatedAt(this.createdAt);
        e.setUpdatedAt(this.updatedAt);
        return e;
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}