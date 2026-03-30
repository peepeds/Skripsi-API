package com.example.skripsi.services;

import com.example.skripsi.entities.User;
import com.example.skripsi.exceptions.BadRequestExceptions;
import com.example.skripsi.repositories.UserRepository;
import com.example.skripsi.securities.SecurityUtils;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractMasterDataService<Entity, Response, CreateRequest, UpdateRequest> {
    protected final JpaRepository<Entity, Integer> repository;
    protected final UserRepository userRepository;
    protected final SecurityUtils securityUtils;

    public AbstractMasterDataService(
            JpaRepository<Entity, Integer> repository,
            UserRepository userRepository,
            SecurityUtils securityUtils) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    public List<Response> getAll() {
        List<Entity> entities = repository.findAll();

        if (entities.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = extractUserIds(entities);
        Map<Long, User> userMap = new java.util.HashMap<>();

        if (!userIds.isEmpty()) {
            Map<Long, User> fetchedUsers = userRepository.findByUserIdMap(userIds);
            userMap.putAll(fetchedUsers);
        }

        final Map<Long, User> finalUserMap = userMap;
        return entities.stream()
                .map(entity -> toResponse(entity, finalUserMap))
                .collect(Collectors.toList());
    }

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

    public Response create(CreateRequest request) {
        Long userId = securityUtils.getCurrentUserId();

        validateBeforeCreate(request);

        Entity entity = buildEntity(request, userId);
        Entity savedEntity = repository.save(entity);

        return toResponse(savedEntity, new java.util.HashMap<>());
    }

    public Response update(Integer id, UpdateRequest request) {
        Long userId = securityUtils.getCurrentUserId();

        Entity entity = repository.findById(id)
                .orElseThrow(() -> new BadRequestExceptions(getNotFoundErrorMessage()));

        updateEntityFields(entity, request);
        entity = setUpdateAuditFields(entity, userId);

        Entity savedEntity = repository.save(entity);

        return toResponse(savedEntity, new java.util.HashMap<>());
    }

    protected String resolveUsername(Long userId, Map<Long, User> userMap) {
        if (userId == null) {
            return null;
        }
        User user = userMap.get(userId);
        return user != null ? user.getFirstName() : null;
    }

    protected abstract void validateBeforeCreate(CreateRequest request);

    protected abstract Entity buildEntity(CreateRequest request, Long userId);

    protected abstract void updateEntityFields(Entity entity, UpdateRequest request);

    protected abstract Entity setUpdateAuditFields(Entity entity, Long userId);

    protected abstract Response toResponse(Entity entity, Map<Long, User> userMap);

    protected abstract String getNotFoundErrorMessage();
}
