package io.falconFlow.services.secret.vault;

import io.falconFlow.entity.SecretEntity;
import io.falconFlow.services.secret.SecretDto;

/**
 * Writes a secret into a vault backend.
 *
 * Contract:
 * - Implementations must not log secret values.
 * - Implementations should throw a RuntimeException (or a domain exception) on failure.
 * - All implementations return SecretEntity for consistent API response.
 */
public interface VaultWriter {
    SecretEntity store(SecretDto request);
}
