package io.falconFlow.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ff_provider_config", uniqueConstraints = {@UniqueConstraint(name = "provider_config_UN", columnNames = "name")})
public class ProviderConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., openai, chatgpt

    @Column(nullable = false)
    private String apiUrl; // full URL to call (e.g., https://api.openai.com/v1/chat/completions)

    @Column
    private String defaultModel;

    @Lob
    private String properties; // free-form JSON for provider-specific settings

    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

    public String getDefaultModel() { return defaultModel; }
    public void setDefaultModel(String defaultModel) { this.defaultModel = defaultModel; }

    public String getProperties() { return properties; }
    public void setProperties(String properties) { this.properties = properties; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    public void prePersist() { this.createdAt = Instant.now(); }

    @PreUpdate
    public void preUpdate() { this.updatedAt = Instant.now(); }
}
