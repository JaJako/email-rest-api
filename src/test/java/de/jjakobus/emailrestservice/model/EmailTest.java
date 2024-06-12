package de.jjakobus.emailrestservice.model;

import de.jjakobus.emailrestservice.model.dtos.EmailAddressDto;
import de.jjakobus.emailrestservice.model.dtos.EmailDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests mapping functionality of {@link Email} entity.
 *
 * @author jjakobus
 */
class EmailTest {

  @Test
  void shouldMapToDtoWithAllInformation() {
    // Given
    LocalDateTime date = LocalDateTime.now();
    Email entity = new Email(
        EmailState.DRAFT,
        getExampleAddress(),
        List.of(getExampleAddress()),
        List.of(getExampleAddress(), getExampleAddress()),
        "Subject string",
        "Body string",
        date,
        date);
    EmailDto expectedDto = new EmailDto(
        entity.getId(),
        EmailState.DRAFT,
        getExampleAddressDto(),
        List.of(getExampleAddressDto()),
        List.of(getExampleAddressDto(), getExampleAddressDto()),
        "Subject string",
        "Body string",
        date,
        date);

    // When
    EmailDto mappedDto = entity.toDto();

    // Then
    assertThat(mappedDto)
        .as("Dto should contain all information of entity.")
        .isEqualTo(expectedDto);
  }

  @Test
  void shouldMapToDtoWithMinimalInformation() {
    // Given
    LocalDateTime date = LocalDateTime.now();
    Email entity = new Email(
        EmailState.DRAFT,
        getExampleAddress(),
        List.of(),
        List.of(),
        "",
        "",
        date,
        date);
    EmailDto expectedDto = new EmailDto(
        entity.getId(),
        EmailState.DRAFT,
        getExampleAddressDto(),
        List.of(),
        List.of(),
        "",
        "",
        date,
        date);

    // When
    EmailDto mappedDto = entity.toDto();

    // Then
    assertThat(mappedDto)
        .as("Dto should contain all information of entity.")
        .isEqualTo(expectedDto);
  }

  private static EmailAddress getExampleAddress() {
    return new EmailAddress(
        "sample.address@domain.de",
        "Sample Address");
  }

  private static EmailAddressDto getExampleAddressDto() {
    return new EmailAddressDto(
        "sample.address@domain.de",
        "Sample Address");
  }
}
