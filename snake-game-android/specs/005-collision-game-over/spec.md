# Feature Specification: Collision Detection and Game Over

**Feature Branch**: `005-collision-game-over`
**Created**: 2026-01-18
**Status**: Draft
**Input**: User description: "snake game의 종료는 snake가 진행방향으로 이동못할때로, 벽에 부딪히거나 snake 몸에 부딪히는 경우 종료한다."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Wall Collision Game Over (Priority: P1)

When the snake moves into a wall or grid boundary, the game immediately ends. This is the fundamental failure condition that creates challenge and enforces the playable area boundaries. Players must navigate within the grid to avoid hitting walls.

**Why this priority**: This is the core game over mechanic that defines the play area limits. Without wall collision detection, the game has no boundary constraints or primary failure condition. This represents the MVP for game termination.

**Independent Test**: Can be fully tested by moving the snake into walls/boundaries from different directions and verifying game ends immediately upon collision. Delivers fundamental game challenge through spatial constraints.

**Acceptance Scenarios**:

1. **Given** snake is moving toward the top boundary, **When** snake head reaches the boundary wall, **Then** game ends immediately
2. **Given** snake is moving toward the right boundary, **When** snake head collides with right wall, **Then** game ends immediately
3. **Given** snake is moving toward the bottom boundary, **When** snake head hits bottom wall, **Then** game ends immediately
4. **Given** snake is moving toward the left boundary, **When** snake head collides with left wall, **Then** game ends immediately

---

### User Story 2 - Self-Collision Game Over (Priority: P2)

When the snake's head collides with any segment of its own body, the game immediately ends. This creates increasing difficulty as the snake grows longer, requiring players to plan movement carefully to avoid self-intersection.

**Why this priority**: This is the secondary failure condition that scales difficulty with player success (longer snake = harder). While wall collision is sufficient for basic gameplay, self-collision adds strategic depth and progressive challenge.

**Independent Test**: Can be tested by maneuvering the snake to collide with its own body segments and verifying game ends upon contact. Delivers scaling difficulty that rewards skillful navigation.

**Acceptance Scenarios**:

1. **Given** snake has grown to 5+ segments, **When** snake head moves into any body segment position, **Then** game ends immediately
2. **Given** snake is in a curved position, **When** player navigates head to collide with mid-body segment, **Then** game ends immediately
3. **Given** snake is moving in a spiral pattern, **When** head collides with tail segment, **Then** game ends immediately
4. **Given** snake just grew after eating fruit, **When** new segment causes head to overlap with body, **Then** game ends immediately

---

### User Story 3 - Game Over State Presentation (Priority: P3)

When game ends due to collision, the game displays a clear game over screen indicating the game has ended. The game stops accepting movement input and presents the final state to the player before returning to the start menu.

**Why this priority**: While collision detection works in P1/P2, clear feedback about game end improves player experience. The game is functional with instant menu return, but game over presentation provides closure and context.

**Independent Test**: Can be tested by triggering game over conditions and verifying a game over state is presented before menu return. Delivers polished end-of-game experience.

**Acceptance Scenarios**:

1. **Given** collision occurs, **When** game over is triggered, **Then** game over screen displays indicating game has ended
2. **Given** game over screen is shown, **When** displayed, **Then** snake movement stops and no further input affects gameplay
3. **Given** game over occurs, **When** game over screen appears, **Then** final game state (snake position, score if applicable) is visible
4. **Given** game over screen is displayed, **When** brief period elapses or player taps screen, **Then** game returns to start menu

---

### Edge Cases

