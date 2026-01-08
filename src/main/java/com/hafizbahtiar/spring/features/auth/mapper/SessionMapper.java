package com.hafizbahtiar.spring.features.auth.mapper;

import com.hafizbahtiar.spring.features.auth.dto.SessionResponse;
import com.hafizbahtiar.spring.features.auth.entity.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for Session entity ↔ DTO conversions.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SessionMapper {

    /**
     * Convert Session entity to SessionResponse.
     * Uses sessionId (UUID) as the id field for frontend compatibility.
     */
    @Mapping(source = "sessionId", target = "id")
    @Mapping(source = "user.id", target = "userId")
    SessionResponse toResponse(Session session);

    /**
     * Convert list of Session entities to list of SessionResponse.
     */
    List<SessionResponse> toResponseList(List<Session> sessions);
}
