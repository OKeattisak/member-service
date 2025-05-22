package com.example.memberservice.controller;

import com.example.memberservice.dto.PrivilegeDto;
import com.example.memberservice.service.PrivilegeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrivilegeController.class)
@ExtendWith(MockitoExtension.class) // Not strictly necessary with @WebMvcTest but good practice
public class PrivilegeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrivilegeService privilegeService;

    @Autowired
    private ObjectMapper objectMapper;

    private PrivilegeDto privilegeDto;

    @BeforeEach
    void setUp() {
        privilegeDto = new PrivilegeDto();
        privilegeDto.setId(1L);
        privilegeDto.setName("TEST_PRIVILEGE");
        privilegeDto.setDescription("Test Description");
        privilegeDto.setMinMemberLevel("BRONZE");
        privilegeDto.setActive(true);
    }

    @Test
    void createPrivilege_Success() throws Exception {
        when(privilegeService.createPrivilege(any(PrivilegeDto.class))).thenReturn(privilegeDto);

        mockMvc.perform(post("/api/admin/privileges")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(privilegeDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(privilegeDto.getName()));
    }

    @Test
    void updatePrivilege_Success() throws Exception {
        when(privilegeService.updatePrivilege(eq(1L), any(PrivilegeDto.class))).thenReturn(privilegeDto);

        mockMvc.perform(put("/api/admin/privileges/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(privilegeDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(privilegeDto.getName()));
    }

    @Test
    void deletePrivilege_Success() throws Exception {
        doNothing().when(privilegeService).deletePrivilege(1L);

        mockMvc.perform(delete("/api/admin/privileges/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void getPrivilege_Success() throws Exception {
        when(privilegeService.getPrivilege(1L)).thenReturn(privilegeDto);

        mockMvc.perform(get("/api/admin/privileges/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(privilegeDto.getName()));
    }

    @Test
    void getAllPrivileges_Success() throws Exception {
        Page<PrivilegeDto> page = new PageImpl<>(Collections.singletonList(privilegeDto), PageRequest.of(0, 10), 1);
        when(privilegeService.getAllPrivileges(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/admin/privileges")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value(privilegeDto.getName()));
    }
}
