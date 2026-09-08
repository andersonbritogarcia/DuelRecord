package com.duelrecord.app.infrastructure.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtSecurityIntegrationTest {
    static final RSAKey KEY;
    static final HttpServer SERVER;
    static {
        try {
            KEY = new RSAKeyGenerator(2048).keyID("test-key").generate();
            SERVER = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            SERVER.createContext("/certs", exchange -> {
                byte[] body = new JWKSet(KEY.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                try (var output = exchange.getResponseBody()) { output.write(body); }
            });
            SERVER.start();
        } catch (Exception error) { throw new ExceptionInInitializerError(error); }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> "http://127.0.0.1:" + SERVER.getAddress().getPort() + "/certs");
    }
    @AfterAll static void stop() { SERVER.stop(0); }
    @Autowired JwtDecoder decoder;
    @Autowired WebApplicationContext context;

    String token(String issuer, String audience, Instant expiry, RSAKey key) throws Exception {
        var claims = new JWTClaimsSet.Builder().issuer(issuer).audience(audience).subject("test-user")
                .issueTime(Date.from(Instant.now().minusSeconds(600))).expirationTime(Date.from(expiry)).build();
        var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("test-key").build(), claims);
        jwt.sign(new RSASSASigner(key));
        return jwt.serialize();
    }

    @Test void verifiesSignatureIssuerAudienceAndExpiryUsingActualConfiguredDecoder() throws Exception {
        String issuer = "https://accounts.google.com";
        String audience = "test-client.apps.googleusercontent.com";
        Instant expiry = Instant.now().plusSeconds(300);
        assertEquals("test-user", decoder.decode(token(issuer, audience, expiry, KEY)).getSubject());
        assertThrows(JwtException.class, () -> decoder.decode(token("https://attacker.example", audience, expiry, KEY)));
        assertThrows(JwtException.class, () -> decoder.decode(token(issuer, "another-client", expiry, KEY)));
        assertThrows(JwtException.class, () -> decoder.decode(token(issuer, audience, Instant.now().minusSeconds(300), KEY)));
        var otherKey = new RSAKeyGenerator(2048).generate();
        assertThrows(JwtException.class, () -> decoder.decode(token(issuer, audience, expiry, otherKey)));
    }

    @Test void restrictsCorsToConfiguredOriginsWithoutCredentialCookies() throws Exception {
        var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        mvc.perform(options("/api/me").header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "GET").header("Access-Control-Request-Headers", "authorization,accept-language"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
        mvc.perform(options("/api/me").header("Origin", "https://attacker.example")
                .header("Access-Control-Request-Method", "GET")).andExpect(status().isForbidden());
    }
}