- What happens when collision occurs at the exact moment of fruit collection?
- How does system handle collision during invincibility period from gold fruit?
- What happens if multiple collision conditions occur simultaneously (wall + self-collision)?
- How does game detect collision when snake is moving at high speed (multiple cells per frame)?
- What happens when snake grows and new segment immediately overlaps with existing segment?
- How does collision work at exact grid corners (simultaneous horizontal and vertical boundaries)?
- What happens if collision is detected during screen transition or state change?
- How does game handle collision timing relative to movement update cycle?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST detect when snake head position equals any grid boundary wall position
- **FR-002**: System MUST end game immediately when snake head collides with any boundary wall
- **FR-003**: System MUST detect when snake head position equals any snake body segment position
- **FR-004**: System MUST end game immediately when snake head collides with its own body
- **FR-005**: System MUST check for collisions on every movement update before rendering
- **FR-006**: System MUST stop snake movement immediately upon collision detection
- **FR-007**: System MUST prevent further gameplay input after collision is detected
- **FR-008**: System MUST display game over state when collision triggers game end
- **FR-009**: System MUST distinguish between valid movement into empty cells and collision movement
- **FR-010**: System MUST handle collision detection consistently across all snake speeds and movement rates
- **FR-011**: System MUST NOT trigger game over during invincibility period from gold fruit (if invincibility is active)
- **FR-012**: System MUST return to start menu after game over state is presented

### Key Entities

- **Collision Event**: Occurrence when snake head occupies the same position as a wall or body segment. Triggers game over sequence.
- **Boundary Wall**: Grid edges that define the playable area. Collision with boundaries causes game over.
- **Snake Head**: Front-most segment of the snake. Position is checked against walls and body for collision detection.
- **Snake Body**: All segments except the head. Collision between head and any body segment causes game over.
- **Game Over State**: Terminal state entered upon collision. Stops gameplay, displays end screen, and transitions to menu.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of wall collisions result in immediate game over (zero instances of passing through walls)
- **SC-002**: 100% of self-collisions result in immediate game over (zero instances of passing through body)
- **SC-003**: Collision detection occurs within same frame as movement (zero instances of delayed collision detection)
- **SC-004**: Game over state displays within 100 milliseconds of collision detection
- **SC-005**: Zero false positive collisions (game ending when no collision occurred)
- **SC-006**: Zero false negative collisions (game continuing when collision occurred)
- **SC-007**: Collision detection works correctly at all snake speeds with 100% accuracy
- **SC-008**: Invincibility correctly prevents game over from collisions 100% of the time when active
- **SC-009**: Game maintains 60 FPS performance during collision detection and game over transitions
- **SC-010**: 95% of players understand why game ended based on game over presentation

## Assumptions

- **Grid Boundaries**: Game has defined rectangular grid boundaries. Walls are the grid edges, not separate obstacle entities.
- **Collision Timing**: Collision is checked after movement calculation but before visual rendering. Game over triggers immediately upon first collision detection.
- **Head-Only Collision**: Only the snake head position is checked for collisions. Body segments do not cause collision when they touch walls (only head matters).
- **Exact Position Matching**: Collision occurs when head position exactly matches wall position or body segment position (discrete grid-based collision, not pixel-perfect).
- **Invincibility Interaction**: If gold fruit invincibility is active, collisions are detected but game over is prevented. Normal collision rules resume when invincibility expires.
- **Movement-Collision Order**: Each game tick follows this sequence: (1) process input, (2) calculate new position, (3) check collision, (4) if no collision update position and render, (5) if collision trigger game over.
- **Game Over Presentation**: Game over screen is a brief state (1-3 seconds) or tap-to-dismiss before returning to start menu. Exact duration and interaction defined during implementation.
- **Multiple Collisions**: If both wall and self-collision would occur simultaneously, both are treated the same (game over). Priority doesn't matter since result is identical.

## Dependencies

- **Grid System**: Requires defined grid with known boundary positions
- **Snake Position Tracking**: Requires access to snake head position and all body segment positions
- **Movement System**: Requires movement update cycle where collision check can be inserted
- **Game State Management**: Requires ability to transition from active gameplay to game over state
- **Start Menu**: Requires start menu to return to after game over
- **Invincibility System** (if implemented): Requires ability to check if invincibility is currently active

## Out of Scope

- Score tracking or high score display on game over screen
- Detailed game over statistics (fruit collected, survival time, etc.)
- Retry button on game over screen (handled by start menu)
- Death animations or visual effects
- Sound effects for collision or game over
- Different game over messages based on collision type
- Gradual game over (slow-motion, fade out)
- Player confirmation before returning to menu
- Save/load of game state across game over
- Obstacles or hazards beyond walls and self-body (e.g., moving obstacles, special wall types)
