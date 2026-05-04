package com.example.skripsi.services;

import com.example.skripsi.exceptions.BadRequestExceptions;
import com.example.skripsi.interfaces.IUserService;
import com.example.skripsi.securities.SecurityUtils;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractMasterDataService<Entity, Response, CreateRequest, UpdateRequest> {
    protected final JpaRepository<Entity, Integer> repository;
    protected final IUserService userService;
    protected final SecurityUtils securityUtils;

    public AbstractMasterDataService(
            JpaRepository<Entity, Integer> repository,
            IUserService userService,
            SecurityUtils securityUtils) {
        this.repository = repository;
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    public List<Response> getAll() {
        List<Entity> entities = repository.findAll();

        if (entities.isEmpty()) {
            return List.of();
        }

        entities = entities.stream()
                .filter(this::isVisibleEntity)
                .collect(Collectors.toList());

        if (entities.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = extractUserIds(entities);
        Map<Long, String> userNameMap = userIds.isEmpty()
                ? new HashMap<>()
                : userService.getUserNameMap(userIds);

        return entities.stream()
                .map(entity -> toResponse(entity, userNameMap))
                .collect(Collectors.toList());
    }

    public Response create(CreateRequest request) {
        Long userId = securityUtils.getCurrentUserId();

        validateBeforeCreate(request);

        Entity entity = buildEntity(request, userId);
        Entity savedEntity = repository.save(entity);

        return toResponse(savedEntity, new HashMap<>());
    }

    public Response update(Integer id, UpdateRequest request) {
        Long userId = securityUtils.getCurrentUserId();

        Entity entity = repository.findById(id)
                .orElseThrow(() -> new BadRequestExceptions(getNotFoundErrorMessage()));

        updateEntityFields(entity, request);
        entity = setUpdateAuditFields(entity, userId);

        Entity savedEntity = repository.save(entity);

        return toResponse(savedEntity, new HashMap<>());
    }

    public Response delete(Integer id, Map<String, Object> body) {
        Entity entity = repository.findById(id)
                .orElseThrow(() -> new BadRequestExceptions(getNotFoundErrorMessage()));

        if (softDeleteEntity(entity)) {
            Entity savedEntity = repository.save(entity);
            return toResponse(savedEntity, new HashMap<>());
        }

        repository.delete(entity);
        return toResponse(entity, new HashMap<>());
    }

    protected boolean isVisibleEntity(Entity entity) {
        Boolean active = readBooleanProperty(entity, "getActive");
        if (active != null && !active) {
            return false;
        }

        Boolean deleted = readBooleanProperty(entity, "getIsDeleted");
        return deleted == null || !deleted;
    }

    protected boolean softDeleteEntity(Entity entity) {
        if (writeBooleanProperty(entity, "setActive", false)) {
            return true;
        }

        return writeBooleanProperty(entity, "setIsDeleted", true);
    }

    private Boolean readBooleanProperty(Entity entity, String methodName) {
        try {
            Object value = entity.getClass().getMethod(methodName).invoke(entity);
            return value instanceof Boolean ? (Boolean) value : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean writeBooleanProperty(Entity entity, String methodName, boolean value) {
        try {
            entity.getClass().getMethod(methodName, Boolean.class).invoke(entity, value);
            return true;
        } catch (NoSuchMethodException ignored) {
            try {
                entity.getClass().getMethod(methodName, boolean.class).invoke(entity, value);
                return true;
            } catch (Exception ignoredAgain) {
                return false;
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    protected abstract void validateBeforeCreate(CreateRequest request);

    protected abstract Entity buildEntity(CreateRequest request, Long userId);

    protected abstract void updateEntityFields(Entity entity, UpdateRequest request);

    protected abstract Entity setUpdateAuditFields(Entity entity, Long userId);

    protected abstract Response toResponse(Entity entity, Map<Long, String> userNameMap);

    protected abstract String getNotFoundErrorMessage();

    protected List<Long> extractUserIds(List<Entity> entities) {
        return entities.stream()
                .flatMap(entity -> {
                    Long createdBy = getCreatedBy(entity);
                    Long updatedBy = getUpdatedBy(entity);
                    return Stream.of(createdBy, updatedBy).filter(Objects::nonNull);
                })
                .distinct()
                .collect(Collectors.toList());
    }

    protected Long getCreatedBy(Entity entity) {
        try {
            return (Long) entity.getClass().getMethod("getCreatedBy").invoke(entity);
        } catch (Exception e) {
            return null;
        }
    }

    protected Long getUpdatedBy(Entity entity) {
        try {
            return (Long) entity.getClass().getMethod("getUpdatedBy").invoke(entity);
        } catch (Exception e) {
            return null;
        }
    }

    protected String resolveUsername(Long userId, Map<Long, String> userNameMap) {
        if (userId == null) {
            return null;
        }

        return userNameMap.get(userId);
    }
}
