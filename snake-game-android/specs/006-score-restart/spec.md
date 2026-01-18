# Feature Specification: Score Display and Restart

**Feature Branch**: `006-score-restart`
**Created**: 2026-01-18
**Status**: Draft
**Input**: User description: "snake game 종료시 늘어난 snake 길이가 점수가 되어 점수가 표시되고 재시작 버튼을 표시한다."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Final Score Display (Priority: P1)

When the game ends, players see their final score displayed on the game over screen. The score is calculated based on the snake's total length achieved during that game session. This provides immediate feedback on performance and creates a measurable goal for players.

**Why this priority**: This is the core scoring feedback that gives players a sense of achievement and progress. Without score display, players have no quantifiable measure of their performance. This represents the MVP for score tracking.

**Independent Test**: Can be fully tested by playing a game to completion and verifying the final snake length is displayed as the score. Delivers immediate performance feedback and achievement recognition.

**Acceptance Scenarios**:

1. **Given** snake grew to length 10 during gameplay, **When** game ends, **Then** score of 10 is displayed on game over screen
2. **Given** snake grew to length 5 during gameplay, **When** game ends, **Then** score of 5 is displayed on game over screen
3. **Given** snake starts at length 3 and grows to length 8, **When** game ends, **Then** score of 8 is displayed (total length, not growth amount)
4. **Given** player collected regular and gold fruits, **When** game ends, **Then** score accurately reflects final snake length including all growth

---

### User Story 2 - Restart Button (Priority: P2)

After viewing their score on the game over screen, players can tap a restart button to immediately begin a new game session without returning to the start menu. This enables quick iteration and repeat gameplay for score improvement.

**Why this priority**: This enhances replayability by reducing friction between game sessions. While players can restart via the start menu, a dedicated restart button streamlines the flow for engaged players attempting to beat their score.

**Independent Test**: Can be tested by triggering game over, tapping the restart button, and verifying a new game begins immediately. Delivers streamlined replay experience.

**Acceptance Scenarios**:

1. **Given** game over screen is displayed with score, **When** player taps restart button, **Then** new game begins immediately with snake at starting position and length
2. **Given** restart button is tapped, **When** new game starts, **Then** score resets to initial snake length
3. **Given** player is viewing game over screen, **When** restart button is visible, **Then** button is clearly identifiable and distinguished from other UI elements
4. **Given** restart button is tapped, **When** new game initializes, **Then** all game state resets (fruit position, snake position, score counter)

---

### User Story 3 - Score Presentation Clarity (Priority: P3)

The score is displayed prominently and clearly on the game over screen, with appropriate labeling that helps players understand what the number represents. The presentation ensures players immediately recognize their achievement.

**Why this priority**: While basic score display works in P1, clear presentation enhances understanding and satisfaction. The game is functional with just a number, but proper labeling and visual design improve player experience.

**Independent Test**: Can be tested by showing game over screen to users and measuring comprehension of the score meaning and value. Delivers polished feedback presentation.

**Acceptance Scenarios**:

1. **Given** game over screen displays, **When** player views score, **Then** score number is large enough to read clearly (minimum readable size)
2. **Given** score is shown, **When** displayed, **Then** score has a label or context indicating it represents snake length (e.g., "Length:", "Score:", or similar)
3. **Given** game over screen contains score and restart button, **When** player views screen, **Then** score is visually prominent and positioned to draw attention
4. **Given** different score values (5 vs 50), **When** displayed, **Then** score formatting adjusts appropriately for different digit counts

---

### Edge Cases

