package io.falconFlow.services.isolateservices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.falconFlow.entity.PluginEntity;
import io.falconFlow.interfaces.FParam;
import io.falconFlow.interfaces.FResource;
import io.falconFlow.interfaces.MCPToolRegistry;
import io.falconFlow.model.MCPToolDefinition;
import io.falconFlow.model.PluginMethodModel;
import io.falconFlow.model.PluginSecretModel;
import io.falconFlow.repository.PluginRepository;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Service
public class PluginManagerService {

    @Autowired
    private ApplicationContext context;
	private final PluginRepository pluginRepository;
    private final MCPToolRegistry registry;

    @Autowired
    ObjectMapper mapper;

	@Autowired
	public PluginManagerService(PluginRepository pluginRepository, MCPToolRegistry registry) {
		this.pluginRepository = pluginRepository;
        this.registry = registry;
	}

	public java.util.List<PluginDto> listAll() {
		// return DTOs instead of entities
		return pluginRepository.findAll().stream().map(PluginDto::fromEntity).toList();
	}

	public Optional<PluginDto> findById(Integer id) {
		return pluginRepository.findById(id).map(PluginDto::fromEntity);
	}

	public Optional<PluginDto> findByPluginId(String pluginId) {
		return pluginRepository.findByPluginId(pluginId).map(PluginDto::fromEntity);
	}

	@Transactional
	public PluginDto create(PluginDto pluginDto) {
		// ensure pluginId uniqueness may be enforced by DB constraint
		PluginEntity entity = pluginDto.toEntity();
		entity.setActive(true);
		PluginEntity saved = pluginRepository.save(entity);
		return PluginDto.fromEntity(saved);
	}

	@Transactional
	public PluginDto update(Integer id, PluginDto updateDto) {
		return pluginRepository.findById(id).map(existing -> {
			if (updateDto.getPluginId() != null) existing.setPluginId(updateDto.getPluginId());
			if (updateDto.getPluginName() != null) existing.setPluginName(updateDto.getPluginName());
			if (updateDto.getPluginDesc() != null) existing.setPluginDesc(updateDto.getPluginDesc());
			if (updateDto.getPluginAuthor() != null) existing.setPluginAuthor(updateDto.getPluginAuthor());
			if (updateDto.getPluginDocument() != null) existing.setPluginDocument(updateDto.getPluginDocument());
			if (updateDto.getProps() != null) existing.setProps(decodeBase64(updateDto.getProps()));
            if (updateDto.getSecrets() != null) existing.setSecrets(decodeBase64(updateDto.getSecrets()));
		 	if (updateDto.getIcon() != null) existing.setIcon(updateDto.getIcon());
            if (updateDto.getPluginType() != null) existing.setPluginType(updateDto.getPluginType());
			existing.setActive(updateDto.isActive());
			PluginEntity saved = pluginRepository.save(existing);
			return PluginDto.fromEntity(saved);
		}).orElseGet(() -> {
			// if not found, treat as create
			PluginEntity created = updateDto.toEntity();
			created.setActive(true);
			PluginEntity saved = pluginRepository.save(created);
			return PluginDto.fromEntity(saved);
		});
	}

	@Transactional
	public void delete(Integer id) {
		pluginRepository.deleteById(id);
	}

	@Transactional
	public PluginDto setActive(Integer id, boolean active) {
		return pluginRepository.findById(id).map(p -> {
			p.setActive(active);
			PluginEntity saved = pluginRepository.save(p);
			return PluginDto.fromEntity(saved);
		}).orElse(null);
	}


    @Transactional
    public PluginDto register(PluginDto pluginDto, Object bean) {
        if (bean == null) throw new IllegalArgumentException("Bean cannot be null");

        String resources = scanAndRegisterTools(bean);
        // Derive pluginId from the actual bean class
        String pluginId = AopUtils.getTargetClass(bean).getSimpleName();
        pluginDto.setResources(resources);
        pluginDto.setPluginId(pluginId);
        
        // Call the string-based register to handle DB persistence
        PluginDto saved = register(pluginDto, pluginId);
        
        // Scan tools directly on the instance (bypassing context lookup issues)

        
        return saved;
    }

