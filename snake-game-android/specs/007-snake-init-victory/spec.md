# Feature Specification: Snake Initialization and Victory Condition

**Feature Branch**: `007-snake-init-victory`
**Created**: 2026-01-18
**Status**: Draft
**Input**: User description: "snake game 시작시 snake 길이는 3개로 시작하며 머리를 제외한 길이이다. 머리를 제외한 길이로 맵크기를 모두 덮을 수 있는 순간 게임이 끝난다."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Starting Snake Configuration (Priority: P1)

When a new game begins, the snake starts with a specific initial configuration: 1 head segment plus 3 body segments for a total of 4 segments. This provides players with a consistent starting point and ensures the game begins with a manageable snake size.

**Why this priority**: This is the fundamental starting state that defines the initial game conditions. Without a defined starting length, the game has no consistent baseline. This represents the MVP for game initialization.

**Independent Test**: Can be fully tested by starting a new game and counting the snake segments (1 head + 3 body = 4 total). Delivers consistent initial gameplay experience.

**Acceptance Scenarios**:

1. **Given** player starts new game from start menu, **When** game initializes, **Then** snake has exactly 4 total segments (1 head + 3 body)
2. **Given** game restarts from game over screen, **When** new game begins, **Then** snake resets to exactly 4 total segments
3. **Given** snake is displayed at game start, **When** player views the snake, **Then** head segment is visually distinguishable from 3 body segments
4. **Given** game just started, **When** counting segments, **Then** body length (excluding head) equals 3 segments

---

### User Story 2 - Victory Condition Detection (Priority: P2)

The game ends in victory when the snake's body segments (excluding the head) completely fill all available grid cells. This creates a maximum achievement goal where players must grow the snake to cover the entire playable area without colliding.

**Why this priority**: This adds a positive win condition beyond just avoiding death. While the game is playable with only collision-based game over, a victory condition provides an ultimate goal and sense of completion.

**Independent Test**: Can be tested by calculating required body length to fill grid, growing snake to that length, and verifying victory triggers. Delivers maximum achievement recognition.

**Acceptance Scenarios**:

1. **Given** grid is 10x10 (100 cells), **When** snake body (excluding head) reaches 99 segments, **Then** game triggers victory condition
2. **Given** snake body fills all cells except head position, **When** final fruit is collected, **Then** victory is detected immediately
3. **Given** victory condition is met, **When** game ends, **Then** game displays victory state (not collision game over)
4. **Given** victory occurs, **When** game ends, **Then** player sees indication that maximum achievement was reached

---

### User Story 3 - Victory State Presentation (Priority: P3)

When victory is achieved, the game displays a special victory screen distinct from the collision game over screen. This celebrates the player's maximum achievement and provides appropriate feedback for completing the ultimate challenge.

**Why this priority**: While victory detection works in P2, distinct presentation enhances the achievement experience. The game is functional with basic victory detection, but special presentation makes the accomplishment feel meaningful.

**Independent Test**: Can be tested by triggering victory condition and verifying a distinct victory screen appears with appropriate messaging. Delivers polished achievement experience.

**Acceptance Scenarios**:

1. **Given** victory condition is met, **When** victory screen displays, **Then** screen clearly indicates victory/completion (not failure)
2. **Given** victory screen is shown, **When** displayed, **Then** final score shows maximum possible body length for the grid
3. **Given** victory achieved, **When** victory screen appears, **Then** visual presentation differs from collision game over screen
4. **Given** victory screen displays, **When** player views it, **Then** restart button is available for new game attempt

---

### Edge Cases

