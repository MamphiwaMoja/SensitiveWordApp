package za.co.assessment.sensitivewords.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.co.assessment.sensitivewords.domain.SensitiveWord;

import java.util.List;

public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, Long> {

    Page<SensitiveWord> findAll(Pageable pageable);

    // Longer entries run first so overlapping words replace predictably.
    @Query("""
            select sw
            from SensitiveWord sw
            order by length(sw.word) desc
            """)
    List<SensitiveWord> findWordsForSanitization();

    @Query("""
            select count(sw) > 0
            from SensitiveWord sw
            where sw.normalizedWord = :normalizedWord
            """)
    boolean existsByNormalizedWord(@Param("normalizedWord") String normalizedWord);

    @Query("""
            select count(sw) > 0
            from SensitiveWord sw
            where sw.normalizedWord = :normalizedWord
              and sw.id <> :excludedId
            """)
    boolean existsByNormalizedWordExcludingId(
            @Param("normalizedWord") String normalizedWord,
            @Param("excludedId") Long excludedId
    );
}
