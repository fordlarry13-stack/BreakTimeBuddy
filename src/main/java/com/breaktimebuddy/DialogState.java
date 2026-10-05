package com.breaktimebuddy;

import java.util.UUID;

/**
 * State object representing a break recommendation dialog.
 *
 * @param id the unique identifier for this recommendation. It is used to implement idempotency of
 *        the accept and reject buttons.
 * @param message the recommendation
 */
public record DialogState(UUID id, String message) {
}
