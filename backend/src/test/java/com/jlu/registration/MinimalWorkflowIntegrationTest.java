package com.jlu.registration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MinimalWorkflowIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void roleBoundariesAndCompleteMinimalWorkflow() throws Exception {
        mvc.perform(post("/api/auth/login").with(httpBasic("student1", "Student123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("STUDENT"));

        mvc.perform(get("/api/people/students").with(httpBasic("student1", "Student123!")))
                .andExpect(status().isForbidden());

        String catalogJson = mvc.perform(get("/api/catalog/offerings")
                        .param("semester", "2026-FALL")
                        .with(httpBasic("student1", "Student123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andReturn().getResponse().getContentAsString();
        JsonNode catalog = objectMapper.readTree(catalogJson);
        List<Long> offeringIds = new ArrayList<>();
        catalog.forEach(item -> offeringIds.add(item.get("offeringId").asLong()));

        for (int index = 0; index < offeringIds.size(); index++) {
            String choiceType = index < 4 ? "PRIMARY" : "ALTERNATE";
            int priority = index < 4 ? index + 1 : index - 3;
            String body = objectMapper.writeValueAsString(new Selection(
                    "2026-FALL", offeringIds.get(index), choiceType, priority));
            mvc.perform(post("/api/registrations/selections")
                            .with(httpBasic("student1", "Student123!"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());
        }

        mvc.perform(post("/api/registrations/submit")
                        .param("semester", "2026-FALL")
                        .with(httpBasic("student1", "Student123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.items.length()").value(6));

        String teachingJson = mvc.perform(get("/api/teaching/my-offerings")
                        .param("semester", "2026-FALL")
                        .with(httpBasic("professor1", "Professor123!")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode ownedOffering = objectMapper.readTree(teachingJson).get(0);
        long offeringId = ownedOffering.get("offeringId").asLong();

        String rosterJson = mvc.perform(get("/api/teaching/offerings/{id}/roster", offeringId)
                        .with(httpBasic("professor1", "Professor123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn().getResponse().getContentAsString();
        long studentId = objectMapper.readTree(rosterJson).get(0).get("studentId").asLong();

        mvc.perform(put("/api/teaching/grades")
                        .with(httpBasic("professor1", "Professor123!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GradeInput(offeringId, studentId, "A"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradeValue").value("A"));

        mvc.perform(post("/api/operations/close-registration")
                        .param("semester", "2026-FALL")
                        .with(httpBasic("registrar", "Registrar123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offeringCount").value(6))
                .andExpect(jsonPath("$.finalizedScheduleCount").value(1));

        mvc.perform(get("/api/operations/billing")
                        .param("semester", "2026-FALL")
                        .with(httpBasic("registrar", "Registrar123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    record Selection(String semester, Long offeringId, String choiceType, Integer priority) {
    }

    record GradeInput(Long offeringId, Long studentId, String gradeValue) {
    }
}
