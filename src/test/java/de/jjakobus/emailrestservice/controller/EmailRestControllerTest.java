package de.jjakobus.emailrestservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import de.jjakobus.emailrestservice.model.EmailState;
import de.jjakobus.emailrestservice.model.dtos.EmailAddressDto;
import de.jjakobus.emailrestservice.model.dtos.EmailDto;
import de.jjakobus.emailrestservice.model.dtos.InsertEmailDto;
import de.jjakobus.emailrestservice.model.exceptions.EmailNotFoundException;
import de.jjakobus.emailrestservice.model.exceptions.EmailUpdateNotAllowedException;
import de.jjakobus.emailrestservice.service.EmailStoreService;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests controller's endpoints for all request-response scenarios. Delegation of tasks to responsible services is
 * tested as well. Services are mocked to not test their implementation here (see their individual unit tests).
 *
 * @author jjakobus
 */
@WebMvcTest(controllers = EmailRestServiceController.class) // Fokus on EmailRestController.
class EmailRestControllerTest {

  /** Mock of email store service. */
  @MockBean
  private EmailStoreService emailStore;

  @Autowired
  private MockMvc mockMvc;

  @Value("${email-rest-service.request-path}")
  private String requestPath;

  @Test
  void shouldHandleInsertEmail() throws Exception {
    // Given
    InsertEmailDto newEmail = createExampleInsertEmail();
    EmailDto expectedInsertedEmail = createExampleEmail(42);
    String expectedEmailJson = toJson(expectedInsertedEmail);

    when(emailStore.saveEmail(newEmail))
        .thenReturn(expectedInsertedEmail);

    // When & Then
    mockMvc.perform(MockMvcRequestBuilders
            .post(requestPath + "/insert")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(newEmail))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(content().json(expectedEmailJson, true));
  }

  @Test
  void shouldHandleBulkInsertEmail() throws Exception {
    // Given
    List<InsertEmailDto> newEmails = List.of(
        createExampleInsertEmail(),
        createExampleInsertEmail());

    List<EmailDto> expectedInsertedEmails = List.of(
        createExampleEmail(42),
        createExampleEmail(12));
    String expectedEmailsJson = toJson(expectedInsertedEmails);


    when(emailStore.saveEmails(newEmails))
        .thenReturn(expectedInsertedEmails);

    // When & Then
    mockMvc.perform(MockMvcRequestBuilders
            .post(requestPath + "/insert")
            .param("bulk", "true")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(newEmails))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(content().json(expectedEmailsJson, true));
  }