	/**
	 * Register a plugin (DTO) and trigger its onload handler once per process lifetime.
	 * The incoming PluginDto may contain version and rawClass/rawProcessClass.
	 * If the plugin code changed, it will be saved via setProps. This method ensures onload
	 * is only invoked once per JVM/process for the given pluginId by tracking loadedPlugins.
	 */
	@Transactional
	public PluginDto register(PluginDto pluginDto,String pluginId) {
        pluginDto.setPluginId(pluginId);
        
        if (pluginDto.getPluginId() == null || pluginDto.getPluginId().isBlank()) {
            throw new IllegalArgumentException("plugin and pluginId are required");
        }

        String pid = pluginDto.getPluginId();

	// Update raw classes if changed (creates if missing)
	//setProps(pid, pluginDto.getRawClass(), pluginDto.getRawProcessClass());

        Optional<PluginEntity> opt = pluginRepository.findByPluginId(pid);
        PluginEntity saved;
        if (opt.isPresent()) {
            // update existing record (preserve DB id)
            saved = opt.get();
            if (pluginDto.getPluginId() != null) saved.setPluginId(pluginDto.getPluginId());
            if (pluginDto.getPluginName() != null) saved.setPluginName(pluginDto.getPluginName());
            if (pluginDto.getPluginDesc() != null) saved.setPluginDesc(pluginDto.getPluginDesc());
            if (pluginDto.getPluginAuthor() != null) saved.setPluginAuthor(pluginDto.getPluginAuthor());
            if (pluginDto.getPluginDocument() != null) saved.setPluginDocument(pluginDto.getPluginDocument());
            if (pluginDto.getIcon() != null) saved.setIcon(pluginDto.getIcon());
            if (pluginDto.getVersion() != null) saved.setVersion(pluginDto.getVersion());
            if (pluginDto.getProps() != null) saved.setProps(decodeBase64(pluginDto.getProps()));
            if (pluginDto.getSecrets() != null) saved.setSecrets(decodeBase64(pluginDto.getSecrets()));
            if (pluginDto.getAiToolDescription() != null) saved.setResources(pluginDto.getResources());
            saved.setAiTool(pluginDto.isAiTool());
            if (pluginDto.getResources() != null) saved.setResources(pluginDto.getResources());
            if (pluginDto.getPluginType() != null) saved.setPluginType(pluginDto.getPluginType());
            saved.setActive(true);
            saved = pluginRepository.save(saved);
        } else {
            // create new record with provided pluginId
            PluginEntity p = new PluginEntity();
            p.setPluginId(pid);
            p.setPluginName(pluginDto.getPluginName() != null ? pluginDto.getPluginName() : pid);
            p.setPluginDesc(pluginDto.getPluginDesc() != null ? pluginDto.getPluginDesc() : "");
            p.setPluginAuthor(pluginDto.getPluginAuthor() != null ? pluginDto.getPluginAuthor() : "");
            p.setPluginDocument(pluginDto.getPluginDocument() != null ? pluginDto.getPluginDocument() : "");
			p.setProps(pluginDto.getProps() == null ? "" : decodeBase64(pluginDto.getProps()));
            p.setSecrets(pluginDto.getSecrets() == null ? "" : decodeBase64(pluginDto.getSecrets()));
			p.setRawClass(pluginDto.getRawClass() == null ? "" : pluginDto.getRawClass());
			try { p.setRawProcessClass(pluginDto.getRawProcessClass() == null ? "" : pluginDto.getRawProcessClass()); } catch (Throwable t) { }
            p.setIcon(pluginDto.getIcon());
            p.setVersion(pluginDto.getVersion());
            p.setAiToolDescription(pluginDto.getAiToolDescription());
            p.setAiTool(pluginDto.isAiTool());
            p.setResources(pluginDto.getResources());
            if (pluginDto.getPluginType() != null) p.setPluginType(pluginDto.getPluginType());
            p.setActive(true);
            saved = pluginRepository.save(p);
        }

        // invoke onload only once per JVM/process
        if (loadedPlugins.add(saved.getPluginId())) {
            saved.setLastLoadedAt(java.time.Instant.now());
            pluginRepository.save(saved);
            // Note: actual invocation of plugin lifecycle (Java code) is out of scope here.
            // Tool scanning is handled by the Object-based register() overload
        }

        return PluginDto.fromEntity(saved);
    }

