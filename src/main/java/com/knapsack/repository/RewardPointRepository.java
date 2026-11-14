package com.knapsack.repository;

import com.knapsack.model.RewardPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RewardPointRepository extends JpaRepository<RewardPoint, Integer> {

    Optional<RewardPoint> findByUser_UserId(Integer userId);
}
