package com.catalyst.user.integration;

import com.catalyst.shared.BaseIntegrationTest;
import com.catalyst.user.application.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("User Registration Integration Test")
class UserRegistrationIT extends BaseIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @DisplayName("Should successfully register a new user")
        void shouldRegisterNewUser() throws Exception {
                RegisterRequest request = new RegisterRequest(
                                "test@example.com",
                                "password123",
                                "Test User");

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.email").value("test@example.com"))
                                .andExpect(jsonPath("$.name").value("Test User"))
                                .andExpect(jsonPath("$.id").exists());
        }

        @Test
        @DisplayName("Should fail when email already exists")
        void shouldFailWhenEmailExists() throws Exception {
                RegisterRequest request = new RegisterRequest(
                                "duplicate@example.com",
                                "password123",
                                "Duplicate User");

                // First registration
                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());

                // Second registration with same email
                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict());
        }
}
