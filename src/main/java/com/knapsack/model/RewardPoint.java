package com.knapsack.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "reward_points")
public class RewardPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reward_id")
    private Integer rewardId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;

    @Column(name = "last_login_date")
    private LocalDate lastLoginDate;

    public RewardPoint() {}

    public RewardPoint(User user, Integer totalPoints, LocalDate lastLoginDate) {
        this.user = user;
        this.totalPoints = totalPoints != null ? totalPoints : 0;
        this.lastLoginDate = lastLoginDate;
    }

    public Integer getRewardId() {
        return rewardId;
    }

    public void setRewardId(Integer rewardId) {
        this.rewardId = rewardId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getTotalPoints() {
        return totalPoints != null ? totalPoints : 0;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public LocalDate getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(LocalDate lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
}
