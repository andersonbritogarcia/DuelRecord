package com.duelrecord.app.identity.web.controller;

import com.duelrecord.app.identity.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import(MeControllerIntegrationTest.TestSecurityConfig.class)
class MeControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public JwtDecoder jwtDecoder() {
            return mock(JwtDecoder.class);
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        userRepository.deleteAll();
    }

    @Test
    void shouldReturn401WhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCreateAndReturnUserWhenValidJwtTokenProvided() throws Exception {
        var googleSub = "google-sub-integration-test-1";
        var email = "anderson@duelrecord.com";

        mockMvc.perform(get("/api/me")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(googleSub)
                                .claim("email", email)
                        ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.authProviderId").value(googleSub))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        assertTrue(userRepository.findByAuthProviderId(googleSub).isPresent());
        assertEquals(email, userRepository.findByAuthProviderId(googleSub).get().getEmail());
    }

    @Test
    void shouldReturnExistingUserWhenSubAlreadyExists() throws Exception {
        var googleSub = "google-sub-integration-test-2";
        var email = "returning_player@duelrecord.com";

        // First call creates
        mockMvc.perform(get("/api/me")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(googleSub)
                                .claim("email", email)
                        )))
                .andExpect(status().isOk());

        assertEquals(1, userRepository.count());

        // Second call retrieves without duplicating
        mockMvc.perform(get("/api/me")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(googleSub)
                                .claim("email", email)
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.authProviderId").value(googleSub));

        assertEquals(1, userRepository.count());
    }

    @ParameterizedTest
    @CsvSource({
            "en,                Invalid JWT claims: missing subject or email",
            "pt-BR,             Claims do JWT inválidas: sub ou email ausentes",
            "pt,                Claims do JWT inválidas: sub ou email ausentes",
            "pt-PT,             Claims do JWT inválidas: sub ou email ausentes",
            "fr,                Revendications JWT invalides : sub ou email manquant",
    })
    void shouldReturnLocalizedErrorDetailAccordingToAcceptLanguageHeader(String acceptLanguage, String expectedDetail) throws Exception {
        mockMvc.perform(get("/api/me")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguage)
                        .with(jwt().jwt(jwt -> jwt.subject("google-sub-missing-email"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value(expectedDetail));
    }

    @Test
    void shouldFallBackToEnglishWhenAcceptLanguageIsMissingOrUnsupported() throws Exception {
        mockMvc.perform(get("/api/me")
                        .with(jwt().jwt(jwt -> jwt.subject("google-sub-missing-email"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid JWT claims: missing subject or email"));

        mockMvc.perform(get("/api/me")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "es")
                        .with(jwt().jwt(jwt -> jwt.subject("google-sub-missing-email"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid JWT claims: missing subject or email"));
    }
}
