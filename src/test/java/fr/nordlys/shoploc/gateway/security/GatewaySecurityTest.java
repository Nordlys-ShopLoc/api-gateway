package fr.nordlys.shoploc.gateway.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(GatewaySecurityTest.FakeServiceController.class)
class GatewaySecurityTest {

    // same key as in src/test/resources/application.properties
    private static final String SECRET = "local-test-secret-never-used-in-production";

    // pretends to be a service: returns the identity sent by the gateway
    @RestController
    static class FakeServiceController {

        @GetMapping("/api/v1/test/identity")
        String identity(@RequestHeader(value = "X-User-Id", required = false) String userId,
                        @RequestHeader(value = "X-User-Role", required = false) String role) {
            return userId + " " + role;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    private String createToken(String secret, String userId, String role) {
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("service-accounts")
                .subject(userId)
                .claim("role", role)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return new NimbusJwtEncoder(new ImmutableSecret<>(key))
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }

    // refuses a request without token
    @Test
    void refusesRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/test/identity"))
                .andExpect(status().isUnauthorized());
    }

    // refuses a token signed with another key
    @Test
    void refusesFakeToken() throws Exception {
        String fakeToken = createToken("another-secret-of-at-least-32-characters", "42", "ADMIN");

        mockMvc.perform(get("/api/v1/test/identity").header("Authorization", "Bearer " + fakeToken))
                .andExpect(status().isUnauthorized());
    }

    // sends the id and the role of the token to the service
    @Test
    void sendsIdentityToService() throws Exception {
        String token = createToken(SECRET, "42", "ADMIN");

        mockMvc.perform(get("/api/v1/test/identity").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("42 ADMIN"));
    }

    // the client cannot send its own identity
    @Test
    void replacesIdentitySentByClient() throws Exception {
        String token = createToken(SECRET, "42", "CLIENT");

        mockMvc.perform(get("/api/v1/test/identity")
                        .header("Authorization", "Bearer " + token)
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(content().string("42 CLIENT"));
    }
}
