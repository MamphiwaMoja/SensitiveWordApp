package za.co.assessment.sensitivewords.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.co.assessment.sensitivewords.domain.MatchType;
import za.co.assessment.sensitivewords.domain.SensitiveWord;

import java.time.LocalDateTime;
import java.util.List;

public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, Long> {

    @EntityGraph(attributePaths = "category")
    Page<SensitiveWord> findAll(Pageable pageable);

    @Query("""
            select sw
            from SensitiveWord sw
            left join fetch sw.category
            where sw.active = true
              and sw.effectiveFrom <= :now
              and (sw.effectiveTo is null or sw.effectiveTo > :now)
            -- Higher severity runs first, and longer words win before shorter overlapping matches.
            order by sw.severityLevel desc, length(sw.word) desc
            """)
    List<SensitiveWord> findActiveRules(@Param("now") LocalDateTime now);

    @Query("""
            select count(sw) > 0
            from SensitiveWord sw
            where sw.normalizedWord = :normalizedWord
              and sw.matchType = :matchType
              and sw.active = true
            """)
    boolean existsActiveRule(
            @Param("normalizedWord") String normalizedWord,
            @Param("matchType") MatchType matchType
    );

    @Query("""
            select count(sw) > 0
            from SensitiveWord sw
            where sw.normalizedWord = :normalizedWord
              and sw.matchType = :matchType
              and sw.active = true
              and sw.id <> :excludedId
            """)
    boolean existsActiveRuleExcludingId(
            @Param("normalizedWord") String normalizedWord,
            @Param("matchType") MatchType matchType,
            @Param("excludedId") Long excludedId
    );
}
