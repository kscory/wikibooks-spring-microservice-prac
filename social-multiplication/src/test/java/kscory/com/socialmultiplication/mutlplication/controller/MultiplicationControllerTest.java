package kscory.com.socialmultiplication.mutlplication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kscory.com.socialmultiplication.mutlplication.domain.Multiplication;
import kscory.com.socialmultiplication.mutlplication.service.MultiplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(MultiplicationController.class)
class MultiplicationControllerTest {

    @MockBean
    private MultiplicationService multiplicationService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

    }

    @Test
    void getRandomMultiplicationTest() throws Exception {
        // given
        Multiplication multiplication = new Multiplication(70, 20);
        given(multiplicationService.createRandomMultiplication())
                .willReturn(multiplication);

        // when
        MockHttpServletResponse response = mockMvc.perform(get("/multiplications/random")
                    .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(multiplication));
    }
}