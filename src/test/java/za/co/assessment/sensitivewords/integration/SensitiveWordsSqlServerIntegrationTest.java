package za.co.assessment.sensitivewords.integration;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import za.co.assessment.sensitivewords.domain.SensitiveWord;
import za.co.assessment.sensitivewords.dto.request.CreateSensitiveWordRequest;
import za.co.assessment.sensitivewords.dto.request.SanitizeTextRequest;
import za.co.assessment.sensitivewords.dto.response.SanitizeTextResponse;
import za.co.assessment.sensitivewords.repository.SensitiveWordRepository;
import za.co.assessment.sensitivewords.service.SanitizationService;
import za.co.assessment.sensitivewords.service.SensitiveWordService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
class SensitiveWordsSqlServerIntegrationTest {

    @Container
    static final MSSQLServerContainer<?> SQL_SERVER = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
            .acceptLicense();

    @DynamicPropertySource
    static void registerSqlServerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", SQL_SERVER::getJdbcUrl);
        registry.add("spring.datasource.username", SQL_SERVER::getUsername);
        registry.add("spring.datasource.password", SQL_SERVER::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SensitiveWordRepository sensitiveWordRepository;

    @Autowired
    private SanitizationService sanitizationService;

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Autowired
    private EntityManager entityManager;

    @Test
    void liquibase_shouldCreateExpectedSchemaObjects() {
        assertThat(tableNames()).contains(
                "sensitive_word_categories",
                "sensitive_words",
                "sanitization_requests",
                "sensitive_word_audit_log"
        );

        Integer wordMaxLength = jdbcTemplate.queryForObject(
                """
                        SELECT CHARACTER_MAXIMUM_LENGTH
                        FROM INFORMATION_SCHEMA.COLUMNS
                        WHERE TABLE_SCHEMA = 'sw'
                          AND TABLE_NAME = 'sensitive_words'
                          AND COLUMN_NAME = 'word'
                        """,
                Integer.class
        );
        assertThat(wordMaxLength).isEqualTo(510);

        Integer computedColumnCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM sys.computed_columns cc
                        JOIN sys.tables t ON cc.object_id = t.object_id
                        JOIN sys.schemas s ON t.schema_id = s.schema_id
                        WHERE s.name = 'sw'
                          AND t.name = 'sensitive_words'
                          AND cc.name = 'normalized_word'
                          AND cc.is_persisted = 1
                        """,
                Integer.class
        );
        assertThat(computedColumnCount).isEqualTo(1);

        Integer activeWordIndexCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM sys.indexes i
                        JOIN sys.tables t ON i.object_id = t.object_id
                        JOIN sys.schemas s ON t.schema_id = s.schema_id
                        WHERE s.name = 'sw'
                          AND t.name = 'sensitive_words'
                          AND i.name = 'UX_sw_words_active_word'
                          AND i.has_filter = 1
                        """,
                Integer.class
        );
        assertThat(activeWordIndexCount).isEqualTo(1);
    }

    @Test
    void computedNormalizedWord_shouldBeGeneratedBySqlServer() {
        SensitiveWord saved = sensitiveWordRepository.saveAndFlush(word("  MixedCaseTerm  ", true));
        entityManager.clear();

        SensitiveWord reloaded = sensitiveWordRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getWord()).isEqualTo("  MixedCaseTerm  ");
        assertThat(reloaded.getNormalizedWord()).isEqualTo("mixedcaseterm");
    }

    @Test
    void uniqueActiveWordIndex_shouldRejectDuplicateActiveNormalizedWords() {
        sensitiveWordRepository.saveAndFlush(word("DuplicateTerm", true));

        assertThatThrownBy(() -> sensitiveWordRepository.saveAndFlush(word("  duplicateterm  ", true)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void uniqueActiveWordIndex_shouldAllowDuplicateInactiveWords() {
        sensitiveWordRepository.saveAndFlush(word("InactiveDuplicate", true));
        SensitiveWord inactiveDuplicate = sensitiveWordRepository.saveAndFlush(word(" inactiveDuplicate ", false));

        assertThat(inactiveDuplicate.getId()).isNotNull();
    }

    @Test
    void sanitize_shouldUseLiquibaseSeededWordsFromDatabase() {
        SanitizeTextResponse response = sanitizationService.sanitize(
                new SanitizeTextRequest("Possible SCAM and testbadword message", "integration-test", true)
        );

        assertThat(response.sanitizedText()).isEqualTo("Possible *** and *** message");
        assertThat(response.matchedWordsCount()).isEqualTo(2);
        assertThat(response.matchedWords())
                .extracting(match -> match.word().toLowerCase())
                .containsExactlyInAnyOrder("scam", "testbadword");

        Integer persistedLogCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM sw.sanitization_requests
                        WHERE source_system = 'integration-test'
                          AND matched_words_count = 2
                        """,
                Integer.class
        );
        assertThat(persistedLogCount).isEqualTo(1);
    }

    @Test
    void sanitize_shouldSeeNewActiveWordAfterServiceInvalidatesCache() {
        sanitizationService.sanitize(
                new SanitizeTextRequest("This text primes the active-word cache", "integration-test", false)
        );

        sensitiveWordService.create(new CreateSensitiveWordRequest(null, "cache-visible-term", 3, true));

        SanitizeTextResponse response = sanitizationService.sanitize(
                new SanitizeTextRequest("Message with cache-visible-term", "integration-test", false)
        );

        assertThat(response.sanitizedText()).isEqualTo("Message with ***");
        assertThat(response.matchedWords())
                .extracting(match -> match.word().toLowerCase())
                .contains("cache-visible-term");
    }

    private List<String> tableNames() {
        return jdbcTemplate.queryForList(
                """
                        SELECT TABLE_NAME
                        FROM INFORMATION_SCHEMA.TABLES
                        WHERE TABLE_SCHEMA = 'sw'
                        """,
                String.class
        );
    }

    private SensitiveWord word(String value, boolean active) {
        SensitiveWord word = new SensitiveWord();
        word.setWord(value);
        word.setSeverityLevel(1);
        word.setActive(active);
        return word;
    }
}
