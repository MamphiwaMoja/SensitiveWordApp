package za.co.assessment.sensitivewords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.assessment.sensitivewords.domain.SensitiveWordAuditLog;

public interface SensitiveWordAuditLogRepository extends JpaRepository<SensitiveWordAuditLog, Long> {
}
