package com.portfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.dto.ContactDTO;
import com.portfolio.service.ContactService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllContacts() throws Exception {
        Mockito.when(contactService.getAllContacts()).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/api/contact"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void testCreateContact() throws Exception {
        ContactDTO dto = new ContactDTO(null, "John Doe", "john@example.com", "Hello");
        ContactDTO saved = new ContactDTO(1L, "John Doe", "john@example.com", "Hello");

        Mockito.when(contactService.createContact(any())).thenReturn(saved);

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testUpdateContact() throws Exception {
        ContactDTO dto = new ContactDTO(1L, "John Smith", "john@example.com", "Hi");

        Mockito.when(contactService.updateContact(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/contact/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Smith"));
    }

    @Test
    void testDeleteContact() throws Exception {
        Mockito.doNothing().when(contactService).deleteContact(1L);

        mockMvc.perform(delete("/api/contact/1"))
                .andExpect(status().isOk());
    }
}
