package de.jjakobus.emailrestservice;

import de.jjakobus.emailrestservice.model.Email;
import de.jjakobus.emailrestservice.model.EmailAddress;
import de.jjakobus.emailrestservice.model.EmailState;
import de.jjakobus.emailrestservice.model.dtos.EmailAddressDto;
import de.jjakobus.emailrestservice.model.dtos.EmailDto;
import de.jjakobus.emailrestservice.model.dtos.InsertEmailDto;
import de.jjakobus.emailrestservice.service.repositories.EmailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the whole REST service in an integration-like test. Covers all REST endpoints by sending some exemplary
 * requests. Does not test all scenarios possible, see unit tests for those. A temporary database is used to test
 * communication with database as well.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "email-rest-service.request-path=/emails-test", // Control request path for tests.
    "spring.datasource.url=jdbc:tc:postgresql://localhost:5432/emails-test", // Use separate test database.
    "spring.datasource.username=test",
    "spring.datasource.password=test"
})
class EmailRestServiceAppIT {

  private static final String HOST_ADDRESS = "http://localhost";
  private static final String PATH = "/emails-test";

  /**
   * Postgres docker container for this integration test.
   */
  @Container
  @ServiceConnection
  @SuppressWarnings("resource") // Misleading warning about closable container when chaining with...() calls.
  static PostgreSQLContainer<?> postgresContainer =
      new PostgreSQLContainer<>("postgres:16.3-alpine")
          .withDatabaseName("emails-test")
          .withUsername("test")
          .withPassword("test")
          .withExposedPorts(5432);

  /** The random port used for the application in test. */
  @LocalServerPort
  private int port;

  /** REST template to create test requests from. */
  @Autowired
  private TestRestTemplate restTemplate;

  /** Repository of emails in database. */
  @Autowired
  private EmailRepository emailRepository;

  /** Base address for requests. */
  private String baseRequestAddress;

  @BeforeEach
  void prepareBaseRequestAddress() {
    baseRequestAddress = HOST_ADDRESS + ":" + port + PATH;
  }

  @BeforeEach
  void prepareEmailsInDatabase() {
    // Delete all data (if some were left).
    emailRepository.deleteAll();

    // Insert new test data.
    EmailAddress addressEntity = new EmailAddress("sample.address@domain.de", "Sample Address");
    EmailAddress addressEntity2 = new EmailAddress("peter.mueller@gmx.de", null);
    Email exampleEntity = new Email(
        EmailState.DRAFT,
        addressEntity,
        List.of(addressEntity2),
        List.of(),
        "Sample subject",
        "Sample body content.",
        LocalDateTime.now());
    emailRepository.save(exampleEntity);
  }

  /* Test CRUD endpoints exemplary. */

  @Test
  void shouldInsertMail() {
    // Given
    InsertEmailDto newEmail = getNewEmail();

    // When
    ResponseEntity<EmailDto> response =
        restTemplate.postForEntity(
            baseRequestAddress + "/insert",
            newEmail,
            EmailDto.class);
    EmailDto returnedEmail = response.getBody();

    // Then
    assertThat(response.getStatusCode())
        .as("HTTP status should be 201 (created).")
        .isEqualTo(HttpStatus.CREATED);
    assertThat(returnedEmail)
        .as("Returned email should be present (not null).")
        .isNotNull()
        .as("Returned stored email should contain all information from inserted email.")
        .matches(storedEmail -> containsAllInformationFromInsertDto(storedEmail, newEmail));
    assertThat(emailRepository.findEmailById(returnedEmail.id()))
        .as("Inserted email should be present in store.")
        .isPresent().get()
        .as("Service-side mail should be equal to returned email.")
        .returns(true, returnedEmail::equals);
  }

  /**
   * Checks whether given {@link EmailDto} contains all the information given by the {@link InsertEmailDto}.
   *
   * @param email email to check
   * @param insertDto inserted email
   * @return whether email contains all information
   */
  private static boolean containsAllInformationFromInsertDto(
      EmailDto email,
      InsertEmailDto insertDto) {

    return insertDto.state().equals(EmailState.SENT)
        && insertDto.from().equals(email.from())
        && insertDto.to().equals(email.to())
        && insertDto.cc().equals(email.cc())
        && insertDto.subject().equals(email.subject())
        && insertDto.body().equals(email.body())
        && insertDto.modifiedDate().equals(email.modifiedDate());
  }

