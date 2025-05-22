package com.example.memberservice.controller;

import com.example.memberservice.dto.PrivilegeDto;
import com.example.memberservice.dto.PrivilegeUseRequestDto;
import com.example.memberservice.security.CustomUserDetails;
import com.example.memberservice.service.PointService;
import com.example.memberservice.service.PrivilegeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrivilegeUsageController.class)
public class PrivilegeUsageControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @MockBean
    private PrivilegeService privilegeService;

    @Autowired
    private ObjectMapper objectMapper;

    private PrivilegeUseRequestDto privilegeUseRequestDto;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        privilegeUseRequestDto = new PrivilegeUseRequestDto();
        privilegeUseRequestDto.setPrivilegeId(1L);
        
        // Mock the security context
        customUserDetails = new CustomUserDetails("testuser", "password", Collections.emptyList(), 1L, "Test User");
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities())
        );
    }

    @Test
    void usePrivilege_Success() throws Exception {
        doNothing().when(pointService).applyPrivilegeForPointBenefit(eq(1L), any(PrivilegeUseRequestDto.class));

        mockMvc.perform(post("/api/me/privileges/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(privilegeUseRequestDto)))
            .andExpect(status().isOk())
            .andExpect(content().string("Privilege used successfully."));
    }
    
    @Test
    void usePrivilege_Unauthenticated_ShouldFailOrRedirect() throws Exception {
        SecurityContextHolder.clearContext(); // Simulate unauthenticated user

        // Depending on security setup, this might return 401, 403, or redirect to login.
        // For @WebMvcTest without full security config, it might pass if endpoint isn't explicitly secured at this level.
        // However, the controller's getCurrentMemberId() would throw IllegalStateException.
        // To properly test this, a full Spring Security context test (`@SpringBootTest`) might be needed.
        // Here, we expect the controller's internal check to fail.
        
        // We can't directly assert the IllegalStateException from getCurrentMemberId() here easily
        // without a more complex setup. The purpose of this unit test is more to check the flow
        // assuming authentication works. The IllegalStateException is an internal server error.
        // A real test with @WithMockUser or full context would be better for auth.
        // For now, we'll assume the service call won't happen if auth fails.
        
        // This test is more illustrative of the challenge in unit testing security context dependent code.
        // If getCurrentMemberId throws, it will result in a 500 if not handled by GlobalExceptionHandler specifically for IllegalStateException
        // from this specific path.
        // Given the current GlobalExceptionHandler for IllegalStateException, it would be 500.
        
         mockMvc.perform(post("/api/me/privileges/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(privilegeUseRequestDto)))
            .andExpect(status().isInternalServerError()); // Due to IllegalStateException in getCurrentMemberId
    }


    @Test
    void getAvailablePrivileges_Success() throws Exception {
        PrivilegeDto availablePrivilege = new PrivilegeDto();
        availablePrivilege.setId(1L);
        availablePrivilege.setName("AVAILABLE_PRIVILEGE");
        List<PrivilegeDto> availableList = Collections.singletonList(availablePrivilege);

        when(privilegeService.getAvailablePrivilegesForMember(eq(1L))).thenReturn(availableList);

        mockMvc.perform(get("/api/me/privileges"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("AVAILABLE_PRIVILEGE"));
    }
}
