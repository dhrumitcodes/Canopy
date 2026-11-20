package com.societyhub.api.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societyhub.api.college.College;
import com.societyhub.api.college.CollegeRepository;
import com.societyhub.api.event.dto.EventRequest;
import com.societyhub.api.society.Society;
import com.societyhub.api.society.SocietyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EventControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private SocietyRepository societyRepository;
    @Autowired private CollegeRepository collegeRepository;
    @Autowired private ObjectMapper objectMapper;
    private Society savedSociety;

    @BeforeEach
    void setUp() {
        College defaultCollege = collegeRepository.findByEmailDomainIgnoreCase("unconfigured.invalid")
                .orElseThrow();
        savedSociety = societyRepository.save(Society.builder()
                .name("Robotics Society").description("Build bots").category("Technical")
                .college(defaultCollege).build());
    }

    @Test
    void getSocietyEvents_RequiresAnAuthenticatedUserFromThatCollege() throws Exception {
        mockMvc.perform(get("/api/v1/societies/" + savedSociety.getId() + "/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createEvent_WithoutAuth_ReturnsUnauthorized() throws Exception {
        EventRequest request = new EventRequest();
        request.setTitle("Build Drone");
        request.setDescription("Workshop");
        request.setLocation("Lab 4");
        request.setStartTime(LocalDateTime.now().plusDays(5));
        request.setEndTime(LocalDateTime.now().plusDays(5).plusHours(2));
        mockMvc.perform(post("/api/v1/societies/" + savedSociety.getId() + "/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}