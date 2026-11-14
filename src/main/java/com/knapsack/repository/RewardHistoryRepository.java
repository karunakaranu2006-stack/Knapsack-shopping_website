package com.knapsack.repository;

import com.knapsack.model.RewardHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardHistoryRepository extends JpaRepository<RewardHistory, Integer> {

    List<RewardHistory> findByUser_UserIdOrderByTransactionDateDesc(Integer userId);
}
