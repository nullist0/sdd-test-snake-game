# Feature Specification: No Reverse Direction Control

**Feature Branch**: `009-no-reverse-direction`
**Created**: 2026-01-18
**Status**: Draft
**Input**: User description: "snake game에서 이동제어는 snake의 진행방향과 반대 방향을  제외한 방향만 가능하다. 예를 들어 위로 이동하는 중이면 좌 우로만 제어가능하다."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Reverse Direction Prevention (Priority: P1)

When the snake is moving in any direction, players can only change direction to perpendicular directions, not the opposite direction. For example, if moving upward, players can only turn left or right, not downward. This prevents the snake from immediately colliding with its own body by reversing into itself.

**Why this priority**: This is a fundamental game rule that prevents instant game over scenarios. Without this restriction, players could accidentally reverse direction and immediately collide with their own body, creating frustrating and unfair game overs. This represents the core safety mechanic.

**Independent Test**: Can be fully tested by moving the snake in one direction and attempting to input the reverse direction command, verifying the command is ignored or rejected. Delivers fair gameplay and prevents accidental self-collision.

**Acceptance Scenarios**:

1. **Given** snake is moving upward, **When** player inputs downward direction command, **Then** snake continues moving upward (command ignored)
2. **Given** snake is moving leftward, **When** player inputs rightward direction command, **Then** snake continues moving leftward (command ignored)
3. **Given** snake is moving downward, **When** player inputs upward direction command, **Then** snake continues moving downward (command ignored)
4. **Given** snake is moving rightward, **When** player inputs leftward direction command, **Then** snake continues moving rightward (command ignored)

---

### User Story 2 - Perpendicular Direction Control (Priority: P2)

When the snake is moving in any direction, players can successfully change direction to either of the two perpendicular directions. For example, if moving upward, players can turn left or right. This allows full maneuverability within safe direction changes.

**Why this priority**: This enables complete directional control while maintaining the reverse-prevention safety. While the game is functional with just reverse prevention, explicit perpendicular control ensures players understand available movement options.

**Independent Test**: Can be tested by moving the snake in one direction and inputting each perpendicular direction command, verifying both perpendicular turns work correctly. Delivers complete maneuvering capability.

**Acceptance Scenarios**:

1. **Given** snake is moving upward, **When** player inputs left direction command, **Then** snake changes direction to leftward
2. **Given** snake is moving upward, **When** player inputs right direction command, **Then** snake changes direction to rightward
3. **Given** snake is moving leftward, **When** player inputs up direction command, **Then** snake changes direction to upward
4. **Given** snake is moving leftward, **When** player inputs down direction command, **Then** snake changes direction to downward

---

### User Story 3 - Same Direction Handling (Priority: P3)

When the snake is moving in any direction, players can input the same direction command without causing errors or unexpected behavior. The snake continues in its current direction smoothly.

**Why this priority**: While not strictly necessary (no direction change occurs), graceful handling of same-direction input improves user experience and prevents confusion. The game is functional without this, but it prevents edge case issues.

**Independent Test**: Can be tested by moving the snake in one direction and repeatedly inputting the same direction command, verifying smooth continued movement. Delivers polished control handling.

**Acceptance Scenarios**:

1. **Given** snake is moving upward, **When** player inputs upward direction command, **Then** snake continues moving upward without interruption
2. **Given** snake is moving leftward, **When** player inputs leftward direction command, **Then** snake continues moving leftward without interruption
3. **Given** same-direction commands are input rapidly, **When** processing multiple commands, **Then** snake maintains steady movement speed
4. **Given** player alternates between perpendicular and same-direction commands, **When** same-direction input occurs, **Then** no direction change happens (expected behavior)

---

### Edge Cases

