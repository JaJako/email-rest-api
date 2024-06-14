package de.jjakobus.emailrestservice.service;

import de.jjakobus.emailrestservice.EmailTestUtils;
import de.jjakobus.emailrestservice.model.Email;
import de.jjakobus.emailrestservice.model.EmailState;
import de.jjakobus.emailrestservice.model.dtos.EmailDto;
import de.jjakobus.emailrestservice.model.dtos.InsertEmailDto;
import de.jjakobus.emailrestservice.model.exceptions.EmailNotFoundException;
import de.jjakobus.emailrestservice.model.exceptions.EmailUpdateNotAllowedException;
import de.jjakobus.emailrestservice.service.repositories.EmailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.jjakobus.emailrestservice.EmailTestUtils.*;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

/**
 * Tests the functionality of managing stored emails in unit tests.
 *
 * @author jjakobus
 */
@ExtendWith(MockitoExtension.class)
class EmailStoreServiceTest {

  @Mock
  private EmailRepository emailRepository;

  private EmailStoreService emailStoreService;

  @BeforeEach
  void createServiceUnderTest() {
    emailStoreService = new EmailStoreService(emailRepository);
  }

  @Test
  void shouldSaveNewEmail() {
    // Given
    InsertEmailDto newEmail = createExampleInsertEmail();

    Email expectedEmailEntity = createExampleEmailEntity(42);
    EmailDto expectedInsertedEmail = createExampleEmail(42);

    ArgumentCaptor<Email> emailEntityCaptor = ArgumentCaptor.forClass(Email.class);
    when(emailRepository.save(emailEntityCaptor.capture()))
        .thenReturn(expectedEmailEntity);

    // When
    EmailDto actualInsertedEmail = emailStoreService.saveEmail(newEmail);
    Email actualEmailEntity = emailEntityCaptor.getValue();

    // Then
    assertThat(actualInsertedEmail)
        //        .usingRecursiveComparison()
        //        .ignoringFields("id") // Ignore ID field, as it is a generated field.
        .as("Inserted email should be equal to expected inserted email (ignore ID).")
        .isEqualTo(expectedInsertedEmail);
    assertThat(actualEmailEntity)
        .usingRecursiveComparison()
        .ignoringFields("id") // Ignore ID field, as it is a generated field.
        .as("Actual email entity should be equal to expected entity (ignore ID).")
        .isEqualTo(expectedEmailEntity);
  }

  @Test
  void shouldSaveNewEmails() {
    // Given
    List<InsertEmailDto> newEmails = List.of(
        createExampleInsertEmail(),
        createExampleInsertEmail());

    List<Email> expectedEmailEntities = List.of(
        createExampleEmailEntity(42),
        createExampleEmailEntity(24));
    List<EmailDto> expectedInsertedEmails = List.of(
        createExampleEmail(42),
        createExampleEmail(24));

    @SuppressWarnings("unchecked") // Cannot create captor with generic type of list.
    ArgumentCaptor<List<Email>> emailsEntityCaptor = ArgumentCaptor.forClass(List.class);
    when(emailRepository.saveAll(emailsEntityCaptor.capture()))
        .thenReturn(expectedEmailEntities);

    // When
    List<EmailDto> actualInsertedEmails = emailStoreService.saveEmails(newEmails);
    List<Email> actualEmailEntities = emailsEntityCaptor.getValue();

    // Then
    assertThat(actualInsertedEmails)
        //        .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id") // Ignore ID field, as it is a generated field.
        .as("Inserted emails should be equal to expected inserted email (ignore ID field each).")
        .containsExactlyInAnyOrderElementsOf(expectedInsertedEmails);
    assertThat(actualEmailEntities)
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id") // Ignore ID field, as it is a generated field.
        .as("Actual email entities should contain all expected entities (ignore ID field each).")
        .isEqualTo(expectedEmailEntities);
  }

  @Test
  void shouldGetExistingEmail() throws EmailNotFoundException {
    // Given
    long id = 42;
    Email existingEmailEntity = createExampleEmailEntity(id);
    EmailDto expectedExistingEmail = createExampleEmail(id);

    when(emailRepository.findById(id))
        .thenReturn(Optional.of(existingEmailEntity));

    // When
    EmailDto actualExistingEmail = emailStoreService.getEmail(id);

    // Then
    assertThat(actualExistingEmail)
        .as("Existing email should be equal to expected email.")
        .isEqualTo(expectedExistingEmail);
  }

  @Test
  void shouldNotGetNonExistingEmail() {
    // Given
    long id = 24;

    when(emailRepository.findById(id))
        .thenReturn(Optional.empty());

    // When & Then
    assertThatExceptionOfType(EmailNotFoundException.class)
        .as("Exception should be thrown for no-email id.")
        .isThrownBy(() -> emailStoreService.getEmail(id))
        .as("Exception should contain significant keywords and given id.")
        .withMessageContainingAll("no", "email", "id", String.valueOf(id));
  }

