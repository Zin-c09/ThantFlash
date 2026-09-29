package com.thantzin.thantflash;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Starts the whole app with an in-memory H2 DB and calls the REST API like a real client. */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @Test
    void fullStudyFlow() throws Exception {
        String token = register("thant");

        long deckId = id(call(token, post("/api/decks"), "{\"name\":\"JLPT N2\"}", 201));
        long cardId = id(call(token, post("/api/decks/" + deckId + "/cards"),
                "{\"front\":\"締切\",\"back\":\"しめきり — deadline\"}", 201));

        mvc.perform(auth(token, get("/api/decks")))
                .andExpect(jsonPath("$[0].cardCount").value(1))
                .andExpect(jsonPath("$[0].dueCount").value(1));

        mvc.perform(auth(token, get("/api/study/due")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].front").value("締切"));

        mvc.perform(auth(token, post("/api/cards/" + cardId + "/review"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"grade\":\"GOOD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intervalDays").value(1.0))
                .andExpect(jsonPath("$.reps").value(1));

        mvc.perform(auth(token, get("/api/study/due"))).andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(auth(token, get("/api/stats")))
                .andExpect(jsonPath("$.totalCards").value(1))
                .andExpect(jsonPath("$.dueNow").value(0))
                .andExpect(jsonPath("$.reviewedToday").value(1))
                .andExpect(jsonPath("$.accuracy").value(1.0))
                .andExpect(jsonPath("$.streakDays").value(1))
                .andExpect(jsonPath("$.last7Days", hasSize(7)));
    }

    @Test
    void usersCannotSeeEachOthersData() throws Exception {
        String alice = register("alice");
        String bob = register("bob");
        long deckId = id(call(alice, post("/api/decks"), "{\"name\":\"Private\"}", 201));
        long cardId = id(call(alice, post("/api/decks/" + deckId + "/cards"), "{\"front\":\"a\",\"back\":\"b\"}", 201));

        mvc.perform(auth(bob, get("/api/decks"))).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(auth(bob, get("/api/decks/" + deckId + "/cards"))).andExpect(status().isNotFound());
        mvc.perform(auth(bob, delete("/api/cards/" + cardId))).andExpect(status().isNotFound());
        mvc.perform(auth(bob, put("/api/cards/" + cardId)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"deckId\":" + deckId + ",\"front\":\"x\",\"back\":\"y\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void importTsvCreatesDecksAndSkipsDuplicates() throws Exception {
        String token = register("importer");
        String tsv = "締切\tしめきり\tN2\n検討\tけんとう\tN2\n締切\tdup\tN2\nhello\tမင်္ဂလာပါ\n";

        mvc.perform(auth(token, post("/api/import/tsv?deck=English")).contentType(MediaType.TEXT_PLAIN).content(tsv))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").value(3))
                .andExpect(jsonPath("$.skipped").value(1));

        mvc.perform(auth(token, get("/api/decks")))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("English"))
                .andExpect(jsonPath("$[1].name").value("N2"))
                .andExpect(jsonPath("$[1].cardCount").value(2));
    }

    @Test
    void authErrors() throws Exception {
        mvc.perform(get("/api/decks")).andExpect(status().isUnauthorized());

        register("taken");
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"taken\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"taken\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"x\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void duplicateDeckNameIsConflict() throws Exception {
        String token = register("dupdeck");
        call(token, post("/api/decks"), "{\"name\":\"Kanji\"}", 201);
        call(token, post("/api/decks"), "{\"name\":\"Kanji\"}", 409);
    }

    // ---------- helpers ----------

    private String register(String username) throws Exception {
        String body = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("token").asText();
    }

    private static MockHttpServletRequestBuilder auth(String token, MockHttpServletRequestBuilder req) {
        return req.header("Authorization", "Bearer " + token);
    }

    private JsonNode call(String token, MockHttpServletRequestBuilder req, String body, int expected) throws Exception {
        String res = mvc.perform(auth(token, req).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(expected))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        return json.readTree(res);
    }

    private static long id(JsonNode node) {
        return node.get("id").asLong();
    }
}