- What happens when grid is too small to achieve victory (fewer cells than initial body length)?
- How does victory check interact with invincibility period?
- What happens if victory condition and collision occur on the same move?
- How does system calculate maximum body length for different grid sizes?
- What happens when fruit would spawn but only head cell remains empty?
- How does victory detection work when snake fills grid in different patterns?
- What happens if grid size changes between games (affects victory threshold)?
- How does score display differentiate between collision game over and victory?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST initialize snake with exactly 4 total segments at game start (1 head + 3 body)
- **FR-002**: System MUST position initial snake in a valid starting location within grid boundaries
- **FR-003**: System MUST reset snake to 4 segments when new game starts (from menu or restart)
- **FR-004**: System MUST calculate maximum body length as (total grid cells - 1) to account for head
- **FR-005**: System MUST detect victory when snake body segment count equals maximum body length
- **FR-006**: System MUST check for victory condition after each fruit collection and growth
- **FR-007**: System MUST trigger victory state when maximum body length is reached
- **FR-008**: System MUST prevent further gameplay after victory is detected
- **FR-009**: System MUST display victory screen distinct from collision game over screen
- **FR-010**: System MUST show final score on victory screen equal to maximum possible body length
- **FR-011**: System MUST provide restart button on victory screen for new game attempt
- **FR-012**: System MUST prioritize victory detection over collision detection when both would occur simultaneously

### Key Entities

- **Initial Snake**: Starting snake configuration with 1 head segment and 3 body segments (4 total). Represents beginning state of each game session.
- **Maximum Body Length**: Calculated value equal to (total grid cells - 1). Defines the victory threshold based on grid size.
- **Victory Condition**: Success state triggered when snake body segment count reaches maximum body length. Represents perfect game completion.
- **Victory Screen**: UI state displayed when victory is achieved. Shows celebration message, final score, and restart option. Distinct from collision game over.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of new games start with exactly 4 snake segments (1 head + 3 body)
- **SC-002**: Victory triggers correctly when body length equals (grid cells - 1) with 100% accuracy
- **SC-003**: Victory is detected within same frame as final fruit collection in 100% of cases
- **SC-004**: Victory screen displays within 100 milliseconds of victory condition being met
- **SC-005**: Players can distinguish victory screen from collision game over screen in 95% of cases
- **SC-006**: Maximum body length calculation is correct for all grid sizes with 100% accuracy
- **SC-007**: Victory screen shows final score matching maximum possible body length with 100% accuracy
- **SC-008**: Game maintains 60 FPS performance during victory detection and screen transition
- **SC-009**: 90% of players understand they achieved maximum possible growth when viewing victory screen

## Assumptions

- **Head and Body Distinction**: Snake consists of 1 head segment (front) plus N body segments (following). Total length = head + body segments.
- **Initial Configuration**: Game always starts with same initial length (4 total = 1 head + 3 body). No variable starting sizes.
- **Body Count**: "Body length" or "body segments" refers to all segments excluding the head. Used for victory calculation.
- **Grid Size**: Grid has a fixed size (e.g., 10x10, 15x15) defined elsewhere. Victory threshold adapts to grid size.
- **Victory Formula**: Maximum body length = (grid width × grid height) - 1. The -1 accounts for the head segment.
- **Victory Priority**: If victory and collision would occur simultaneously (e.g., final fruit collection causes both), victory takes precedence and player wins.
- **No Partial Cells**: Grid cells are discrete positions. Snake occupies whole cells, not partial spaces.
- **Starting Position**: Initial snake position is valid and within grid boundaries. Specific position (center, edge, corner) defined during implementation.
- **Victory is Rare**: Achieving victory requires perfect play with no collisions through entire grid fill. Expected to be a rare, significant achievement.

## Dependencies

- **Grid System**: Requires defined grid with known dimensions for calculating maximum body length
- **Snake Segment Tracking**: Requires ability to count and track head vs body segments separately
- **Game Initialization**: Requires game start logic that can set initial snake configuration
- **Growth System**: Requires fruit collection and snake growth mechanics for reaching victory
- **Game State Management**: Requires ability to transition to victory state (separate from collision game over)
- **Collision System**: Requires collision detection to distinguish victory from collision game over

## Out of Scope

- Variable starting snake lengths based on difficulty
- Multiple snake head positions or multi-headed snakes
- Different victory conditions (time-based, score-based, challenge-based)
- Victory replay or recording
- Victory statistics tracking across sessions
- Achievements or rewards for victory
- Different victory thresholds per difficulty level
- Partial victory or milestone celebrations
- Sound effects or music for victory
- Animated victory celebrations
- Social sharing of victory achievement
- Leaderboards for fastest victory time