  @Test
  void shouldHandleQueryEmailById() throws Exception {
    // Given
    EmailDto exampleEmail = createExampleEmail(42);
    String expectedEmailJson = toJson(exampleEmail);
    long id = 42;

    when(emailStore.getEmail(id))
        .thenReturn(exampleEmail);

    // When & Then
    mockMvc.perform(MockMvcRequestBuilders
            .get(requestPath + "/query")
            .param("id", String.valueOf(id))
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedEmailJson, true));
  }

  @Test
  void shouldHandleQueryEmailByNonExistingId() throws Exception {
    // Given
    long id = 24;

    when(emailStore.getEmail(id))
        .thenThrow(EmailNotFoundException.class);

    // When & Then
    mockMvc.perform(MockMvcRequestBuilders
            .get(requestPath + "/query")
            .param("id", String.valueOf(id))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @MethodSource("provideBulkQueryParams")
  void shouldHandleBulkQueryEmailById(
      List<Long> searchedIds,
      List<EmailDto> expectedFoundEmails
  ) throws Exception {
    // Given
    String expectedEmailsJson = toJson(expectedFoundEmails);

    List<String> searchedIdsAsString =
        searchedIds.stream()
            .map(String::valueOf)
            .toList();
    MultiValueMap<String, String> idParams = new LinkedMultiValueMap<>();
    idParams.addAll("ids", searchedIdsAsString);

    when(emailStore.getEmails(searchedIds))
        .thenReturn(expectedFoundEmails);

    // When & Then
    mockMvc.perform(MockMvcRequestBuilders
            .get(requestPath + "/query")
            .param("bulk", "true")
            .params(idParams)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedEmailsJson, true));
  }

  private static Stream<Arguments> provideBulkQueryParams() {
    EmailDto email1 = createExampleEmail(42);
    EmailDto email2 = createExampleEmail(12);
    EmailDto email3 = createExampleEmail(16);
    List<Long> searchedIds = List.of(42L, 12L, 16L);

    return Stream.of(
        Arguments.of(Named.of("All emails are found", searchedIds),
            List.of(email1, email3, email2)),
        Arguments.of(Named.of("Some emails are found", searchedIds),
            List.of(email1, email3)),
        Arguments.of(Named.of("No emails are found", searchedIds),
            List.of())
    );
  }

  @Test
  void shouldHandleUpdateEmail() throws Exception {
    // Given
    EmailDto updatedEmail = createExampleEmail(42);
    long id = 42;

    // When & Then
    mockMvc.perform(MockMvcRequestBuilders
            .put(requestPath + "/update/" + id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(updatedEmail)))
        .andExpect(status().isOk());
    // Verify call to store's updateEmail(id, updatedEmail).
    verify(emailStore).updateEmail(id, updatedEmail);
  }

  @Test
  void shouldHandleNonAllowedUpdateEmail() throws Exception {
    // Given
    EmailDto updatedEmail = createExampleEmail(42);
    long id = 42;

    doThrow(EmailUpdateNotAllowedException.class)
        .when(emailStore).updateEmail(id, updatedEmail);

    // When & Then
    mockMvc.perform(MockMvcRequestBuilders
            .put(requestPath + "/update/" + id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(updatedEmail)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldHandleUpdateNonExistingEmail() throws Exception {
    // Given
    EmailDto updatedEmail = createExampleEmail(23);
    long id = 23;

    doThrow(new EmailNotFoundException("no email with id"))
        .when(emailStore).updateEmail(id, updatedEmail);

    // When & Then
    mockMvc.perform(MockMvcRequestBuilders
            .put(requestPath + "/update/" + id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(updatedEmail)))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldHandleDeleteEmail() throws Exception {
    // Given
    long id = 12;

    // When & Then
    mockMvc.perform(MockMvcRequestBuilders
            .delete(requestPath + "/delete/" + id))
        .andExpect(status().isOk());
    // Verify call to store's deleteEmail(id).
    verify(emailStore).deleteEmail(id);
  }

  @Test
  void shouldHandleDeleteNonExistingEmail() throws Exception {
    // Given
    long id = 12;

    doThrow(new EmailNotFoundException("no email with id"))
        .when(emailStore).deleteEmail(id);

    // When & Then
    mockMvc.perform(MockMvcRequestBuilders
            .delete(requestPath + "/delete/" + id))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldHandleBulkDeleteEmailById() throws Exception {
    // Given
    List<Long> ids = List.of(42L, 12L);

    MultiValueMap<String, String> idParams = new LinkedMultiValueMap<>();
    idParams.addAll("ids", List.of("42", "12"));

    // When & Then
    mockMvc.perform(MockMvcRequestBuilders
            .delete(requestPath + "/delete")
            .param("bulk", "true")
            .params(idParams))
        .andExpect(status().isOk());
    // Verify call to store's deleteEmails(ids).
    verify(emailStore).deleteEmails(ids);
  }

  /**
   * Returns JSON representation of given object.
   *
   * @param obj object to generate JSON for
   * @return JSON fo object
   */
  private static String toJson(Object obj) {
    String objJson;
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      // Use StdDateFormat() to match date format in REST responses.
      objectMapper.setDateFormat(new StdDateFormat());

      objJson = objectMapper.writeValueAsString(obj);

    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error mapping object to JSON.", e);

    }

    return objJson;
  }

  private static InsertEmailDto createExampleInsertEmail() {

    return new InsertEmailDto(
        EmailState.SENT,
        new EmailAddressDto("sample.address@domain.de", null),
        List.of(new EmailAddressDto("peter.lustig@gmail.com", "Peter Lustig")),
        List.of(),
        "Löwenzahn",
        "Planung neuer Sendung",
        new Date());
  }

  private static EmailDto createExampleEmail(int id) {

    return new EmailDto(
        id,
        EmailState.DRAFT,
        new EmailAddressDto("peter.mueller@gmx.de", "Peter Müller <peter.mueller(a)gmx.de>"),
        List.of(
            new EmailAddressDto("juergen.vogel@web.de", null),
            new EmailAddressDto("hans-peter@gmail.com", "Hans Peter")),
        List.of(),
        "Draft subject",
        "Hello,\n\nGoodbye!",
        new Date());
  }
}
