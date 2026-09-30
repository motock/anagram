package com.example.anagram;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Defines the 400 error-body contract of {@code POST /anagrams} as produced by
 * {@link GlobalExceptionHandler} and serialised through {@link ErrorResponse}.
 *
 * <p>Three cases are pinned:
 * <ul>
 *   <li>(a) {@code {"name":""}} violates {@code @NotBlank} &rarr; 400 with
 *       {@code $.status == 400} and {@code $.fieldErrors[0].field == "name"}.</li>
 *   <li>(b) {@code {}} leaves {@code name} null, which {@code @NotBlank} also
 *       rejects &rarr; 400 with {@code $.fieldErrors[0].field == "name"}.</li>
 *   <li>(c) a malformed JSON body with {@code Content-Type: application/json}
 *       &rarr; 400 with {@code $.message == "Malformed JSON request"}.</li>
 * </ul>
 *
 * <p>A fourth case issues two requests in sequence to prove the
 * {@code fieldErrors} list is rebuilt per call rather than accumulated on a
 * shared field/static.
 */
@SpringBootTest
class GlobalExceptionHandlerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    void blankNameReturns400WithNameFieldError() throws Exception {
        this.mockMvc.perform(post("/anagrams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
    }

    @Test
    void missingNameReturns400WithNameFieldError() throws Exception {
        this.mockMvc.perform(post("/anagrams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
    }

    @Test
    void malformedJsonReturns400WithMalformedJsonMessage() throws Exception {
        this.mockMvc.perform(post("/anagrams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));
    }

    @Test
    void fieldErrorsAreNotAccumulatedAcrossRequests() throws Exception {
        this.mockMvc.perform(post("/anagrams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()" ).value(1))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));

        this.mockMvc.perform(post("/anagrams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()" ).value(1))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
    }

    // --- 405: method not supported on an existing endpoint ---

    @Test
    void unsupportedMethodReturns405WithErrorResponseShape() throws Exception {
        this.mockMvc.perform(get("/anagrams"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                .andExpect(jsonPath("$.message").value("Method not allowed"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors").isEmpty())
                .andExpect(jsonPath("$.path").doesNotExist());
    }

    // --- 404: no handler for the requested path ---

    @Test
    void unknownEndpointReturns404WithErrorResponseShape() throws Exception {
        this.mockMvc.perform(post("/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"cat\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No such endpoint"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors").isEmpty())
                .andExpect(jsonPath("$.path").doesNotExist());
    }

    @Test
    void unknownEndpointReturns404ForGetToo() throws Exception {
        this.mockMvc.perform(get("/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No such endpoint"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors").isEmpty())
                .andExpect(jsonPath("$.path").doesNotExist());
    }

    @Test
    void unsupportedMethodOnUnknownPathStillReturns404Shape() throws Exception {
        this.mockMvc.perform(get("/no/such/endpoint"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No such endpoint"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors").isEmpty())
                .andExpect(jsonPath("$.path").doesNotExist());
    }

    // --- the documented 400 contract must be untouched by the new handlers ---

    @Test
    void existing400ContractIsUnchanged() throws Exception {
        this.mockMvc.perform(post("/anagrams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
                .andExpect(jsonPath("$.path").doesNotExist());
    }
}
