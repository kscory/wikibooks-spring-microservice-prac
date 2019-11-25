package kscory.com.socialmultiplication.mutlplication.service;

import kscory.com.socialmultiplication.mutlplication.domain.Multiplication;
import kscory.com.socialmultiplication.mutlplication.domain.MultiplicationResultAttempt;
import kscory.com.socialmultiplication.mutlplication.domain.User;
import kscory.com.socialmultiplication.mutlplication.event.EventDispatcher;
import kscory.com.socialmultiplication.mutlplication.event.MultiplicationSolvedEvent;
import kscory.com.socialmultiplication.mutlplication.repository.MultiplicationResultAttemptRepository;
import kscory.com.socialmultiplication.mutlplication.repository.UserRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class MultiplicationServiceImplTest {

    private MultiplicationServiceImpl multiplicationService;

    @Mock
    private RandomGeneratorService randomGeneratorService;

    @Mock
    private MultiplicationResultAttemptRepository attemptRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventDispatcher eventDispatcher;

    @BeforeEach
    void setUp() {
        // 목 객체를 초기화
        MockitoAnnotations.initMocks(this);
        multiplicationService = new MultiplicationServiceImpl(randomGeneratorService,
                attemptRepository,
                userRepository,
                eventDispatcher);
    }

    @Test
    void createRandomMultiplicationTest() {
        // given (randomGeneratorService 가 처음에 50, 나중에 30을 반환하도록 설정)
        given(randomGeneratorService.generateRandomFactor())
                .willReturn(50, 30);

        // when
        Multiplication multiplication = multiplicationService.createRandomMultiplication();

        // assert
        assertThat(multiplication.getFactorA()).isEqualTo(50);
        assertThat(multiplication.getFactorB()).isEqualTo(30);
    }

    @Test
    void checkCorrectAttemptTest() {
        // given
        Multiplication multiplication = new Multiplication(50, 60);
        User user = new User("Cory");
        MultiplicationResultAttempt attempt = new MultiplicationResultAttempt(user,
                multiplication,
                50 * 60,
                false);
        MultiplicationResultAttempt verifiedAttempt = new MultiplicationResultAttempt(user,
                multiplication,
                50 * 60,
                true);
        MultiplicationSolvedEvent event = new MultiplicationSolvedEvent(attempt.getId(), attempt.getUser().getId(), true);

        given(userRepository.findByAlias("Cory"))
                .willReturn(Optional.empty());

        // when
        boolean attemptResult = multiplicationService.checkAttempt(attempt);

        // then
        assertThat(attemptResult).isTrue();
        verify(attemptRepository).save(verifiedAttempt);
        verify(eventDispatcher).send(eq(event));
    }

    @Test
    void checkWrongAttemptTest() {
        // given
        Multiplication multiplication = new Multiplication(50, 60);
        User user = new User("Cory");
        MultiplicationResultAttempt attempt = new MultiplicationResultAttempt(user,
                multiplication,
                50 * 50,
                false);
        MultiplicationSolvedEvent event = new MultiplicationSolvedEvent(attempt.getId(),
                attempt.getUser().getId(), false);
        given(userRepository.findByAlias("Cory"))
                .willReturn(Optional.empty());

        // when
        boolean attemptResult = multiplicationService.checkAttempt(attempt);

        // then
        assertThat(attemptResult).isFalse();
        verify(attemptRepository).save(attempt);
        verify(eventDispatcher).send(eq(event));
    }

    @Test
    void retrieveStatsTest() {
        // given
        Multiplication multiplication = new Multiplication(50, 60);
        User user = new User("Cory");
        MultiplicationResultAttempt attempt1 = new MultiplicationResultAttempt(user,
                multiplication,
                50 * 50 + 11,
                false);
        MultiplicationResultAttempt attempt2 = new MultiplicationResultAttempt(user,
                multiplication,
                50 * 50 + 20,
                false);
        List<MultiplicationResultAttempt> latestAttempts = Lists.newArrayList(attempt1, attempt2);
        given(userRepository.findByAlias("Cory")).willReturn(Optional.empty());
        given(attemptRepository.findTop5ByUserAliasOrderByIdDesc("Cory")).willReturn(latestAttempts);

        // when
        List<MultiplicationResultAttempt> latestAttemptsResult =
                multiplicationService.getStatsForUser("Cory");

        // then
        assertThat(latestAttemptsResult).isEqualTo(latestAttempts);
    }
}