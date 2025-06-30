package com.portfolio.mapper;

import com.portfolio.dto.ContactDTO;
import com.portfolio.model.Contact;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContactMapper {
    ContactDTO toDto(Contact contact);
    Contact toEntity(ContactDTO dto);
}