- What happens when reverse direction command is input at the exact moment of a turn?
- How does system handle rapid direction inputs (perpendicular followed by reverse)?
- What happens when snake has only 1 segment (head only) - is reverse allowed?
- How does reverse prevention work during invincibility period?
- What happens when player inputs reverse direction while snake is stationary (game start)?
- How are diagonal swipes interpreted relative to current direction?
- What happens when multiple direction commands are queued before next movement tick?
- How does reverse prevention interact with very fast snake speeds?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST detect the snake's current movement direction at all times
- **FR-002**: System MUST identify when a directional input represents the reverse (opposite) direction
- **FR-003**: System MUST ignore or reject directional inputs that are opposite to current movement direction
- **FR-004**: System MUST allow directional inputs that are perpendicular (90 degrees) to current movement direction
- **FR-005**: System MUST allow directional inputs that match the current movement direction
- **FR-006**: System MUST prevent snake from reversing into its own body through direction control
- **FR-007**: System MUST maintain reverse prevention for all four cardinal directions (up, down, left, right)
- **FR-008**: System MUST apply reverse direction restriction continuously during active gameplay
- **FR-009**: System MUST process perpendicular direction changes within same movement tick if input before movement
- **FR-010**: System MUST handle rapid direction inputs without allowing reverse direction through timing exploits
- **FR-011**: System MUST provide consistent reverse prevention behavior regardless of snake length
- **FR-012**: System MUST apply reverse prevention even during invincibility period from gold fruit

### Key Entities

- **Current Direction**: The direction the snake is actively moving at any given moment. One of four cardinal directions: up, down, left, right.
- **Reverse Direction**: The opposite direction of the current direction. Forms pairs: up↔down, left↔right.
- **Perpendicular Directions**: The two directions at 90-degree angles to the current direction. For up/down: left and right. For left/right: up and down.
- **Direction Command**: Player input requesting a direction change. Subject to reverse prevention validation before being applied.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Reverse direction commands are rejected 100% of the time across all four direction pairs
- **SC-002**: Perpendicular direction commands are accepted and executed within 100 milliseconds with 100% reliability
- **SC-003**: Players cannot trigger self-collision through reverse direction input in 100% of attempts
- **SC-004**: Same-direction inputs maintain steady movement without speed fluctuation in 95% of cases
- **SC-005**: Direction validation completes before next movement tick in 100% of inputs
- **SC-006**: Rapid input sequences (3+ commands per second) are handled without allowing reverse through timing exploits in 100% of tests
- **SC-007**: Players understand reverse direction restriction within first 3 gameplay attempts in 90% of tests
- **SC-008**: Game maintains 60 FPS performance during direction input validation
- **SC-009**: Zero accidental game overs from reverse direction inputs within first 10 gameplay sessions per player

## Assumptions

- **Four Cardinal Directions**: Movement is restricted to up, down, left, right (no diagonal movement). Reverse pairs are: up↔down, left↔right.
- **Continuous Movement**: Snake is always moving in one of the four directions during active gameplay. Direction changes are relative to current direction.
- **Reverse Definition**: Reverse means exact opposite direction (180 degrees). Up↔Down, Left↔Right. No partial reverses exist.
- **Perpendicular Definition**: Perpendicular means 90-degree angles from current direction. For vertical movement (up/down), perpendicular is horizontal (left/right) and vice versa.
- **Body Length Requirement**: Reverse prevention applies when snake has 2+ segments. With only 1 segment (head only), reverse prevention may still apply for consistency (implementation choice).
- **Input Method**: Directional inputs come from swipe gestures (as defined in feature 001-swipe-controls). Reverse prevention applies to processed swipe direction.
- **No Diagonal Ambiguity**: Swipe detection resolves to cardinal directions before validation. Diagonal swipes are interpreted as closest cardinal direction.
- **Game State**: Reverse prevention only applies during active gameplay, not during paused, game over, or menu states.
- **Priority Over Other Rules**: Reverse prevention is checked before applying direction change. If reverse, direction change is blocked entirely.

## Dependencies

- **Directional Control System**: Requires swipe-based directional input (feature 001-swipe-controls)
- **Snake Movement**: Requires snake to have a current direction of movement
- **Body Tracking**: Requires tracking of snake body segments to understand self-collision risk
- **Input Processing**: Requires ability to validate and filter directional inputs before applying them
- **Collision System**: Reverse prevention specifically prevents self-collision scenarios (feature 005-collision-game-over)

## Out of Scope

- Diagonal direction movement or controls
- Variable reverse prevention based on snake length (always active)
- Warning messages or visual feedback when reverse input is attempted
- Tutorial or instruction explaining reverse prevention rule
- Different reverse prevention rules for different difficulty levels
- Reverse prevention toggle or settings option
- Input buffering for queued direction changes
- Reverse direction "cooldown" or timing-based prevention
- Different reverse prevention behavior during invincibility
- Undo/redo direction changes
- Direction prediction or auto-correction
- Haptic feedback for rejected reverse inputs