- What happens when snake length is at starting minimum (never collected fruit)?
- How does score display handle very large snake lengths (e.g., 100+ segments)?
- What happens if game over occurs during invincibility period (does score still display)?
- How does restart button behave if tapped multiple times rapidly?
- What happens when player returns to start menu instead of using restart button?
- How is score calculated when player collected both regular and gold fruits?
- What happens if game over and restart occur in rapid succession?
- How does score display work across different screen sizes and orientations?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST track snake length throughout gameplay session
- **FR-002**: System MUST calculate final score as the total snake length at game over
- **FR-003**: System MUST display final score on game over screen
- **FR-004**: System MUST display restart button on game over screen
- **FR-005**: System MUST begin new game session when restart button is tapped
- **FR-006**: System MUST reset score to initial snake length when new game starts via restart
- **FR-007**: System MUST reset all game state (snake position, fruit, direction) when restart button is used
- **FR-008**: System MUST display score before restart button interaction is available
- **FR-009**: System MUST handle restart button taps within 100 milliseconds (responsive feedback)
- **FR-010**: System MUST maintain score accuracy regardless of fruit types collected (regular vs gold)
- **FR-011**: System MUST provide visual distinction between score display and restart button

### Key Entities

- **Score**: Numeric value representing the snake's total length achieved in a game session. Calculated at game over and displayed to player.
- **Snake Length**: Total number of segments in the snake at any point in time. Directly determines score value.
- **Game Over Screen**: UI state displayed after collision. Shows score and restart button. Intermediary state between gameplay and new game start.
- **Restart Button**: Interactive UI element that initiates a new game session from game over screen. Bypasses start menu for quick replay.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Score displays accurately reflect final snake length with 100% consistency
- **SC-002**: Players can read and comprehend score value within 1 second of game over screen appearing in 95% of tests
- **SC-003**: Restart button successfully initiates new game 100% of the time when tapped
- **SC-004**: New game via restart completes initialization within 200 milliseconds in 95% of restarts
- **SC-005**: Score resets correctly to starting snake length on restart in 100% of cases
- **SC-006**: Players can distinguish restart button from score display within 500 milliseconds in 95% of first-time views
- **SC-007**: Score calculation includes all growth from regular and gold fruits with 100% accuracy
- **SC-008**: Game over screen maintains 60 FPS rendering performance with score and button display
- **SC-009**: 90% of players understand score represents snake length based on presentation

## Assumptions

- **Score Calculation**: Score equals total snake length (number of segments) at game over. Not growth amount, not fruit count, but absolute length.
- **Initial Length**: Snake starts at a defined initial length (e.g., 3 segments). Score minimum is this initial length if no fruits collected.
- **Length Counting**: All segments count toward score, including head and all body segments.
- **Regular Fruit**: Adds 1 to snake length, therefore adds 1 to potential final score.
- **Gold Fruit**: Adds 3 to snake length, therefore adds 3 to potential final score.
- **Score Display Timing**: Score is calculated and frozen at the moment of collision/game over. No further updates to score after game over triggers.
- **Restart vs Menu**: Restart button starts new game directly from game over screen. Separate from start menu flow (which still exists and is accessible).
- **Button Interaction**: Restart button requires explicit tap. No auto-restart or timeout-based restart.
- **Score Persistence**: Score from one session is not saved or compared across sessions (no high score tracking in this feature).
- **Screen Layout**: Game over screen displays both score and restart button simultaneously. Not separate sequential screens.

## Dependencies

- **Snake Length Tracking**: Requires ongoing tracking of snake segment count during gameplay
- **Game Over System**: Requires game over state that can display UI before returning to menu
- **Game Initialization**: Requires ability to start new game from restart button (not just from start menu)
- **State Reset**: Requires ability to reset all game state (snake, fruit, score) for new game
- **UI Rendering**: Requires ability to display text (score) and button on game over screen

## Out of Scope

- High score tracking across sessions
- Score history or statistics
- Leaderboards or score sharing
- Score breakdown (e.g., regular fruits vs gold fruits)
- Time-based scoring or bonus points
- Achievements or milestones based on score
- Sound effects for score display or restart
- Animated score counting (e.g., score incrementing from 0 to final value)
- Comparison to previous session scores
- Save/load of scores across app sessions
- Return to start menu button (separate from restart - already exists in game over flow)
- Score multipliers or difficulty-based scoring
