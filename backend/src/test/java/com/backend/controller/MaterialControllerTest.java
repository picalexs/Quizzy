// com/backend/MaterialControllerTest.java
package com.backend.controller;

import com.backend.config.MaterialTestConfig;
import com.backend.config.SecurityConfig;
import com.backend.model.Course;
import com.backend.model.Material;
import com.backend.service.MaterialService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MaterialController.class)
@Import({MaterialTestConfig.class, SecurityConfig.class})
class MaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private ObjectMapper objectMapper;

    private Material material;

    @BeforeEach
    void setUp() {
        Course course = new Course();
        course.setId(42L);

        material = new Material();
        material.setId(1L);
        material.setName("Test Material");
        material.setPath("/files/test.pdf");
        material.setCourse(course);
    }

    @Test
    void testGetAllMaterials() throws Exception {
        Mockito.when(materialService.getAllMaterials()).thenReturn(List.of(material));

        mockMvc.perform(get("/api/materials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(material.getId()));
    }

    @Test
    void testGetMaterialById() throws Exception {
        Mockito.when(materialService.getMaterialById(1L)).thenReturn(material);

        mockMvc.perform(get("/api/materials/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Material"));
    }

    @Test
    void testCreateMaterial() throws Exception {
        Mockito.when(materialService.createMaterial(any(Material.class))).thenReturn(material);

        mockMvc.perform(post("/api/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(material)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(material.getId()))
                .andExpect(jsonPath("$.path").value("/files/test.pdf"));
    }

    @Test
    void testUpdateMaterial() throws Exception {
        Material updated = new Material();
        updated.setName("Updated");
        updated.setPath("/files/updated.pdf");
        updated.setCourse(material.getCourse());
        Mockito.when(materialService.updateMaterial(anyLong(), any(Material.class))).thenReturn(updated);

        mockMvc.perform(put("/api/materials/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void testDeleteMaterial() throws Exception {
        mockMvc.perform(delete("/api/materials/1"))
                .andExpect(status().isNoContent());
        Mockito.verify(materialService).deleteMaterial(1L);
    }
}
