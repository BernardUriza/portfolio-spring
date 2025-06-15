package com.portfolio.service;

import com.portfolio.dto.ContactDTO;
import com.portfolio.model.Contact;
import com.portfolio.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;

    public List<ContactDTO> getAllContacts() {
        return contactRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ContactDTO createContact(ContactDTO dto) {
        Contact contact = toEntity(dto);
        contact = contactRepository.save(contact);
        return toDto(contact);
    }

    public ContactDTO updateContact(Long id, ContactDTO dto) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contacto no encontrado con id: " + id));
        contact.setName(dto.getName());
        contact.setEmail(dto.getEmail());
        contact.setMessage(dto.getMessage());
        contact = contactRepository.save(contact);
        return toDto(contact);
    }

    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
    }

    private Contact toEntity(ContactDTO dto) {
        return Contact.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .message(dto.getMessage())
                .build();
    }

    private ContactDTO toDto(Contact contact) {
        ContactDTO dto = new ContactDTO();
        dto.setId(contact.getId());
        dto.setName(contact.getName());
        dto.setEmail(contact.getEmail());
        dto.setMessage(contact.getMessage());
        return dto;
    }
}
