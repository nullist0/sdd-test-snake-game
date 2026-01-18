package com.snakegame.domain.usecase

import com.snakegame.domain.model.Direction

/**
 * Validates direction changes according to snake game rules.
 *
 * Rule: Snake cannot reverse direction (180-degree turn).
 * Reverse directions are rejected to prevent instant self-collision.
 *
 * Valid directions:
 * - Perpendicular (90-degree turns): Accepted
 * - Same direction (continue current): Accepted
 * - Reverse (180-degree turn): Rejected
 *
 * Examples:
 * - Moving UP → can turn LEFT or RIGHT (perpendicular) ✓
 * - Moving UP → cannot turn DOWN (reverse) ✗
 * - Moving UP → can continue UP (same) ✓
 */
class ValidateDirectionUseCase {
    /**
     * Validates a requested direction change.
     *
     * @param current The snake's current movement direction
     * @param requested The direction requested by player input
     * @return true if direction change is allowed, false if rejected
     */
    operator fun invoke(current: Direction, requested: Direction): Boolean {
        // Reject reverse directions, accept all others
        // This includes:
        // - Perpendicular directions (90° turns): Valid for maneuvering
        // - Same direction (continue current): Valid for maintaining path
        return !current.isReverse(requested)
    }
}
