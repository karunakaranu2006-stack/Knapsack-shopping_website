package com.knapsack.service;

import com.knapsack.model.RewardHistory;
import com.knapsack.model.RewardPoint;
import com.knapsack.model.User;
import com.knapsack.repository.RewardHistoryRepository;
import com.knapsack.repository.RewardPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RewardService {

    private final RewardPointRepository rewardPointRepository;
    private final RewardHistoryRepository rewardHistoryRepository;

    @Autowired
    public RewardService(RewardPointRepository rewardPointRepository, RewardHistoryRepository rewardHistoryRepository) {
        this.rewardPointRepository = rewardPointRepository;
        this.rewardHistoryRepository = rewardHistoryRepository;
    }

    @Transactional
    public void checkAndProcessDailyLoginReward(User user) {
        if (user == null || "ADMIN".equals(user.getRole())) return;

        LocalDate today = LocalDate.now();
        Optional<RewardPoint> rewardOpt = rewardPointRepository.findByUser_UserId(user.getUserId());

        RewardPoint rewardPoint;
        if (rewardOpt.isPresent()) {
            rewardPoint = rewardOpt.get();
            if (rewardPoint.getLastLoginDate() == null || rewardPoint.getLastLoginDate().isBefore(today)) {
                rewardPoint.setTotalPoints(rewardPoint.getTotalPoints() + 5);
                rewardPoint.setLastLoginDate(today);
                rewardPointRepository.save(rewardPoint);

                RewardHistory history = new RewardHistory(user, 5, 0, "Daily login reward");
                rewardHistoryRepository.save(history);
            }
        } else {
            rewardPoint = new RewardPoint(user, 5, today);
            rewardPointRepository.save(rewardPoint);

            RewardHistory history = new RewardHistory(user, 5, 0, "Daily login reward");
            rewardHistoryRepository.save(history);
        }
    }

    public RewardPoint getRewardPoint(User user) {
        return rewardPointRepository.findByUser_UserId(user.getUserId())
                .orElseGet(() -> new RewardPoint(user, 0, LocalDate.now()));
    }

    public List<RewardHistory> getRewardHistory(User user) {
        return rewardHistoryRepository.findByUser_UserIdOrderByTransactionDateDesc(user.getUserId());
    }

    @Transactional
    public boolean redeemPoints(User user, int pointsToRedeem, String orderReference) {
        if (pointsToRedeem <= 0) return true;

        Optional<RewardPoint> rewardOpt = rewardPointRepository.findByUser_UserId(user.getUserId());
        if (rewardOpt.isPresent()) {
            RewardPoint rewardPoint = rewardOpt.get();
            if (rewardPoint.getTotalPoints() >= pointsToRedeem) {
                rewardPoint.setTotalPoints(rewardPoint.getTotalPoints() - pointsToRedeem);
                rewardPointRepository.save(rewardPoint);

                RewardHistory history = new RewardHistory(user, 0, pointsToRedeem, "Redeemed points on order #" + orderReference);
                rewardHistoryRepository.save(history);
                return true;
            }
        }
        return false;
    }
}
