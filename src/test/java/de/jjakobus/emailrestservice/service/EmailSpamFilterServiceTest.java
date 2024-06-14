package de.jjakobus.emailrestservice.service;

import de.jjakobus.emailrestservice.model.Email;
import de.jjakobus.emailrestservice.model.EmailState;
import de.jjakobus.emailrestservice.service.repositories.EmailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static de.jjakobus.emailrestservice.EmailTestUtils.createExampleEmailEntity;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the functionality of the spam filter task.
 *
 * @author jjakobus
 */
@ExtendWith(MockitoExtension.class)
class EmailSpamFilterServiceTest {

  @Mock
  private EmailRepository emailRepository;

  private EmailSpamFilterService emailSpamFilterService;

  @BeforeEach
  void createServiceUnderTest() {
    emailSpamFilterService = new EmailSpamFilterService(emailRepository);
  }

  @Test
  void shouldMarkMatchingEmailsAsSpam() {
    // Given
    Email matchingEmail1 = createExampleEmailEntity(0, EmailState.SENT);
    Email matchingEmail2 = createExampleEmailEntity(1, EmailState.SENT);
    matchingEmail2.getFrom().setAddress("matching2@domain.de");

    Email nonMatchingStateDeleted = createExampleEmailEntity(2, EmailState.DELETED);
    Email nonMatchingStateDraft = createExampleEmailEntity(3, EmailState.DRAFT);
    Email nonMatchingStateSpam = createExampleEmailEntity(4, EmailState.SPAM);

    Email expectedSpamEmail1 = createExampleEmailEntity(0, EmailState.SPAM);
    Email expectedSpamEmail2 = createExampleEmailEntity(1, EmailState.SPAM);
    expectedSpamEmail2.getFrom().setAddress("matching2@domain.de");

    emailSpamFilterService.addFilterAddress(
        matchingEmail1.getFrom());
    emailSpamFilterService.addFilterAddress(
        matchingEmail2.getFrom());

    when(emailRepository.findAllByFrom_Address(matchingEmail1.getFrom().getAddress()))
        .thenReturn(List.of(matchingEmail1, nonMatchingStateDeleted, nonMatchingStateDraft, nonMatchingStateSpam));
    when(emailRepository.findAllByFrom_Address("matching2@domain.de"))
        .thenReturn(List.of(matchingEmail2));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Email>> spamEmailsCaptor = ArgumentCaptor.forClass(List.class);
    when(emailRepository.saveAll(spamEmailsCaptor.capture()))
        .thenReturn(emptyList());

    // When
    emailSpamFilterService.classifySpamEmails();

    // Then
    assertThat(spamEmailsCaptor.getValue())
        .as("All SPAM emails should have been updated in repository.")
        .containsExactlyInAnyOrder(expectedSpamEmail1, expectedSpamEmail2);
  }

  @Test
  void shouldMarkNoEmailAsSpamInEmptyRepo() {
    // Given
    // When
    emailSpamFilterService.classifySpamEmails();

    // Then
    // Verify saveAll(...) on repository has been called with empty list = no updates.
    verify(emailRepository).saveAll(emptyList());
  }
}
