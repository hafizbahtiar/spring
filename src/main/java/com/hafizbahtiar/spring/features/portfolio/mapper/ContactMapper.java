package com.hafizbahtiar.spring.features.portfolio.mapper;

import com.hafizbahtiar.spring.features.portfolio.dto.ContactRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.ContactResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.Contact;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for Contact entity ↔ DTO conversions.
 * Handles enum display names.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContactMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "repliedAt", ignore = true)
    @Mapping(target = "status", ignore = true) // Set by service
    @Mapping(target = "source", expression = "java(request.getSource() != null ? request.getSource() : com.hafizbahtiar.spring.features.portfolio.entity.ContactSource.FORM)")
    Contact toEntity(ContactRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "repliedAt", ignore = true)
    void updateEntityFromRequest(ContactRequest request, @MappingTarget Contact contact);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "statusDisplayName", expression = "java(contact.getStatus() != null ? contact.getStatus().getDisplayName() : null)")
    @Mapping(target = "sourceDisplayName", expression = "java(contact.getSource() != null ? contact.getSource().getDisplayName() : null)")
    ContactResponse toResponse(Contact contact);

    List<ContactResponse> toResponseList(List<Contact> contacts);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "statusDisplayName", expression = "java(contact.getStatus() != null ? contact.getStatus().getDisplayName() : null)")
    @Mapping(target = "source", source = "source")
    @Mapping(target = "sourceDisplayName", expression = "java(contact.getSource() != null ? contact.getSource().getDisplayName() : null)")
    @Mapping(target = "createdAt", source = "createdAt")
    ContactResponse.Summary toSummary(Contact contact);

    List<ContactResponse.Summary> toSummaryList(List<Contact> contacts);
}
