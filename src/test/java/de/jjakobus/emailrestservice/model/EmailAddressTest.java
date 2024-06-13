package de.jjakobus.emailrestservice.model;

import de.jjakobus.emailrestservice.model.dtos.EmailAddressDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests mapping functionality of {@link EmailAddress} entity.
 *
 * @author jjakobus
 */
class EmailAddressTest {

  @Test
  void shouldMapToDto() {
    // Given
    EmailAddress entity = new EmailAddress("sample.address@domain.de", "Sample Address");
    EmailAddressDto expectedDto = new EmailAddressDto("sample.address@domain.de", "Sample Address");

    // When
    EmailAddressDto mappedDto = entity.toDto();

    // Then
    assertThat(mappedDto)
        .as("Mapped DTO should contain all information from entity.")
        .isEqualTo(expectedDto);
  }

  @Test
  void shouldMapToDtoWithoutDisplayName() {
    // Given
    EmailAddress entity = new EmailAddress("sample.address@domain.de", null);
    EmailAddressDto expectedDto = new EmailAddressDto("sample.address@domain.de", null);

    // When
    EmailAddressDto mappedDto = entity.toDto();

    // Then
    assertThat(mappedDto)
        .as("Mapped DTO should contain all information from entity.")
        .isEqualTo(expectedDto);
  }
}
