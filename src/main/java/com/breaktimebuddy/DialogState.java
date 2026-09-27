package com.breaktimebuddy;

import java.util.UUID;

/**
 * State object representing a break recommendation dialog.
 *
 * {@link #id} is the unique identifier for this recommendation. It is used to implement idempotency
 * of the accept and reject buttons.
 */
public record DialogState(UUID id, String message) {
}
