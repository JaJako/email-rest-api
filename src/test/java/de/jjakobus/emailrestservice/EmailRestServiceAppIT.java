package de.jjakobus.emailrestservice;

import de.jjakobus.emailrestservice.model.Email;
import de.jjakobus.emailrestservice.model.EmailAddress;
import de.jjakobus.emailrestservice.model.EmailState;
import de.jjakobus.emailrestservice.model.dtos.EmailDto;
import de.jjakobus.emailrestservice.model.dtos.InsertEmailDto;
import de.jjakobus.emailrestservice.service.EmailSpamFilterService;
import de.jjakobus.emailrestservice.service.repositories.EmailRepository;
import org.awaitility.Durations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.SpyBean;
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

import java.util.List;

import static de.jjakobus.emailrestservice.EmailTestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

/**
 * Tests the whole REST service in an integration-like test. Covers all REST endpoints by sending some exemplary
 * requests. Does not test all scenarios possible, see unit tests for those. A temporary database is used to test
 * communication with database as well.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "email-rest-service.spam-filter-cron=0/5 * * * * ?", // Run scheduled task every 5 seconds.
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

  @SpyBean
  private EmailSpamFilterService spamFilterTask;

  /** The random port used for the application in test. */
  @LocalServerPort
  private int port;

  /** REST template to create test requests from. */
  @Autowired
  private TestRestTemplate restTemplate;

  /** Repository of emails in database. */
  @Autowired
  private EmailRepository emailRepository;

  /** Example emails stored in repository. */
  private Email storedEmail1;
  private Email storedEmail2;

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
    Email exampleEntity1 = createExampleEmailEntity(42);
    storedEmail1 = emailRepository.save(exampleEntity1);
    Email exampleEntity2 = createExampleEmailEntity(43);
    storedEmail2 = emailRepository.save(exampleEntity2);
  }

  @Test
  void shouldRunSpamFilterScheduledTask() {
    // Given
    Email nonMatchingState = emailRepository.save(createExampleEmailEntity(101, EmailState.DRAFT));
    Email nonMatchingAddress = emailRepository.save(createExampleEmailEntity(102, EmailState.SENT));
    Email matchingAddressStub = createExampleEmailEntity(103, EmailState.SENT);
    matchingAddressStub.setFrom(new EmailAddress("carl@gbtec.com", null));
    Email matchingAddress = emailRepository.save(matchingAddressStub);

    // When & Then
    await().atMost(Durations.TEN_SECONDS).untilAsserted(() -> {
      // Verify task has run at least once.
      verify(spamFilterTask, atLeast(1)).classifySpamEmails();

      // Check emails in store, matchingAddress email should have state SPAM now.
      assertThat(emailRepository.findById(nonMatchingState.getId()))
          .isPresent().get()
          .as("Non-matching state email should have old state DRAFT.")
          .returns(EmailState.DRAFT, Email::getState);
      assertThat(emailRepository.findById(nonMatchingAddress.getId()))
          .isPresent().get()
          .as("Non-matching address email should have old state SENT.")
          .returns(EmailState.SENT, Email::getState);
      assertThat(emailRepository.findById(matchingAddress.getId()))
          .isPresent().get()
          .as("Matching address email should have new state SPAM.")
          .returns(EmailState.SPAM, Email::getState);
    });
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
        .matches(email -> containsAllInformationFromInsertDto(email, newEmail));
    assertThat(emailRepository.existsById(returnedEmail.id()))
        .as("Inserted email should be present in store.")
        .isTrue();
  }

  @Test
  void shouldInsertMailsBulk() {
    // Given
    List<InsertEmailDto> newEmails = List.of(createExampleInsertEmail(), createExampleInsertEmail());

    // When
    ResponseEntity<List<EmailDto>> response =
        restTemplate.exchange(
            baseRequestAddress + "/insert?bulk",
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
        .allMatch(email -> containsAllInformationFromInsertDto(email, newEmails.get(0)))
        .as("Service-side mails should be present.")
        .allMatch(email -> emailRepository.existsById(email.id()));
  }

  @Test
  void shouldQueryMail() {
    // Given
    long emailId = storedEmail1.getId(); // Use ID of stored email to run query.
    EmailDto expectedEmail = storedEmail1.toDto();

    // When
    ResponseEntity<EmailDto> response =
        restTemplate.getForEntity(
            baseRequestAddress + "/query?id={id}",
            EmailDto.class,
            emailId); // Send ID via URL param.

    // Then
    assertThat(response.getStatusCode())
        .as("HTTP status should be 200 (ok).")
        .isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .as("Returned email should be equal to expected email.")
        .isEqualTo(expectedEmail);
  }

  @Test
  void shouldQueryMailsBulk() {
    // Given
    long emailId1 = storedEmail1.getId(); // Use ID of stored email to run query.
    long emailId2 = storedEmail2.getId(); // Use ID of stored email to run query.
    long unknownId = 101;
    EmailDto expectedEmail1 = storedEmail1.toDto();
    EmailDto expectedEmail2 = storedEmail2.toDto();

    // When
    String idsParam = emailId1 + "," + emailId2 + "," + unknownId;
    ResponseEntity<List<EmailDto>> response =
        restTemplate.exchange(
            baseRequestAddress + "/query?bulk&ids={ids}",
            HttpMethod.GET,
            new HttpEntity<>(null, null), // Empty request entity/body.
            new ParameterizedTypeReference<>() {
            },
            idsParam);

    List<EmailDto> matchedEmails = response.getBody();

    // Then
    assertThat(response.getStatusCode())
        .as("HTTP status should be 200 (ok).")
        .isEqualTo(HttpStatus.OK);
    assertThat(matchedEmails)
        .as("Matched emails should be present (not null) and be no empty list.")
        .isNotNull().isNotEmpty()
        .as("Expected matched emails should be present.")
        .containsExactlyInAnyOrder(expectedEmail1, expectedEmail2);
  }

  @Test
  void shouldUpdateMail() {
    // Given
    EmailDto draftEmail = storedEmail1.toDto();
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
            draftEmail.modifiedDate());

    // When
    ResponseEntity<String> response =
        restTemplate.exchange(
            baseRequestAddress + "/update/{id}",
            HttpMethod.PUT,
            new HttpEntity<>(updatedEmail),
            String.class, // There will be no response value, but type is needed anyway, so use String.
            emailId);

    // Then
    assertThat(response.getStatusCode())
        .as("HTTP status should be 200 (ok).")
        .isEqualTo(HttpStatus.OK);
    assertThat(emailRepository.findById(updatedEmail.id()))
        .as("Updated email should be present.")
        .isPresent().get()
        .as("Service-side mail should contain updated subject and body.")
        .returns("Changed subject", Email::getSubject)
        .returns("new body content", Email::getBody);
  }

  @Test
  void shouldDeleteMail() {
    // Given
    long emailId = storedEmail1.getId(); // Use ID of stored email to delete.

    // When
    ResponseEntity<String> response =
        restTemplate.exchange(
            baseRequestAddress + "/delete/{id}",
            HttpMethod.DELETE,
            new HttpEntity<>(null, null), // Empty request entity/body.
            String.class, // There will be no response value, but type is needed anyway, so use String.
            emailId);

    // Then
    assertThat(response.getStatusCode())
        .as("HTTP status should be 200 (ok).")
        .isEqualTo(HttpStatus.OK);
    assertThat(emailRepository.findById(emailId))
        .as("Service should still contain a mail with given ID.")
        .isPresent().get()
        .as("State of mail should be \"DELETED\".")
        .returns(EmailState.DELETED, Email::getState);
  }

  @Test
  void shouldDeleteMailsBulk() {
    // Given
    long emailId1 = storedEmail1.getId(); // Use ID of stored email to run query.
    long emailId2 = storedEmail2.getId(); // Use ID of stored email to run query.
    long unknownId = 101;
    List<Long> ids = List.of(emailId1, unknownId, emailId2);

    // When
    String idsParam = emailId1 + "," + unknownId + "," + emailId2;
    ResponseEntity<String> response =
        restTemplate.exchange(
            baseRequestAddress + "/delete?bulk&ids={ids}",
            HttpMethod.DELETE,
            new HttpEntity<>(null, null), // Empty request entity/body.
            String.class,
            idsParam);

    // Then
    assertThat(response.getStatusCode())
        .as("HTTP status should be 200 (ok).")
        .isEqualTo(HttpStatus.OK);
    assertThat(emailRepository.findAllById(ids))
        .as("Service should still contain the mails with given IDs.")
        .hasSize(2)
        .as("State of mails should be \"DELETED\".")
        .allMatch(email -> email.getState() == EmailState.DELETED);
  }
}