  @ParameterizedTest
  @MethodSource("provideGetMatchingEmailsParams")
  void shouldGetMatchingEmails(
      List<Long> searchedIds,
      List<Email> matchedEmailEntities,
      List<EmailDto> expectedMatchedEmails
  ) {
    // Given
    when(emailRepository.findAllById(searchedIds))
        .thenReturn(matchedEmailEntities);

    // When
    List<EmailDto> actualMatchedEmail = emailStoreService.getEmails(searchedIds);

    // Then
    assertThat(actualMatchedEmail)
        .as("Matched emails should be equal to expected emails.")
        .containsExactlyInAnyOrderElementsOf(expectedMatchedEmails);
  }

  private static Stream<Arguments> provideGetMatchingEmailsParams() {
    return Stream.of(
        Arguments.of(Named.of("All matching IDs", List.of(42L, 16L, 52L)),
            List.of(createExampleEmailEntity(42), createExampleEmailEntity(16), createExampleEmailEntity(52)),
            List.of(createExampleEmail(42), createExampleEmail(16), createExampleEmail(52))),
        Arguments.of(Named.of("Some matching IDs", List.of(42L, 16L, 52L)),
            List.of(createExampleEmailEntity(42), createExampleEmailEntity(52)),
            List.of(createExampleEmail(42), createExampleEmail(52))),
        Arguments.of(Named.of("No matching ID", List.of(42L, 16L, 52L)),
            emptyList(),
            emptyList())
    );
  }

  @ParameterizedTest
  @MethodSource("provideAllowedUpdateParams")
  void shouldUpdateExistingEmail(
      Email origEmailEntity,
      Email expectedUpdatedEmail
  ) throws EmailNotFoundException, EmailUpdateNotAllowedException {
    // Given
    long id = origEmailEntity.getId();

    when(emailRepository.findById(id))
        .thenReturn(Optional.of(origEmailEntity));

    ArgumentCaptor<Email> updatedEmailCaptor = ArgumentCaptor.forClass(Email.class);
    when(emailRepository.save(updatedEmailCaptor.capture()))
        .thenReturn(expectedUpdatedEmail);

    // When
    emailStoreService.updateEmail(id, expectedUpdatedEmail.toDto());

    // Then
    assertThat(updatedEmailCaptor.getValue())
        .as("Updated email entity should be equal to expected updated email entity.")
        .isEqualTo(expectedUpdatedEmail);
  }


  private static Stream<Arguments> provideAllowedUpdateParams() {

    return Stream.of(
        Arguments.of(
            Named.of("Draft email update (all fields except state)",
                EmailTestUtils.createExampleEmailEntity(24, EmailState.DRAFT)),
            createUpdatedEmailOfState(24, EmailState.DRAFT)),
        Arguments.of(
            Named.of("Draft email to sent (fields unchanged)",
                EmailTestUtils.createExampleEmailEntity(24, EmailState.DRAFT)),
            EmailTestUtils.createExampleEmailEntity(24, EmailState.SENT)),

        Arguments.of(
            Named.of("Sent email to SPAM", EmailTestUtils.createExampleEmailEntity(42, EmailState.SENT)),
            EmailTestUtils.createExampleEmailEntity(42, EmailState.SPAM)),
        Arguments.of(
            Named.of("Sent email to DELETED", EmailTestUtils.createExampleEmailEntity(42, EmailState.SENT)),
            EmailTestUtils.createExampleEmailEntity(42, EmailState.DELETED)),

        Arguments.of(
            Named.of("SPAM email to SENT", EmailTestUtils.createExampleEmailEntity(42, EmailState.SPAM)),
            EmailTestUtils.createExampleEmailEntity(42, EmailState.SENT)),
        Arguments.of(
            Named.of("SPAM email to DELETED", EmailTestUtils.createExampleEmailEntity(42, EmailState.SPAM)),
            EmailTestUtils.createExampleEmailEntity(42, EmailState.DELETED)),

        Arguments.of(
            Named.of("DELETED email to SENT", EmailTestUtils.createExampleEmailEntity(42, EmailState.DELETED)),
            EmailTestUtils.createExampleEmailEntity(42, EmailState.SENT)),
        Arguments.of(
            Named.of("DELETED email to SPAM", EmailTestUtils.createExampleEmailEntity(42, EmailState.DELETED)),
            EmailTestUtils.createExampleEmailEntity(42, EmailState.SPAM))
    );
  }

  @Test
  void shouldNotUpdateNonExistingEmail() {
    // Given
    long id = 42;

    when(emailRepository.findById(id))
        .thenReturn(Optional.empty());

    // When & Then
    assertThatExceptionOfType(EmailNotFoundException.class)
        .as("Exception should be thrown for no-email id.")
        .isThrownBy(() -> emailStoreService.updateEmail(id, createExampleEmail(id)))
        .as("Exception should contain significant keywords and given id.")
        .withMessageContainingAll("no", "email", "id", String.valueOf(id));
    // Verify save(...) of repository has NOT been called.
    verify(emailRepository, never()).save(any());
  }

