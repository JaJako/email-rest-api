package de.jjakobus.emailrestservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.stream.Stream;

import static de.jjakobus.emailrestservice.EmailTestUtils.createExampleEmail;
import static de.jjakobus.emailrestservice.EmailTestUtils.createExampleInsertEmail;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
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
class EmailRestServiceControllerTest {

  /** Mock of email store service. */
  @MockBean
  private EmailStoreService emailStore;

  @Autowired
  private MockMvc mockMvc;

  @Value("${email-rest-service.request-path}")
  private String prefixPath;

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
            .post(prefixPath + "/insert")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(newEmail))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(content().json(expectedEmailJson, true));
  }

  @ParameterizedTest
  @MethodSource("provideBulkInsertEmailParams")
  void shouldHandleBulkInsertEmail(
      List<InsertEmailDto> newEmails,
      List<InsertEmailDto> filteredNewEmails,
      List<EmailDto> expectedInsertedEmails
  ) throws Exception {
    // Given
    String expectedEmailsJson = toJson(expectedInsertedEmails);

    when(emailStore.saveEmails(filteredNewEmails))
        .thenReturn(expectedInsertedEmails);

    // When & Then
    mockMvc.perform(MockMvcRequestBuilders
            .post(prefixPath + "/insert")
            .param("bulk", "true")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(newEmails))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(content().json(expectedEmailsJson, true));
  }

  private static Stream<Arguments> provideBulkInsertEmailParams() {
    InsertEmailDto email1 = createExampleInsertEmail();
    EmailDto email1Dto = createExampleEmail(42);
    InsertEmailDto email2 = createExampleInsertEmail();
    EmailDto email2Dto = createExampleEmail(24);

    return Stream.of(
        Arguments.of(Named.of("Null-free list", List.of(email1, email2)),
            List.of(email1, email2),
            List.of(email1Dto, email2Dto)),
        Arguments.of(Named.of("Some null elements", asList(email1, null, email2)),
            List.of(email1, email2),
            List.of(email1Dto, email2Dto)),
        Arguments.of(Named.of("All null elements", asList(null, null)),
            emptyList(),
            emptyList()),
        Arguments.of(Named.of("No elements", emptyList()),
            emptyList(),
            emptyList())
    );
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
            .get(prefixPath + "/query")
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
            .get(prefixPath + "/query")
            .param("id", String.valueOf(id))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @MethodSource("provideBulkQueryParams")
  void shouldHandleBulkQueryEmailById(
      List<Long> searchedIds,
      List<Long> filteredIds,
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

    when(emailStore.getEmails(filteredIds))
        .thenReturn(expectedFoundEmails);

    // When & Then
    mockMvc.perform(MockMvcRequestBuilders
            .get(prefixPath + "/query")
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

    return Stream.of(
        Arguments.of(Named.of("All emails are found", asList(42L, 12L, 16L)),
            List.of(42L, 12L, 16L),
            List.of(email1, email3, email2)),
        Arguments.of(Named.of("Some emails are found", asList(42L, 96L, 16L)),
            List.of(42L, 96L, 16L),
            List.of(email1, email3)),
        Arguments.of(Named.of("No emails are found", asList(13L, 96L, 122L)),
            List.of(13L, 96L, 122L),
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
            .put(prefixPath + "/update/" + id)
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
            .put(prefixPath + "/update/" + id)
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
            .put(prefixPath + "/update/" + id)
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
            .delete(prefixPath + "/delete/" + id))
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
            .delete(prefixPath + "/delete/" + id))
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
            .delete(prefixPath + "/delete")
            .param("bulk", "true")
            .params(idParams))
        .andExpect(status().isOk());
    // Verify call to store's deleteEmails(ids).
    verify(emailStore).deleteEmails(ids);
  }

  @ParameterizedTest
  @MethodSource("provideMissingInputRequests")
  void shouldHandleMissingInput(String requestPath, HttpMethod httpMethod) throws Exception {
    // Given
    // When & Then
    mockMvc.perform(MockMvcRequestBuilders
            .request(httpMethod, prefixPath + requestPath)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  private static Stream<Arguments> provideMissingInputRequests() {

    return Stream.of(
        Arguments.of("/insert", HttpMethod.POST),
        Arguments.of("/insert?bulk", HttpMethod.POST),
        Arguments.of("/query", HttpMethod.GET),
        Arguments.of("/query?bulk", HttpMethod.GET),
        Arguments.of("/update/0", HttpMethod.PUT),
        Arguments.of("/delete", HttpMethod.DELETE),
        Arguments.of("/delete?bulk", HttpMethod.DELETE)
    );
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
}
