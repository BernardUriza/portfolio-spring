package com.portfolio.service;

import com.portfolio.dto.ContactDTO;
import com.portfolio.model.Contact;
import com.portfolio.mapper.ContactMapper;
import com.portfolio.repository.ContactRepository;
import com.portfolio.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final MailService mailService;
    private final ContactMapper contactMapper;

    public Page<ContactDTO> getAllContacts(Pageable pageable) {
        return contactRepository.findAll(pageable)
                .map(contactMapper::toDto);
    }

    @Transactional
    public ContactDTO createContact(ContactDTO dto) {
        Contact contact = contactMapper.toEntity(dto);
        contact = contactRepository.save(contact);
        return contactMapper.toDto(contact);
    }

    public void sendContactEmail(ContactDTO dto) {
        mailService.sendContactEmail(dto);
    }

    @Transactional
    public ContactDTO updateContact(Long id, ContactDTO dto) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contacto no encontrado con id: " + id));
        contact.setName(dto.getName());
        contact.setEmail(dto.getEmail());
        contact.setMessage(dto.getMessage());
        contact = contactRepository.save(contact);
        return contactMapper.toDto(contact);
    }

    @Transactional
    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
    }

}
