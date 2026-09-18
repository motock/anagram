package com.example.anagram;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AnagramEndToEndIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Happy path: returns sorted anagrams excluding input")
    void happyPath() throws Exception {
        mockMvc.perform(post("/anagrams")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"listen\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.anagrams", containsInAnyOrder("enlist", "inlets", "silent", "tinsel")))
                .andExpect(jsonPath("$.anagrams", not(hasItem("listen"))));
    }

    @Test
    @DisplayName("Normalization: ignores case and punctuation")
    void normalization() throws Exception {
        mockMvc.perform(post("/anagrams")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"LiStEn!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anagrams", containsInAnyOrder("enlist", "inlets", "silent", "tinsel")))
                .andExpect(jsonPath("$.anagrams", not(hasItem("listen"))));
    }

    @Test
    @DisplayName("No matches: empty list")
    void noMatches() throws Exception {
        mockMvc.perform(post("/anagrams")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"zzz\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anagrams", hasSize(0)));
    }

    @Test
    @DisplayName("Blank input: validation error")
    void blankInput() throws Exception {
        mockMvc.perform(post("/anagrams")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors[0].field", is("name")));
    }

    @Test
    @DisplayName("Missing field: validation error")
    void missingField() throws Exception {
        mockMvc.perform(post("/anagrams")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field", is("name")));
    }

    @Test
    @DisplayName("Malformed JSON: error message")
    void malformedJson() throws Exception {
        mockMvc.perform(post("/anagrams")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Malformed JSON request")));
    }

    @Test
    @DisplayName("Alphabetical ordering of anagrams")
    void alphabeticalOrdering() throws Exception {
        mockMvc.perform(post("/anagrams")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anagrams", contains("amen", "mane", "mean")));
    }
}