  @Test
  void shouldQueryMail() {
    // Given
    EmailDto storedEmail = getStoredDraftEmail();
    long emailId = storedEmail.id(); // Use ID of stored email to run query.

    // When
    ResponseEntity<EmailDto> response =
        restTemplate.getForEntity(
            baseRequestAddress + "/query",
            EmailDto.class,
            Map.of("id", emailId)); // Send ID via URL param.

    // Then
    assertThat(response.getStatusCode())
        .as("HTTP status should be 200 (ok).")
        .isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .as("Returned email should be equal to expected email.")
        .isEqualTo(storedEmail);
  }

  @Test
  void shouldUpdateMail() {
    // Given
    EmailDto draftEmail = getStoredDraftEmail();
    EmailDto updatedEmail =
        new EmailDto(
            draftEmail.id(),
            draftEmail.state(),
            draftEmail.from(),
            draftEmail.to(),
            draftEmail.cc(),
            "Changed subject",
            "new body content",
            LocalDateTime.now());

    // When
    ResponseEntity<String> response =
        restTemplate.exchange(
            baseRequestAddress + "/update",
            HttpMethod.PUT,
            new HttpEntity<>(updatedEmail),
            String.class); // There will be no response value, but type is needed anyway, so use String.

    // Then
    assertThat(response.getStatusCode())
        .as("HTTP status should be 200 (ok).")
        .isEqualTo(HttpStatus.OK);
    assertThat(emailRepository.findEmailById(updatedEmail.id()))
        .as("Updated email should be present.")
        .isPresent().get()
        .as("Service-side mail should be equal to local updated email.")
        .returns(true, updatedEmail::equals);
  }

  @Test
  void shouldDeleteMail() {
    // Given
    EmailDto storedEmail = getStoredDraftEmail();
    long emailId = storedEmail.id(); // Use ID of stored email to delete.

    // When
    ResponseEntity<String> response =
        restTemplate.exchange(
            baseRequestAddress + "/delete/" + emailId,
            HttpMethod.DELETE,
            new HttpEntity<>(null, null), // Empty request entity/body.
            String.class); // There will be no response value, but type is needed anyway, so use String.

    // Then
    assertThat(response.getStatusCode())
        .as("HTTP status should be 200 (ok).")
        .isEqualTo(HttpStatus.OK);
    assertThat(emailRepository.findEmailById(emailId))
        .as("Service should still contain a mail with given ID.")
        .isPresent().get()
        .as("State of mail should be \"DELETED\".")
        .returns(EmailState.DELETED, EmailDto::state);
  }

  /**
   * Returns a draft email that is stored in database.
   *
   * @return stored draft email
   */
  private static EmailDto getStoredDraftEmail() {

    return new EmailDto(
        42,
        EmailState.DRAFT,
        new EmailAddressDto("peter.mueller@gmx.de", "Peter Müller <peter.mueller(a)gmx.de>"),
        List.of(
            new EmailAddressDto("juergen.vogel@web.de", null),
            new EmailAddressDto("hans-peter@gmail.com", "Hans Peter")),
        List.of(),
        "Draft subject",
        "Hello,\n\nGoodbye!",
        LocalDateTime.of(2024, 5, 31, 10, 8, 54)
    );
  }

  /**
   * Returns a new email that is not stored in database yet.
   *
   * @return new email
   */
  private static InsertEmailDto getNewEmail() {

    return new InsertEmailDto(
        EmailState.DRAFT,
        new EmailAddressDto("juergen.vogel@web.de", null),
        List.of(
            new EmailAddressDto("peter.mueller@gmx.de", "Peter Müller <peter.mueller(a)gmx.de>")),
        List.of(),
        "Very important, please respond!",
        "Forgot what I want to tell you.",
        LocalDateTime.of(2024, 6, 3, 12, 48, 32)
    );
  }
}
