package io.falconFlow.services.secret.vault;

import io.falconFlow.services.secret.SecretDto;

/**
 * Writes a secret into a vault backend.
 *
 * Contract:
 * - Implementations must not log secret values.
 * - Implementations should throw a RuntimeException (or a domain exception) on failure.
 */
public interface VaultWriter {
    void store(SecretDto request);
}
