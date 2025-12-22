package io.falconFlow.services.isolateservices;

import io.falconFlow.entity.PluginEntity;

import java.time.Instant;

/**
 * Data Transfer Object for PluginEntity used by controllers/services.
 */
public class PluginDto {
    private Integer id;
    private String pluginId;
    private String pluginName;
    private String pluginDesc;
    private String pluginAuthor;
    private String pluginDocument;
    private String props;
    private String secrets;
    private String rawClass;
    private String rawProcessClass;
    private String icon;
    private boolean active;
    private String version;
    private Instant lastLoadedAt;


    public PluginDto() {}

    public static PluginDto fromEntity(PluginEntity e) {
        if (e == null) return null;
        PluginDto d = new PluginDto();
        d.setId(e.getId());
        d.setPluginId(e.getPluginId());
        d.setPluginName(e.getPluginName());
        d.setPluginDesc(e.getPluginDesc());
        d.setPluginAuthor(e.getPluginAuthor());
        d.setPluginDocument(e.getPluginDocument());
    d.setProps(e.getProps());
        d.setSecrets(e.getSecrets());
    d.setRawClass(e.getRawClass());
    // rawProcessClass may be null if not present in entity
    try { d.setRawProcessClass(e.getRawProcessClass()); } catch (Throwable t) { /* ignore */ }
        d.setIcon(e.getIcon());
        d.setActive(e.isActive());
        d.setVersion(e.getVersion());
        d.setLastLoadedAt(e.getLastLoadedAt());
        return d;
    }

    public PluginEntity toEntity() {
        PluginEntity e = new PluginEntity();
        e.setId(this.id);
        e.setPluginId(this.pluginId);
        e.setPluginName(this.pluginName);
        e.setPluginDesc(this.pluginDesc);
        e.setPluginAuthor(this.pluginAuthor);
        e.setPluginDocument(this.pluginDocument);
        e.setProps(this.props);
        e.setSecrets(this.secrets);
    e.setRawClass(this.rawClass);
    try { e.setRawProcessClass(this.rawProcessClass); } catch (Throwable t) { /* ignore */ }
        e.setIcon(this.icon);
        e.setActive(this.active);
        e.setVersion(this.version);
        e.setLastLoadedAt(this.lastLoadedAt);
        return e;
    }

    // getters and setters
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
    // new primary property storage (props)
    public String getProps() { return props; }
    public void setProps(String props) { this.props = props; }

    public String getSecrets() {
        return secrets;
    }

    public void setSecrets(String secrets) {
        this.secrets = secrets;
    }

    // raw class/source associated with plugin
    public String getRawClass() { return rawClass; }
    public void setRawClass(String rawClass) { this.rawClass = rawClass; }

    // processed/normalized class source
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


}
