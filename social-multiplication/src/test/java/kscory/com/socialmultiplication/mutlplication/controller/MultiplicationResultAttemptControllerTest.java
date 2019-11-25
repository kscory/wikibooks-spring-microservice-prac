package kscory.com.socialmultiplication.mutlplication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import kscory.com.socialmultiplication.mutlplication.domain.Multiplication;
import kscory.com.socialmultiplication.mutlplication.domain.MultiplicationResultAttempt;
import kscory.com.socialmultiplication.mutlplication.domain.User;
import kscory.com.socialmultiplication.mutlplication.service.MultiplicationService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(MultiplicationResultAttemptController.class)
class MultiplicationResultAttemptControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private MultiplicationService multiplicationService;

    @Test
    void postResultReturnCorrect() throws Exception {
        genericParameterizedTest(true);
    }

    @Test
    void postResultReturnNotCorrect() throws Exception {
        genericParameterizedTest(false);
    }

    void genericParameterizedTest(final boolean correct) throws Exception {
        // given
        given(multiplicationService.checkAttempt(any(MultiplicationResultAttempt.class)))
                .willReturn(correct);

        User user = new User("Cory");
        Multiplication multiplication = new Multiplication(50, 70);
        MultiplicationResultAttempt attempt = new MultiplicationResultAttempt(user, multiplication, 50 * 70, false);
        MultiplicationResultAttempt resultAttempt = new MultiplicationResultAttempt(user, multiplication, 50 * 70, correct);

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/results")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(attempt)))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(resultAttempt));
    }

    @Test
    void getUserStats() throws Exception {
        // given
        User user = new User("Cory");
        Multiplication multiplication = new Multiplication(50, 70);
        MultiplicationResultAttempt attempt = new MultiplicationResultAttempt(
                user, multiplication, 3500, true);
        List<MultiplicationResultAttempt> recentAttempts = Lists.newArrayList(attempt, attempt);
        given(multiplicationService
                .getStatsForUser("Cory"))
                .willReturn(recentAttempts);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/results")
                    .param("alias", "Cory"))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(recentAttempts));
    }

}