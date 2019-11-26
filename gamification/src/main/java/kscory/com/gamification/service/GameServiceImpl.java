package kscory.com.gamification.service;

import kscory.com.gamification.client.MultiplicationResultAttemptClient;
import kscory.com.gamification.client.dto.MultiplicationResultAttempt;
import kscory.com.gamification.domain.Badge;
import kscory.com.gamification.domain.BadgeCard;
import kscory.com.gamification.domain.GameStats;
import kscory.com.gamification.domain.ScoreCard;
import kscory.com.gamification.repository.BadgeCardRepository;
import kscory.com.gamification.repository.ScoreCardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GameServiceImpl implements GameService {

    public static final int LUCKY_NUMBER = 42;

    private ScoreCardRepository scoreCardRepository;

    private BadgeCardRepository badgeCardRepository;

    private MultiplicationResultAttemptClient attemptClient;

    @Autowired
    public GameServiceImpl(ScoreCardRepository scoreCardRepository,
                           BadgeCardRepository badgeCardRepository,
                           MultiplicationResultAttemptClient attemptClient) {
        this.scoreCardRepository = scoreCardRepository;
        this.badgeCardRepository = badgeCardRepository;
        this.attemptClient = attemptClient;
    }

    @Override
    public GameStats newAttemptForUser(Long userId, Long attemptId, boolean correct) {

        // 처음엔 답이 맞은 경우만 점수를 받는다.
        if (correct) {
            ScoreCard scoreCard = new ScoreCard(userId, attemptId);
            scoreCardRepository.save(scoreCard);
            log.info("사용자 ID {}, 점수 {} 점, 답안 ID {}",
                    userId, scoreCard.getScore(), attemptId);

            List<BadgeCard> badgeCards = processForBadges(userId, attemptId);
            return new GameStats(userId,
                    scoreCard.getScore(),
                    badgeCards.stream().map(BadgeCard::getBadge)
                            .collect(Collectors.toList()));
        }
        return GameStats.emptyStats(userId);
    }

    private List<BadgeCard> processForBadges(final Long userId,
                                             final Long attemptId) {

        List<BadgeCard> badgeCards = new ArrayList<>();
        int totalScore = scoreCardRepository.getTotalScoreForUser(userId);
        log.info("사용자 ID {} 의 새로운 점수 {}", userId, totalScore);

        List<ScoreCard> scoreCardList = scoreCardRepository
                .findByUserIdOrderByScoreTimestampDesc(userId);
        List<BadgeCard> badgeCardList = badgeCardRepository
                .findByUserIdOrderByBadgeTimestampDesc(userId);

        // 첫번째 정답 배지
        if (scoreCardList.size() == 1 && !containsBadge(badgeCardList, Badge.FIRST_WON)) {
            BadgeCard firstWonBadge = giveBadgeToUser(Badge.FIRST_WON, userId);
            badgeCards.add(firstWonBadge);
        }

        // 브론즈 배지 획득
        checkAndGiveBadgeBasedOnScore(userId,
                badgeCardList, totalScore, 100, Badge.BRONZE_MULTIPLICATOR)
                .ifPresent(badgeCards::add);
        // 실버 배지 획득
        checkAndGiveBadgeBasedOnScore(userId,
                badgeCardList, totalScore, 500, Badge.SILVER_MULTIPLICATOR)
                .ifPresent(badgeCards::add);
        // 골드 배지 획득
        checkAndGiveBadgeBasedOnScore(userId,
                badgeCardList, totalScore, 999, Badge.GOLD_MULTIPLICATOR)
                .ifPresent(badgeCards::add);

        // 행운의 숫자 배지
        MultiplicationResultAttempt attempt = attemptClient
                .retrieveMultiplicationResultAttemptById(attemptId);
        if (!containsBadge(badgeCardList, Badge.LUCKY_NUMBER) &&
                (LUCKY_NUMBER == attempt.getMultiplicationFactorA() ||
                        LUCKY_NUMBER == attempt.getMultiplicationFactorB())) {
            BadgeCard luckyNumberBadge = giveBadgeToUser(
                    Badge.LUCKY_NUMBER, userId);
            badgeCards.add(luckyNumberBadge);
        }

        return badgeCards;
    }

    @Override
    public GameStats retrieveStatsForUser(Long userId) {
        int score = scoreCardRepository.getTotalScoreForUser(userId);
        List<BadgeCard> badgeCards = badgeCardRepository
                .findByUserIdOrderByBadgeTimestampDesc(userId);
        return new GameStats(userId, score,
                badgeCards.stream().map(BadgeCard::getBadge)
                        .collect(Collectors.toList()));
    }

    @Override
    public ScoreCard getScoreForAttempt(final Long attemptId) {
        return scoreCardRepository.findByAttemptId(attemptId);
    }

    /**
     * 배지를 얻기 위한 조건을 넘는지 체크하는 편의성 메소드
     * 또한 조건이 충족되면 사용자에게 배지를 부여
     */
    private Optional<BadgeCard> checkAndGiveBadgeBasedOnScore(Long userId,
                                                   List<BadgeCard> badgeCards,
                                                   int score,
                                                   int scoreThreshold,
                                                   Badge badge) {
        if (score >= scoreThreshold && !containsBadge(badgeCards, badge)) {
            return Optional.of(giveBadgeToUser(badge, userId));
        }
        return Optional.empty();
    }

    /**
     * 배지 목록에 해당 배지가 포함되어 있는지 확인하는 메소드
     */
    private boolean containsBadge(final List<BadgeCard> badgeCards,
                                  final Badge badge) {
        return badgeCards.stream().anyMatch(b -> b.getBadge().equals(badge));
    }

    /**
     * 주어진 사용자에게 새로운 배지를 부여하는 메소드
     */
    private BadgeCard giveBadgeToUser(final Badge badge, final Long userId) {
        BadgeCard badgeCard = new BadgeCard(userId, badge);
        badgeCardRepository.save(badgeCard);
        log.info("사용자 ID {} 새로운 배지 획득: {}", userId, badge);
        return badgeCard;
    }
}

