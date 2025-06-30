package com.portfolio.controller;

import com.portfolio.dto.ContactDTO;
import com.portfolio.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Tag(name = "Contact API", description = "Gestiona los contactos del portafolio")
public class ContactController {

    private final ContactService contactService;

    @GetMapping
    @Operation(summary = "Listar todos los contactos")
    public ResponseEntity<Page<ContactDTO>> getAllContacts(Pageable pageable) {
        return ResponseEntity.ok(contactService.getAllContacts(pageable));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo contacto")
    public ResponseEntity<ContactDTO> createContact(@Valid @RequestBody ContactDTO dto) {
        return ResponseEntity.ok(contactService.createContact(dto));
    }

    @PostMapping("/send")
    @Operation(summary = "Enviar un mensaje de contacto por email")
    public ResponseEntity<Void> sendContactEmail(@Valid @RequestBody ContactDTO dto) {
        contactService.sendContactEmail(dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un contacto por ID")
    public ResponseEntity<ContactDTO> updateContact(@PathVariable Long id, @Valid @RequestBody ContactDTO dto) {
        return ResponseEntity.ok(contactService.updateContact(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un contacto por ID")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
        return ResponseEntity.noContent().build();
    }
}
