package za.co.assessment.sensitivewords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.assessment.sensitivewords.domain.SanitizationRequestLog;

import java.util.UUID;

public interface SanitizationRequestLogRepository extends JpaRepository<SanitizationRequestLog, UUID> {
}
