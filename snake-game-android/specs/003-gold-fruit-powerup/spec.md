# Feature Specification: Gold Fruit Power-Up System

**Feature Branch**: `003-gold-fruit-powerup`
**Created**: 2026-01-18
**Status**: Draft
**Input**: User description: "fruit에는 두가지 종류가 있으며, 기본 fruit는 snake가 먹는 경우 꼬리가 하나 늘고, 황금색의 gold fruit는 snake가 먹는 경우 꼬리가 3개 늘며, snake에게 게임 오버를 피할 수 있는 무적시간을 3초 부여한다. gold fruit는 fruit 생성시 3개마다 50%의 확률로 생성될 수 있다."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Basic Fruit Collection and Growth (Priority: P1)

When players collect a regular fruit, the snake grows by one segment. This is the fundamental growth mechanic that makes the game progressively more challenging as the snake gets longer. Regular fruits appear as the standard collectible throughout the game.

**Why this priority**: This is the core progression mechanic that defines snake gameplay. Without basic growth from fruit collection, the game has no increasing difficulty or sense of advancement. This represents the MVP for fruit collection.

**Independent Test**: Can be fully tested by collecting multiple regular fruits and verifying the snake grows by exactly one segment per fruit. Delivers immediate gameplay value through visible progress and increasing challenge.

**Acceptance Scenarios**:

1. **Given** snake has 5 segments, **When** player collects a regular fruit, **Then** snake grows to 6 segments
2. **Given** snake collects regular fruit, **When** growth occurs, **Then** new segment appears at the tail position
3. **Given** multiple regular fruits collected in sequence, **When** each fruit is consumed, **Then** snake grows by one segment each time (cumulative growth)
4. **Given** snake is at minimum length (initial size), **When** player collects first regular fruit, **Then** snake grows to initial size plus one

---

### User Story 2 - Gold Fruit Bonus Growth (Priority: P2)

Gold fruits are special rare collectibles that appear with a distinctive golden color. When players collect a gold fruit, the snake grows by three segments instead of one, providing a significant growth bonus. This creates exciting moments of accelerated progression.

**Why this priority**: This adds variety and excitement to fruit collection. While the game is playable with only regular fruits, gold fruits provide strategic opportunities and reward moments that enhance player engagement.

**Independent Test**: Can be tested by spawning and collecting gold fruits, verifying the snake grows by exactly three segments per gold fruit. Delivers enhanced gameplay value through bonus rewards.

**Acceptance Scenarios**:

1. **Given** snake has 5 segments, **When** player collects a gold fruit, **Then** snake grows to 8 segments (growth of 3)
2. **Given** gold fruit appears on grid, **When** player views it, **Then** gold fruit is visually distinct with golden color
3. **Given** player collects both regular and gold fruits, **When** growth is compared, **Then** gold fruit produces 3x growth of regular fruit
4. **Given** snake collects gold fruit, **When** growth occurs, **Then** three new segments appear sequentially at the tail

---

### User Story 3 - Invincibility Power-Up (Priority: P3)

When players collect a gold fruit, in addition to bonus growth, they receive a 3-second invincibility period. During this time, the snake cannot die from collisions with walls, boundaries, or itself. This provides a safety window and enables riskier aggressive play.

**Why this priority**: This adds strategic depth and forgiveness mechanics. The game is functional without invincibility, but it creates exciting risk-reward moments and helps players recover from difficult situations.

**Independent Test**: Can be tested by collecting a gold fruit and attempting normally-fatal collisions during the 3-second window, verifying the snake survives. Delivers enhanced player experience through tactical advantages.

**Acceptance Scenarios**:

1. **Given** player collects gold fruit, **When** invincibility activates, **Then** visual indicator shows snake is invincible for 3 seconds
2. **Given** snake is invincible, **When** snake collides with wall, **Then** snake survives and continues playing
3. **Given** snake is invincible, **When** snake head collides with its own body, **Then** snake survives and continues playing
4. **Given** invincibility period expires, **When** 3 seconds elapse after gold fruit collection, **Then** invincibility ends and normal collision rules resume

---

### User Story 4 - Gold Fruit Spawn Probability (Priority: P4)

Gold fruits spawn with strategic rarity. On every third fruit spawn, there is a 50% chance the fruit will be gold instead of regular. This creates anticipation and makes gold fruits feel special without being too common or too rare.

**Why this priority**: This balances gold fruit rarity for optimal game pacing. While spawn logic is needed for gold fruits to appear, the exact probability can be adjusted. This priority ensures the feature is complete and tuned for good game feel.

**Independent Test**: Can be tested by tracking fruit spawns over 100+ iterations and verifying gold fruits appear approximately every 6 spawns (every 3rd spawn with 50% chance). Delivers balanced power-up distribution.

**Acceptance Scenarios**:

1. **Given** 2 regular fruits have spawned, **When** third fruit spawns, **Then** 50% probability it is gold, 50% probability it is regular
2. **Given** third fruit spawn was regular, **When** next 2 fruits spawn, **Then** both are guaranteed regular fruits
3. **Given** third fruit spawn was gold, **When** next 2 fruits spawn, **Then** both are guaranteed regular fruits
4. **Given** multiple play sessions, **When** spawn statistics are analyzed, **Then** approximately 1 in 6 total fruits are gold (every third spawn with 50% chance)

---

### Edge Cases

