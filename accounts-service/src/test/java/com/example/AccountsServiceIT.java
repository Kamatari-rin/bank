//package com.example;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureMockMvc
//@Testcontainers
//@TestPropertySource(properties = {
//        "spring.liquibase.enabled=false",
//        "spring.jpa.hibernate.ddl-auto=none",
//        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9999/realms/test" // заглушка, нам важен только jwt() в тесте
//})
//class AccountsServiceIT {
//
//    @Container
//    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16")
//            .withDatabaseName("bank")
//            .withUsername("postgres")
//            .withPassword("555666");
//
//    @DynamicPropertySource
//    static void props(DynamicPropertyRegistry r) {
//        r.add("spring.datasource.url", () -> pg.getJdbcUrl() + "&currentSchema=accounts");
//        r.add("spring.datasource.username", pg::getUsername);
//        r.add("spring.datasource.password", pg::getPassword);
//    }
//
//    @Autowired MockMvc mvc;
//
//    @Test
//    void me_requires_auth() throws Exception {
//        mvc.perform(get("/api/accounts/me")).andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    void me_with_jwt_but_no_user_returns_404() throws Exception {
//        mvc.perform(get("/api/accounts/me").with(jwt().jwt(j -> j.claim("sub", "00000000-0000-0000-0000-000000000000"))))
//                .andExpect(status().isNotFound());
//    }
//}