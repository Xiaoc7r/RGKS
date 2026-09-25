package com.jlu.registration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SevenModuleIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void module1AuthenticatesAndEnforcesRoleBoundaries() throws Exception {
        mvc.perform(post("/api/auth/login").with(httpBasic("student1", "Student123!")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("STUDENT"));
        mvc.perform(get("/api/people/students").with(student()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/people/students").with(registrar()))
                .andExpect(status().isOk());
    }

    @Test
    void module2CompletesPeopleCrudAndMasksIdentity() throws Exception {
        String create = """
                {"studentNumber":"20269999","name":"测试学生","dateOfBirth":"2006-09-09",
                 "identityNumber":"110101200609090099","status":"ACTIVE","major":"软件工程",
                 "graduationDate":"2030-06-30"}
                """;
        String json = mvc.perform(post("/api/people/students").with(registrar())
                        .contentType(MediaType.APPLICATION_JSON).content(create))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.maskedIdentityNumber").value("****0099"))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(json).get("id").asLong();
        String update = """
                {"studentNumber":"20269999","name":"测试学生（已修改）","dateOfBirth":"2006-09-09",
                 "identityNumber":"","status":"ON_LEAVE","major":"软件工程",
                 "graduationDate":"2031-06-30"}
                """;
        mvc.perform(put("/api/people/students/{id}", id).with(registrar())
                        .contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ON_LEAVE"));
        mvc.perform(delete("/api/people/students/{id}", id).with(registrar()))
                .andExpect(status().isNoContent());
    }

    @Test
    void module3ExposesReadOnlyLegacyCatalogAdapter() throws Exception {
        mvc.perform(get("/api/catalog/status").with(student()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.readOnly").value(true));
        mvc.perform(get("/api/catalog/courses").with(student()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(7));
        mvc.perform(get("/api/catalog/offerings").param("semester", "2026-FALL").with(student()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].remainingSeats").isNumber());
    }

    @Test
    void module4CreatesUpdatesSubmitsAndDeletesSchedule() throws Exception {
        Map<String, Long> ids = catalogIds();
        addSelection(ids.get("CS201"), "PRIMARY", 1);
        addSelection(ids.get("SE201"), "PRIMARY", 2);
        addSelection(ids.get("DB201"), "PRIMARY", 3);
        addSelection(ids.get("WEB201"), "PRIMARY", 4);
        addSelection(ids.get("NET201"), "ALTERNATE", 1);
        String last = addSelection(ids.get("SE301"), "ALTERNATE", 2);
        long alternateId = findItemId(objectMapper.readTree(last), "SE301");
        mvc.perform(put("/api/registrations/selections/{id}", alternateId)
                        .param("semester", "2026-FALL").with(student())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"choiceType\":\"ALTERNATE\",\"priority\":2}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/registrations/save").param("semester", "2026-FALL").with(student()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DRAFT"));
        mvc.perform(post("/api/registrations/submit").param("semester", "2026-FALL").with(student()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.items.length()").value(6));
        mvc.perform(delete("/api/registrations/my-schedule").param("semester", "2026-FALL").with(student()))
                .andExpect(status().isNoContent());
    }

    @Test
    void module5SelectsAndDeselectsEligibleTeachingOffering() throws Exception {
        long networkId = catalogIds().get("NET201");
        mvc.perform(get("/api/teaching/eligible-offerings").param("semester", "2026-FALL").with(professor()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4));
        mvc.perform(post("/api/teaching/offerings/{id}/select", networkId).with(professor()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ownedByMe").value(true));
        mvc.perform(delete("/api/teaching/offerings/{id}/selection", networkId).with(professor()))
                .andExpect(status().isOk());
    }

    @Test
    void module6GradesClosedOfferingAndBuildsReportCard() throws Exception {
        String offerings = mvc.perform(get("/api/grading/my-offerings")
                        .param("semester", "2026-SPRING").with(professor()))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("CLOSED"))
                .andReturn().getResponse().getContentAsString();
        long offeringId = objectMapper.readTree(offerings).get(0).get("offeringId").asLong();
        String roster = mvc.perform(get("/api/grading/offerings/{id}/roster", offeringId).with(professor()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andReturn().getResponse().getContentAsString();
        long studentId = objectMapper.readTree(roster).get(0).get("studentId").asLong();
        mvc.perform(put("/api/grading/grades").with(professor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GradeInput(offeringId, studentId, "B"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.gradeValue").value("B"));
        mvc.perform(get("/api/grading/my-report-card").param("semester", "2026-SPRING").with(student()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.gradePointAverage").value(3.00))
                .andExpect(jsonPath("$.items[0].gradeValue").value("B"));
    }

    @Test
    void module7ClosesRegistrationBillsAndRetriesFailure() throws Exception {
        Map<String, Long> ids = catalogIds();
        addSelection(ids.get("CS201"), "PRIMARY", 1);
        addSelection(ids.get("SE201"), "PRIMARY", 2);
        addSelection(ids.get("DB201"), "PRIMARY", 3);
        addSelection(ids.get("WEB201"), "PRIMARY", 4);
        addSelection(ids.get("NET201"), "ALTERNATE", 1);
        addSelection(ids.get("SE301"), "ALTERNATE", 2);
        mvc.perform(post("/api/registrations/submit").param("semester", "2026-FALL").with(student()))
                .andExpect(status().isOk());
        mvc.perform(post("/api/operations/close-registration")
                        .param("semester", "2026-FALL").with(registrar()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cancelledOfferingCount").value(2))
                .andExpect(jsonPath("$.finalizedScheduleCount").value(3))
                .andExpect(jsonPath("$.billingCount").value(3));
        String billingJson = mvc.perform(get("/api/operations/billing")
                        .param("semester", "2026-FALL").with(registrar()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3))
                .andReturn().getResponse().getContentAsString();
        long billingId = objectMapper.readTree(billingJson).get(0).get("id").asLong();
        mvc.perform(post("/api/operations/billing/{id}/attempt", billingId)
                        .param("success", "false").with(registrar()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.retryCount").value(1));
        mvc.perform(post("/api/operations/billing/{id}/attempt", billingId)
                        .param("success", "true").with(registrar()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.retryCount").value(2));
    }

    private Map<String, Long> catalogIds() throws Exception {
        String json = mvc.perform(get("/api/catalog/offerings").param("semester", "2026-FALL").with(student()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Map<String, Long> result = new HashMap<>();
        objectMapper.readTree(json).forEach(node -> result.put(node.get("courseCode").asText(), node.get("offeringId").asLong()));
        return result;
    }

    private String addSelection(Long offeringId, String choiceType, int priority) throws Exception {
        return mvc.perform(post("/api/registrations/selections").with(student())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new Selection("2026-FALL", offeringId, choiceType, priority))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    }

    private long findItemId(JsonNode schedule, String courseCode) {
        for (JsonNode item : schedule.get("items")) {
            if (courseCode.equals(item.get("courseCode").asText())) return item.get("itemId").asLong();
        }
        throw new IllegalStateException("测试课表中未找到课程 " + courseCode);
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor student() {
        return httpBasic("student1", "Student123!");
    }
    private org.springframework.test.web.servlet.request.RequestPostProcessor professor() {
        return httpBasic("professor1", "Professor123!");
    }
    private org.springframework.test.web.servlet.request.RequestPostProcessor registrar() {
        return httpBasic("registrar", "Registrar123!");
    }

    record Selection(String semester, Long offeringId, String choiceType, Integer priority) {}
    record GradeInput(Long offeringId, Long studentId, String gradeValue) {}
}
