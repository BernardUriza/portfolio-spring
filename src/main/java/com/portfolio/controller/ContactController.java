package com.portfolio.controller;

import com.portfolio.dto.ContactDTO;
import com.portfolio.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Tag(name = "Contact API", description = "Gestiona los contactos del portafolio")
public class ContactController {

    private final ContactService contactService;

    @GetMapping
    @Operation(summary = "Listar todos los contactos")
    public ResponseEntity<List<ContactDTO>> getAllContacts() {
        return ResponseEntity.ok(contactService.getAllContacts());
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo contacto")
    public ResponseEntity<ContactDTO> createContact(@Valid @RequestBody ContactDTO dto) {
        return ResponseEntity.ok(contactService.createContact(dto));
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