	// in-memory set tracking which pluginIds have had onload invoked in this process
	private final java.util.Set<String> loadedPlugins = java.util.concurrent.ConcurrentHashMap.newKeySet();

	/**
	 * Decodes a base64 encoded string. If the input is not valid base64, returns the original string.
	 * This allows backward compatibility with non-encoded data.
	 */
	private String decodeBase64(String encoded) {
		if (encoded == null || encoded.isEmpty()) {
			return encoded;
		}
		try {
			byte[] decodedBytes = Base64.getDecoder().decode(encoded);
			return new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);
		} catch (IllegalArgumentException e) {
			// If decoding fails, return the original string (backward compatibility)
			return encoded;
		}
	}

    public PluginSecretModel getSecret(String pluginId){
       Optional<PluginEntity> pluginEntity =  pluginRepository.findByPluginId(pluginId);
       if(pluginEntity!=null && pluginEntity.isPresent()){
           String secrets =  pluginEntity.get().getSecrets();
           try {
             return mapper.readValue(secrets, PluginSecretModel.class);
           } catch (JsonProcessingException e) {
               return null;
           }
       }
       return  null;
    }

    private String scanAndRegisterTools(Object bean) {
            Class<?> clazz = AopUtils.getTargetClass(bean);

            List<PluginMethodModel> pluginMethodModelList = new ArrayList<>();
            for (Method method : clazz.getDeclaredMethods()) {


                FResource tool = method.getAnnotation(FResource.class);
                if (tool == null) continue;
                MCPToolDefinition def = new MCPToolDefinition();
                def.setName(clazz.getSimpleName() +":"+ method.getName());
                def.setDescription(tool.description());
                def.setMethod(method);
                def.setBean(clazz.getSimpleName());
                Map<String, Map<String, String>>  params = resolveSchema(method);
                def.setInputSchema(params);
                System.out.println("Name >>>>> " + def.getName());
                System.out.println("Defination >>>>> " + def.getInputSchema());

                // add method details
                PluginMethodModel pluginMethodModel = new PluginMethodModel();
                pluginMethodModel.setMethod(def.getMethod().getName());
                pluginMethodModel.setName(def.getName());
                pluginMethodModel.setProperties(params);
                pluginMethodModel.setDisplayName(tool.name());
                pluginMethodModel.setDescription(tool.description());
                pluginMethodModel.setSelected(tool.selected());
                pluginMethodModelList.add(pluginMethodModel);



                registry.register(def);
            }
        try {
            return  mapper.writeValueAsString(pluginMethodModelList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Map<String, String>> resolveSchema(Method method) {
        Map<String, Map<String, String>> schema = new LinkedHashMap<>();
        for (Parameter p : method.getParameters()) {
            FParam ann = p.getAnnotation(FParam.class);
            Map<String, String> details = new HashMap<>();
            details.put("type", resolveMcpType(p.getType()));
            if (ann != null) {
                details.put("description", ann.description());
                details.put("required", String.valueOf(ann.required()));
                schema.put(ann.value(), details);
            } else {
                details.put("required", "false");
                schema.put(p.getName(), details);
            }
        }

        return schema;
    }



    private String resolveMcpType(Class<?> type) {

        // String / char
        if (type == String.class || type == char.class || type == Character.class) {
            return "string";
        }

        // Integers
        if (type == int.class || type == Integer.class
                || type == long.class || type == Long.class
                || type == short.class || type == Short.class) {
            return "integer";
        }

        // Floating point
        if (type == float.class || type == Float.class
                || type == double.class || type == Double.class) {
            return "number";
        }

        // Boolean
        if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        }

        // Enums
        if (type.isEnum()) {
            return "string";
        }

        // Arrays / collections
        if (type.isArray() || Collection.class.isAssignableFrom(type)) {
            return "array";
        }

        // Maps / objects
        if (Map.class.isAssignableFrom(type)) {
            return "object";
        }

        // Dates
        if (java.time.temporal.Temporal.class.isAssignableFrom(type)
                || java.util.Date.class.isAssignableFrom(type)) {
            return "string";
        }

        // Fallback (POJO)
        return "object";
    }
}
