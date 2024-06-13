package de.jjakobus.emailrestservice;

import de.jjakobus.emailrestservice.model.Email;
import de.jjakobus.emailrestservice.model.EmailState;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static de.jjakobus.emailrestservice.EmailTestUtils.*;
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
    Email exampleEntity = createExampleEmailEntity(42);
    emailRepository.save(exampleEntity);
  }

  /* Test CRUD endpoints exemplary. */

  @Test
  void shouldInsertMail() {
    // Given
    InsertEmailDto newEmail = createExampleInsertEmail();

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

  @Test
  void shouldInsertMultipleMails() {
    // Given
    List<InsertEmailDto> newEmails = List.of(createExampleInsertEmail(), createExampleInsertEmail());

    // When
    ResponseEntity<List<EmailDto>> response =
        restTemplate.exchange(
            baseRequestAddress + "/insert",
            HttpMethod.POST,
            new HttpEntity<>(newEmails),
            new ParameterizedTypeReference<>() {
            });
    List<EmailDto> returnedEmails = response.getBody();

    // Then
    assertThat(response.getStatusCode())
        .as("HTTP status should be 201 (created).")
        .isEqualTo(HttpStatus.CREATED);
    assertThat(returnedEmails)
        .as("Returned emails should be present (not null) and be no empty list.")
        .isNotNull().isNotEmpty()
        .as("Returned stored email should contain all information from inserted email.")
        .allMatch(storedEmail -> containsAllInformationFromInsertDto(storedEmail, newEmails.get(0)))
        .as("Service-side mails should be equal to returned emails.")
        .allMatch(email ->
            emailRepository.findEmailById(email.id())
                .filter(email::equals)
                .isPresent());
  }

  @Test
  void shouldQueryMail() {
    // Given
    EmailDto storedEmail = createExampleEmail(42);
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
    EmailDto draftEmail = createExampleEmail(42);
    long emailId = draftEmail.id(); // Use ID of stored email to update.
    EmailDto updatedEmail =
        new EmailDto(
            draftEmail.id(),
            draftEmail.state(),
            draftEmail.from(),
            draftEmail.to(),
            draftEmail.cc(),
            "Changed subject",
            "new body content",
            new Date());

    // When
    ResponseEntity<String> response =
        restTemplate.exchange(
            baseRequestAddress + "/update/" + emailId,
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
    EmailDto storedEmail = createExampleEmail(42);
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
}
