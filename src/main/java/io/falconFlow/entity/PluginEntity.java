package io.falconFlow.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "ff_plugins",
    uniqueConstraints = {@UniqueConstraint(name = "ff_plugin__UN", columnNames = "plugin_id")},
        indexes = {@Index(name = "idx_plugin_name", columnList = "plugin_name" )}
)

public class PluginEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "plugin_id", nullable = false, length = 50)
    private String pluginId;

    @Column(name = "plugin_name", nullable = false, length = 100)
    private String pluginName;

    @Column(name = "plugin_desc", nullable = false, length = 100)
    private String pluginDesc;

    @Column(name = "plugin_author", nullable = false, length = 100)
    private String pluginAuthor;

    @Column(name = "plugin_document", nullable = false, length = 4000)
    private String pluginDocument;

    @Column(name = "plugin_category", nullable = true, length = 4000)
    private String pluginCategory;

    @Lob
    @Column(name = "props", nullable = true)
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String props;

    @Lob
    @Column(name = "secrets", nullable = true)
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String secrets;

    @Lob
    @Column(name = "rawClass", nullable = false)
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String rawClass;

    @Lob
    @Column(name = "resources", nullable = true)
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String resources;

    @Lob
    @Column(name = "rawProcessClass", nullable = false)
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String rawProcessClass;

    @Lob
    @Column(name = "icon", nullable = false)
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String icon;

    @Column(name = "aiToolDescription", nullable = true, length = 1000)
    private String aiToolDescription;

    @Column(name = "is_aitool", nullable = false)
    private boolean isAiTool = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "version", length = 50)
    private String version;

    private Instant lastLoadedAt;

    public PluginEntity() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getPluginId() { return pluginId; }
    public void setPluginId(String pluginId) { this.pluginId = pluginId; }

    public String getPluginName() { return pluginName; }
    public void setPluginName(String pluginName) { this.pluginName = pluginName; }

    public String getPluginDesc() { return pluginDesc; }
    public void setPluginDesc(String pluginDesc) { this.pluginDesc = pluginDesc; }

    public String getPluginAuthor() { return pluginAuthor; }
    public void setPluginAuthor(String pluginAuthor) { this.pluginAuthor = pluginAuthor; }

    public String getPluginDocument() { return pluginDocument; }
    public void setPluginDocument(String pluginDocument) { this.pluginDocument = pluginDocument; }

    // properties storage (JSON props)
    public String getProps() { return props; }
    public void setProps(String props) { this.props = props; }

    // rawClass: source/code associated with the plugin (retained as separate LOB)
    public String getRawClass() { return rawClass; }
    public void setRawClass(String rawClass) { this.rawClass = rawClass; }

    public String getRawProcessClass() { return rawProcessClass; }
    public void setRawProcessClass(String rawProcessClass) { this.rawProcessClass = rawProcessClass; }

   public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public Instant getLastLoadedAt() { return lastLoadedAt; }
    public void setLastLoadedAt(Instant lastLoadedAt) { this.lastLoadedAt = lastLoadedAt; }

    public String getPluginCategory() {
        return pluginCategory;
    }

    public void setPluginCategory(String pluginCategory) {
        this.pluginCategory = pluginCategory;
    }

    public String getSecrets() {
        return secrets;
    }

    public void setSecrets(String secrets) {
        this.secrets = secrets;
    }

    public boolean isAiTool() {
        return isAiTool;
    }

    public void setAiTool(boolean aiTool) {
        isAiTool = aiTool;
    }

    public String getAiToolDescription() {
        return aiToolDescription;
    }

    public void setAiToolDescription(String aiToolDescription) {
        this.aiToolDescription = aiToolDescription;
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }
}


