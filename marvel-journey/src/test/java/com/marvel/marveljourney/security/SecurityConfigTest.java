package com.marvel.marveljourney.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // @Test
    // public void testPublicEndpoint() throws Exception {
    //     mockMvc.perform(get("/auth/login"))
    //             .andExpect(status().isOk());
    // }

    // @Test
    // @WithMockUser(roles = "USER")
    // public void testUserEndpointWithUserRole() throws Exception {
    //     mockMvc.perform(get("/user/profile").with(csrf()))
    //             .andExpect(status().isOk());
    // }

    // @Test
    // @WithMockUser(roles = "ADMIN")
    // public void testAdminEndpointWithAdminRole() throws Exception {
    //     mockMvc.perform(get("/admin/dashboard").with(csrf()))
    //             .andExpect(status().isOk());
    // }

    // @Test
    // @WithMockUser(roles = "USER")
    // public void testAdminEndpointWithUserRole() throws Exception {
    //     mockMvc.perform(get("/admin/dashboard").with(csrf()))
    //             .andExpect(status().isForbidden());
    // }
}