package com.react.backend.msaudit.repository;

import com.react.backend.msaudit.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findByAggregateIdOrderByTimestampDesc(String aggregateId);
    List<AuditEvent> findByEventType(String eventType);
}