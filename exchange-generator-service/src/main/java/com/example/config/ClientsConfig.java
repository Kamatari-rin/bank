package com.example.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class ClientsConfig {

    private final ClientRegistrationRepository registrations;
    private final OAuth2AuthorizedClientService clients;

    @Value("${exchange.base-url}")
    private String exchangeBase;

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager() {
        var provider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();
        var m = new AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations, clients);
        m.setAuthorizedClientProvider(provider);
        return m;
    }

    @Bean
    public WebClient gatewayWebClient() {
        return WebClient.builder().baseUrl(exchangeBase).build();
    }
}
