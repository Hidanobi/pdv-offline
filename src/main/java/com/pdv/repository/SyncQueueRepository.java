package com.pdv.repository;

import com.pdv.model.SyncQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyncQueueRepository extends JpaRepository<SyncQueue, Long> {

    List<SyncQueue> findByStatusOrderByDataCriacaoAsc(String status);

    long countByStatus(String status);
}
