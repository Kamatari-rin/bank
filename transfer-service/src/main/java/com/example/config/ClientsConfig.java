package com.example.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class ClientsConfig {

    private final ClientRegistrationRepository registrations;
    private final OAuth2AuthorizedClientService clientService;

    @Value("${gateway.base-url}")
    private String gatewayBaseUrl;

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager() {
        var provider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();
        var m = new AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations, clientService);
        m.setAuthorizedClientProvider(provider);
        return m;
    }

    @Bean
    public WebClient gatewayWebClient(@Value("${gateway.base-url:http://gateway:8080}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(512 * 1024)).build())
                .build();
    }
}
