package com.thantflash.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thantflash.reminder.ReminderRepository;
import com.thantflash.reminder.ReminderService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ReminderService reminderService;
    @Autowired ReminderRepository reminderRepository;

    String token;

    @BeforeEach
    void registerFreshUser() throws Exception {
        token = register("user-" + UUID.randomUUID() + "@example.com");
    }

    @Test
    void protectedEndpointsRequireToken() throws Exception {
        mvc.perform(get("/api/decks")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginReturnsTokenAndRejectsWrongPassword() throws Exception {
        String email = "login-" + UUID.randomUUID() + "@example.com";
        register(email);
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateRegistrationIsConflict() throws Exception {
        String email = "dup-" + UUID.randomUUID() + "@example.com";
        register(email);
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(email)))
                .andExpect(status().isConflict());
    }

    @Test
    void validationErrorsAreProblemDetails() throws Exception {
        mvc.perform(auth(post("/api/decks")).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void studyFlowSchedulesCardAndRecordsStats() throws Exception {
        long deckId = createDeck("English");
        long cardId = id(mvc.perform(auth(post("/api/decks/" + deckId + "/cards"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"front\":\"Hello\",\"back\":\"မင်္ဂလာပါ\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        mvc.perform(auth(get("/api/decks")))
                .andExpect(jsonPath("$[0].cardCount").value(1))
                .andExpect(jsonPath("$[0].dueCount").value(1));
        mvc.perform(auth(get("/api/study/due"))).andExpect(jsonPath("$", hasSize(1)));

        String reviewed = mvc.perform(auth(post("/api/cards/" + cardId + "/review"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"grade\":\"GOOD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intervalDays").value(1.0))
                .andExpect(jsonPath("$.reps").value(1))
                .andReturn().getResponse().getContentAsString();
        Instant due = Instant.parse(json.readTree(reviewed).get("dueAt").asText());
        assertThat(due).isAfter(Instant.now().plus(23, ChronoUnit.HOURS));

        mvc.perform(auth(get("/api/study/due"))).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(auth(get("/api/stats")))
                .andExpect(jsonPath("$.reviewsToday").value(1))
                .andExpect(jsonPath("$.currentStreak").value(1))
                .andExpect(jsonPath("$.retentionRate30d").value(1.0))
                .andExpect(jsonPath("$.last30Days", hasSize(30)));
    }

    @Test
    void cardListSearchesAndPaginates() throws Exception {
        long deckId = createDeck("Search");
        for (String front : new String[] {"Apple", "Banana", "Pineapple"}) {
            mvc.perform(auth(post("/api/decks/" + deckId + "/cards")).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"front\":\"" + front + "\",\"back\":\"fruit\"}")).andExpect(status().isCreated());
        }
        mvc.perform(auth(get("/api/decks/" + deckId + "/cards?size=2")))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
        mvc.perform(auth(get("/api/decks/" + deckId + "/cards?q=APPLE")))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void usersCannotSeeEachOthersData() throws Exception {
        long deckId = createDeck("Private");
        String other = register("other-" + UUID.randomUUID() + "@example.com");

        mvc.perform(get("/api/decks/" + deckId + "/cards").header("Authorization", "Bearer " + other))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/decks/" + deckId).header("Authorization", "Bearer " + other))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/decks").header("Authorization", "Bearer " + other))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void tsvImportCreatesDecksAndSkipsDuplicates() throws Exception {
        String tsv = "勉強\tべんきょう<br>study\tJLPT N2\nHello\tမင်္ဂလာပါ\nbroken line\n";
        MockMultipartFile file = new MockMultipartFile("file", "cards.tsv", "text/tab-separated-values",
                tsv.getBytes(StandardCharsets.UTF_8));

        mvc.perform(auth(multipart("/api/import/tsv").file(file)))
                .andExpect(jsonPath("$.imported").value(2))
                .andExpect(jsonPath("$.invalidLines").value(1));
        mvc.perform(auth(multipart("/api/import/tsv").file(file)))
                .andExpect(jsonPath("$.imported").value(0))
                .andExpect(jsonPath("$.skippedDuplicates").value(2));
        mvc.perform(auth(get("/api/decks")))
                .andExpect(jsonPath("$[*].name", org.hamcrest.Matchers.containsInAnyOrder("Imported", "JLPT N2")));
    }

    @Test
    void dueRemindersFireOnceAndRepeatingOnesRollForward() throws Exception {
        String past = Instant.now().minus(1, ChronoUnit.HOURS).toString();
        long once = id(createReminder("Study N2 kanji", past, "NONE"));
        long daily = id(createReminder("Daily review", past, "DAILY"));

        reminderService.fireDue();

        assertThat(reminderRepository.findById(once).orElseThrow().isDone()).isTrue();
        var rolled = reminderRepository.findById(daily).orElseThrow();
        assertThat(rolled.isDone()).isFalse();
        assertThat(rolled.getFireAt()).isAfter(Instant.now());
    }

    // ---------- helpers ----------

    private String register(String email) throws Exception {
        String body = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("accessToken").asText();
    }

    private static String registerBody(String email) {
        return "{\"email\":\"" + email + "\",\"password\":\"password123\",\"displayName\":\"Thant Zin\"}";
    }

    private long createDeck(String name) throws Exception {
        return id(mvc.perform(auth(post("/api/decks")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private String createReminder(String title, String fireAt, String repeat) throws Exception {
        return mvc.perform(auth(post("/api/reminders")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"fireAt\":\"" + fireAt + "\",\"repeat\":\"" + repeat + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private long id(String responseBody) throws Exception {
        JsonNode node = json.readTree(responseBody);
        return node.get("id").asLong();
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder req) {
        return req.header("Authorization", "Bearer " + token);
    }
}