- What happens when snake collects gold fruit while already invincible from previous gold fruit?
- How does system handle invincibility visual indicator during rapid movement?
- What happens if invincibility expires at the exact moment of collision?
- How does growth work when snake is very long (near grid capacity) and collects gold fruit requiring 3 segments?
- What happens when gold fruit spawns but player never collects it (does probability counter reset)?
- How is spawn count tracked across game restarts or level changes?
- What happens when player collects gold fruit at the exact moment invincibility from previous gold fruit expires?
- How does system display both invincibility indicator and normal snake visuals?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide two distinct fruit types: regular fruit and gold fruit
- **FR-002**: System MUST grow snake by exactly 1 segment when regular fruit is collected
- **FR-003**: System MUST grow snake by exactly 3 segments when gold fruit is collected
- **FR-004**: System MUST activate 3-second invincibility period when gold fruit is collected
- **FR-005**: System MUST prevent snake death from all collision types (wall, boundary, self) during invincibility period
- **FR-006**: System MUST automatically deactivate invincibility exactly 3 seconds after gold fruit collection
- **FR-007**: System MUST track fruit spawn count to determine when gold fruit eligibility occurs
- **FR-008**: System MUST spawn gold fruit with 50% probability on every third fruit spawn
- **FR-009**: System MUST spawn regular fruit with 100% probability on non-third spawns (spawns 1, 2, 4, 5, 7, 8, etc.)
- **FR-010**: System MUST reset the "third spawn" counter after each third spawn, regardless of fruit type spawned
- **FR-011**: System MUST visually distinguish gold fruit from regular fruit (golden color vs normal color)
- **FR-012**: System MUST provide visual indicator when snake is invincible
- **FR-013**: System MUST handle multiple consecutive gold fruit collections (refresh invincibility timer to 3 seconds)
- **FR-014**: System MUST maintain 60 FPS performance during invincibility state transitions and multi-segment growth

### Key Entities

- **Regular Fruit**: Standard collectible that grants 1 segment growth. Always spawns on non-third spawn cycles. Visually distinct with normal fruit appearance.
- **Gold Fruit**: Special collectible that grants 3 segments growth plus 3-second invincibility. Spawns with 50% probability on every third fruit spawn. Visually distinct with golden color.
- **Invincibility State**: Temporary power-up status lasting 3 seconds. Prevents death from all collision types. Has active/inactive status and remaining duration.
- **Spawn Counter**: Tracking mechanism that counts fruit spawns (1, 2, 3, reset). Determines when gold fruit eligibility occurs (every third spawn).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Snake grows by exactly 1 segment per regular fruit with 100% consistency
- **SC-002**: Snake grows by exactly 3 segments per gold fruit with 100% consistency
- **SC-003**: Invincibility lasts exactly 3 seconds (±50ms tolerance) after gold fruit collection
- **SC-004**: Zero player deaths occur during invincibility period from any collision type
- **SC-005**: Gold fruits spawn on approximately 50% of third-spawn opportunities over 100+ spawn samples
- **SC-006**: Players can visually distinguish gold fruit from regular fruit within 200 milliseconds of fruit appearance
- **SC-007**: Players can identify invincibility status within 200 milliseconds of activation through visual indicator
- **SC-008**: Invincibility timer refresh works correctly 100% of the time when collecting multiple gold fruits
- **SC-009**: Spawn counter cycles correctly (1-2-3-reset) with 100% accuracy across game sessions
- **SC-010**: Game maintains 60 FPS during 3-segment growth animations and invincibility state changes

## Assumptions

- **Fruit Types**: Only two fruit types exist in this feature (regular and gold). Other fruit varieties are out of scope.
- **Growth Mechanics**: Segments are added to the tail of the snake. Growth is instantaneous (or near-instantaneous with smooth animation).
- **Invincibility Scope**: Invincibility prevents death from all collision types: walls, grid boundaries, and self-collision. It does not affect movement or game speed.
- **Invincibility Duration**: 3-second duration starts immediately upon gold fruit collection. Timer is refreshed (reset to 3 seconds) if another gold fruit is collected during active invincibility.
- **Spawn Counter Persistence**: Spawn counter tracks across continuous gameplay but resets when game restarts. Counter increments only on actual fruit spawns, not on collection.
- **Probability Mechanism**: 50% chance is determined by random number generation at the moment of third-spawn event. Each third spawn is an independent 50/50 roll.
- **Visual Indicators**: Gold fruit has distinct golden/yellow color. Invincibility is indicated by visual effect (e.g., glow, color change, or animation) that clearly differentiates invincible state from normal state.
- **Multiple Gold Fruits**: Collecting gold fruit during active invincibility refreshes the timer to full 3 seconds and adds 3 more segments. Effects stack.

## Dependencies

- **Fruit Spawning System**: Requires existing fruit spawn logic that can be extended to support multiple fruit types
- **Snake Growth Mechanics**: Requires ability to add segments to snake body (extend by 1 or 3 segments)
- **Collision Detection**: Requires collision system that can be toggled or bypassed during invincibility
- **Timer System**: Requires ability to track and update 3-second countdown for invincibility duration
- **Random Number Generation**: Requires RNG for 50% gold fruit probability on third spawns
- **Visual Rendering**: Requires ability to render distinct visuals for gold fruit and invincibility state
- **Spawn Counter**: Requires persistent counter that tracks fruit spawn cycles (1, 2, 3, reset)

## Out of Scope

- Additional fruit types beyond regular and gold (e.g., speed-up fruit, shrink fruit, bonus points fruit)
- Invincibility duration adjustments based on difficulty level
- Variable gold fruit spawn probabilities or dynamic rarity
- Gold fruit special effects beyond growth and invincibility (e.g., score multipliers, temporary abilities)
- Player choice or control over invincibility activation
- Sound effects for gold fruit collection or invincibility activation
- Particle effects or complex animations for gold fruit
- Invincibility stacking or duration extension beyond timer refresh
- Different growth patterns (e.g., growth at head instead of tail)
- Save/load of spawn counter state across game sessions
