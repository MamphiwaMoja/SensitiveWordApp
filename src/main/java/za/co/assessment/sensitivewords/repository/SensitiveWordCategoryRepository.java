package za.co.assessment.sensitivewords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.assessment.sensitivewords.domain.SensitiveWordCategory;

import java.util.List;

public interface SensitiveWordCategoryRepository extends JpaRepository<SensitiveWordCategory, Long> {

    List<SensitiveWordCategory> findByActiveTrueOrderByCodeAsc();
}