  @ParameterizedTest
  @MethodSource("provideNonAllowedUpdateParams")
  void shouldNotProcessNonAllowedUpdates(
      Email origEmailEntity,
      Email updatedEmail
  ) {
    // Given
    long id = origEmailEntity.getId();
    when(emailRepository.findById(id))
        .thenReturn(Optional.of(origEmailEntity));

    // When & Then
    assertThatExceptionOfType(EmailUpdateNotAllowedException.class)
        .as("Exception should be thrown for non-allowed update.")
        .isThrownBy(() -> emailStoreService.updateEmail(id, updatedEmail.toDto()))
        .as("Exception should contain significant keywords.")
        .withMessageContainingAll("not allowed", "email", "id", String.valueOf(id), "reason");
    // Verify save(...) of repository has NOT been called.
    verify(emailRepository, never()).save(any());
  }

  private static Stream<Arguments> provideNonAllowedUpdateParams() {

    return Stream.of(
        Arguments.of(
            Named.of("Draft email to SENT (with updated fields)",
                EmailTestUtils.createExampleEmailEntity(42, EmailState.DRAFT)),
            createUpdatedEmailOfState(42, EmailState.SENT)),
        Arguments.of(
            Named.of("Draft email to DELETE", EmailTestUtils.createExampleEmailEntity(42, EmailState.DRAFT)),
            EmailTestUtils.createExampleEmailEntity(42, EmailState.DELETED)),
        Arguments.of(
            Named.of("Draft email to SPAM", EmailTestUtils.createExampleEmailEntity(42, EmailState.DRAFT)),
            EmailTestUtils.createExampleEmailEntity(42, EmailState.SPAM)),

        Arguments.of(
            Named.of("DELETED email to DRAFT", EmailTestUtils.createExampleEmailEntity(42, EmailState.DELETED)),
            EmailTestUtils.createExampleEmailEntity(42, EmailState.DRAFT)),
        Arguments.of(
            Named.of("SPAM email to DRAFT", EmailTestUtils.createExampleEmailEntity(42, EmailState.SPAM)),
            EmailTestUtils.createExampleEmailEntity(42, EmailState.DRAFT)),
        Arguments.of(
            Named.of("SENT email to DRAFT", EmailTestUtils.createExampleEmailEntity(42, EmailState.SENT)),
            EmailTestUtils.createExampleEmailEntity(42, EmailState.DRAFT)),

        Arguments.of(
            Named.of("Non-draft email field changes", EmailTestUtils.createExampleEmailEntity(42, EmailState.SENT)),
            createUpdatedEmailOfState(42, EmailState.SENT)),
        Arguments.of(
            Named.of("Non-draft email field + state changes",
                EmailTestUtils.createExampleEmailEntity(42, EmailState.SENT)),
            createUpdatedEmailOfState(42, EmailState.DELETED)),
        Arguments.of(
            Named.of("Changing ID of any email", EmailTestUtils.createExampleEmailEntity(42, EmailState.DRAFT)),
            EmailTestUtils.createExampleEmailEntity(24, EmailState.DRAFT))
    );
  }

  @Test
  void shouldDeleteExistingEmail() throws EmailNotFoundException {
    // Given
    long id = 42;
    Email emailToDelete = createExampleEmailEntity(id);
    Email expectedDeletedEmail = createExampleEmailEntity(id);
    expectedDeletedEmail.setState(EmailState.DELETED);

    when(emailRepository.findById(id))
        .thenReturn(Optional.of(emailToDelete));

    // When
    emailStoreService.deleteEmail(id);

    // Then
    // Verify save(...) of repository has been called with updated email.
    verify(emailRepository).save(expectedDeletedEmail);
  }

  @Test
  void shouldNotDeleteNonExistingEmail() {
    // Given
    long id = 24;

    // When & Then
    assertThatExceptionOfType(EmailNotFoundException.class)
        .as("Exception should be thrown for no-email id.")
        .isThrownBy(() -> emailStoreService.deleteEmail(id))
        .as("Exception should contain significant keywords and given id.")
        .withMessageContainingAll("no", "email", "id", String.valueOf(id));
    // Verify save(...) of repository has NOT been called.
    verify(emailRepository, never()).save(any());
  }

  @Test
  void shouldDeleteMatchingEmails() {
    // Given
    List<Long> ids = List.of(42L, 16L);

    Email emailToDelete1 = createExampleEmailEntity(42L);
    Email expectedDeletedEmail1 = createExampleEmailEntity(42L);
    expectedDeletedEmail1.setState(EmailState.DELETED);
    Email emailToDelete2 = createExampleEmailEntity(16L);
    Email expectedDeletedEmail2 = createExampleEmailEntity(16L);
    expectedDeletedEmail2.setState(EmailState.DELETED);

    when(emailRepository.findAllById(ids))
        .thenReturn(List.of(emailToDelete1, emailToDelete2));


    // When
    emailStoreService.deleteEmails(ids);

    // Then
    // Verify saveAll(...) of repository has been called with updated emails.
    verify(emailRepository).saveAll(List.of(expectedDeletedEmail1, expectedDeletedEmail2));
  }
}
