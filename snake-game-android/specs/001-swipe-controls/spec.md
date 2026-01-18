# Feature Specification: Swipe-Based Snake Controls

**Feature Branch**: `001-swipe-controls`
**Created**: 2026-01-18
**Status**: Draft
**Input**: User description: "snake의 이동은 swipe를 통해서 작동해야한다."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Directional Control via Swipe (Priority: P1)

Players control the snake's direction by swiping on the screen. A swipe up moves the snake upward, swipe down moves it downward, swipe left moves it left, and swipe right moves it right. This is the core interaction mechanism for the entire game.

**Why this priority**: This is the fundamental control mechanism for the game. Without directional control, no gameplay is possible. This represents the minimum viable interaction for a snake game.

**Independent Test**: Can be fully tested by displaying a moving snake on screen and verifying that swipe gestures in each of the four cardinal directions (up, down, left, right) change the snake's movement direction accordingly. Delivers core gameplay value immediately.

**Acceptance Scenarios**:

1. **Given** snake is moving right, **When** player swipes upward on screen, **Then** snake changes direction to move upward
2. **Given** snake is moving up, **When** player swipes left on screen, **Then** snake changes direction to move left
3. **Given** snake is moving down, **When** player swipes right on screen, **Then** snake changes direction to move right
4. **Given** snake is moving left, **When** player swipes down on screen, **Then** snake changes direction to move down

---

### User Story 2 - Prevent Reverse Direction (Priority: P2)

Players cannot reverse the snake's direction 180 degrees (e.g., from right to left, or up to down) with a single swipe, as this would cause the snake to collide with itself immediately. The game must ignore reverse-direction swipe commands.

**Why this priority**: This prevents instant death scenarios that would frustrate players. While the game is playable without this safeguard, it would be extremely difficult and feel unfair to new players.

**Independent Test**: Can be tested by attempting to swipe in the opposite direction of current movement and verifying the snake continues in its original direction. Delivers a fair and learnable gameplay experience.

**Acceptance Scenarios**:

1. **Given** snake is moving right, **When** player swipes left, **Then** snake continues moving right (ignores reverse command)
2. **Given** snake is moving up, **When** player swipes down, **Then** snake continues moving up
3. **Given** snake is moving down, **When** player swipes up, **Then** snake continues moving down
4. **Given** snake is moving left, **When** player swipes right, **Then** snake continues moving left

---

### User Story 3 - Responsive Swipe Detection (Priority: P3)

Swipe gestures are detected quickly and reliably, providing immediate visual feedback when direction changes. Players should feel that their input is registered instantly without lag.

**Why this priority**: While basic swipe detection works in P1, this focuses on polish and responsiveness. The game is functional without perfect timing, but responsiveness significantly improves player satisfaction and perceived quality.

**Independent Test**: Can be tested by performing rapid direction changes and measuring the delay between swipe gesture completion and direction change execution. Delivers a smooth, responsive gameplay experience.

**Acceptance Scenarios**:

1. **Given** snake is moving, **When** player performs a swipe gesture, **Then** direction change occurs within 100 milliseconds of swipe completion
2. **Given** player performs multiple valid swipes in quick succession, **When** each swipe is in a valid direction, **Then** each direction change is queued and executed in order
3. **Given** player performs a very short swipe gesture, **When** swipe meets minimum distance threshold, **Then** direction change is registered
4. **Given** player performs an ambiguous diagonal swipe, **When** swipe has stronger horizontal or vertical component, **Then** direction is determined by the dominant axis

---

### Edge Cases

- What happens when player swipes during game initialization before snake starts moving?
- How does system handle simultaneous multi-finger touches while swiping?
- What happens if player performs multiple rapid swipes before the first direction change executes?
- How does system distinguish between intentional swipes and accidental touches?
- What happens when player performs a circular or curved swipe gesture?
- How does system handle swipes that start outside the game play area?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST detect swipe gestures in four cardinal directions (up, down, left, right)
- **FR-002**: System MUST change snake direction based on detected swipe direction within 100 milliseconds
- **FR-003**: System MUST prevent reverse-direction commands (180-degree turns that would cause immediate self-collision)
- **FR-004**: System MUST ignore swipe inputs that do not meet minimum distance threshold for gesture recognition
- **FR-005**: System MUST determine swipe direction based on the dominant axis (horizontal vs vertical) when gesture is not perfectly aligned
- **FR-006**: System MUST queue valid directional inputs when multiple swipes occur between movement updates
- **FR-007**: System MUST provide visual feedback indicating current snake direction
- **FR-008**: System MUST maintain 60 FPS during swipe gesture processing and direction changes
- **FR-009**: System MUST handle swipe gestures consistently across different screen sizes and aspect ratios
- **FR-010**: System MUST distinguish between swipes and taps (single-point touches without movement)

### Key Entities

- **Swipe Gesture**: Touch input characterized by start position, end position, duration, and velocity. Determines direction command.
- **Direction State**: Current movement direction of the snake (up, down, left, right). Updated by valid swipe gestures.
- **Input Queue**: Buffer for storing multiple direction change commands when swipes occur faster than snake movement updates.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Players can successfully change snake direction on their first attempt 95% of the time
- **SC-002**: Direction changes execute within 100 milliseconds of swipe gesture completion
- **SC-003**: Game maintains consistent 60 FPS performance during continuous swipe input
- **SC-004**: Reverse-direction commands are blocked 100% of the time (zero instances of immediate self-collision due to 180-degree turns)
- **SC-005**: System correctly interprets swipe direction for gestures with at least 30-degree deviation from pure diagonal (handles ambiguous input gracefully)
- **SC-006**: Players report control responsiveness as "good" or "excellent" in 90% of user feedback
- **SC-007**: Accidental touches (non-swipe) are filtered out with 99% accuracy

## Assumptions

- **Input Method**: Players use single-finger swipe gestures. Multi-touch gestures are not part of this feature scope.
- **Swipe Threshold**: Minimum swipe distance threshold is approximately 50 pixels (exact value to be determined during implementation based on typical device DPI).
- **Movement Speed**: Snake moves at a constant speed with discrete cell-based movement (not continuous pixel-based), allowing time for directional input between movements.
- **Screen Orientation**: Game supports portrait orientation by default. Landscape support may be added in future features.
- **Touch Sensitivity**: Standard Android touch sensitivity settings apply. No custom sensitivity adjustments in this feature.
- **Visual Feedback**: Existing snake rendering provides sufficient visual indication of direction. No additional direction indicators required in this feature.

## Dependencies

- **Game Grid System**: Swipe controls depend on existence of a game grid/play area where snake movement occurs
- **Snake Entity**: Requires basic snake entity with position and direction properties
- **Game Loop**: Requires game loop/update cycle to process queued direction changes and move snake accordingly

## Out of Scope

- Alternative control schemes (virtual joystick, button controls, tilt controls)
- Gesture customization or sensitivity settings
- Multi-touch or gesture combos
- Haptic feedback on swipe detection
- Tutorial or visual hints for swipe controls
- Accessibility alternatives to swipe gestures
