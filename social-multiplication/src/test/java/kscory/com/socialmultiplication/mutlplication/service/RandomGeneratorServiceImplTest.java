package kscory.com.socialmultiplication.mutlplication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class RandomGeneratorServiceImplTest {

    private RandomGeneratorServiceImpl randomGeneratorService;

    @BeforeEach
    void setUp() {
        randomGeneratorService = new RandomGeneratorServiceImpl();
    }

    @Test
    void generateRandomFactorIsBetweenExpectedLimits() {
        // 무작위 숫자 생성
        List<Integer> randomFactors = IntStream.range(0, 1000)
                .map(i -> randomGeneratorService.generateRandomFactor())
                .boxed()
                .collect(Collectors.toList());

        // 적당히 어려운 계산을 만들기 위해 생성한 인수가 11 ~ 99 범위에 있는지 확인
        assertThat(randomFactors)
                .containsOnlyElementsOf(IntStream.range(11, 100)
                        .boxed()
                        .collect(Collectors.toList()));
    }
}