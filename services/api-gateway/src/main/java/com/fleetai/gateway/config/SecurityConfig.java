package com.fleetai.gateway.config;

import com.fleetai.gateway.filter.AuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the JWT authentication gateway filter as a global filter.
 * NOTE: this is a lightweight, shared-secret HMAC validator suitable for a scaffold /
 * internal demo. For a real production rollout, swap AuthenticationFilter's validation
 * for OAuth2/OIDC token introspection against the client's identity provider.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public AuthenticationFilter authenticationFilter() {
        return new AuthenticationFilter();
    }
}
