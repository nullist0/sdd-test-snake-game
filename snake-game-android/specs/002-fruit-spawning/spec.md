# Feature Specification: Strategic Fruit Spawning

**Feature Branch**: `002-fruit-spawning`
**Created**: 2026-01-18
**Status**: Draft
**Input**: User description: "fruit의 생성시 생성 위치는 snake 의 꼬리를 중심으로 3*3 위치에 가능한 영역에 생성하되 생성이 불가능한 경우 빈공간에 아무곳이나 생성한다."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Nearby Fruit Spawning (Priority: P1)

When a new fruit needs to appear, the game spawns it in a 3x3 grid area centered on the snake's tail position. This creates a strategic element where players can influence where the next fruit appears by positioning their snake thoughtfully. The fruit only spawns in empty cells within this preferred zone.

**Why this priority**: This is the core fruit spawning mechanic that makes the game interesting. Without fruit spawning, there's no objective or scoring mechanism. The 3x3 tail-centered approach adds strategic depth while keeping fruit accessible.

**Independent Test**: Can be fully tested by observing fruit spawn locations relative to the snake's tail over multiple spawn events. Delivers immediate gameplay value by providing collectible objectives that enable snake growth and scoring.

**Acceptance Scenarios**:

1. **Given** snake tail is at position (5,5) and cells around tail have empty spaces, **When** fruit spawns, **Then** fruit appears in one of the 8 cells surrounding the tail (3x3 grid excluding tail itself)
2. **Given** snake tail is at position (10,10) and multiple cells in 3x3 area are empty, **When** fruit spawns, **Then** fruit appears randomly in one of the available empty cells within that 3x3 zone
3. **Given** snake tail is at position (8,8) and only 2 cells in 3x3 area are empty, **When** fruit spawns, **Then** fruit appears in one of those 2 empty cells
4. **Given** game just started with short snake, **When** first fruit spawns, **Then** fruit appears within 3x3 grid centered on snake's tail

---

### User Story 2 - Fallback Random Spawning (Priority: P2)

When the preferred 3x3 area around the snake's tail is completely occupied (by the snake body or walls), the game automatically spawns the fruit in any random empty cell on the entire game grid. This ensures gameplay continues even when the preferred zone is unavailable.

**Why this priority**: This prevents the game from getting stuck when the tail area is blocked. While the game is playable without this (if 3x3 area is usually available), it's essential for longer games where the snake fills more space.

**Independent Test**: Can be tested by creating scenarios where the snake fills the 3x3 tail area completely, then verifying fruit spawns elsewhere on the grid. Delivers robustness for extended gameplay sessions.

**Acceptance Scenarios**:

1. **Given** snake tail is surrounded by snake body segments filling entire 3x3 area, **When** fruit needs to spawn, **Then** fruit appears in any empty cell anywhere on the game grid
2. **Given** snake tail is in a corner with all 3x3 cells occupied, **When** fruit spawns, **Then** fruit appears in a random empty location on the grid
3. **Given** snake is very long and fills most of the grid, **When** only 5 empty cells remain (all outside 3x3 tail area), **Then** fruit spawns in one of those 5 cells
4. **Given** 3x3 tail area is blocked but many empty cells exist elsewhere, **When** fruit spawns, **Then** fruit has equal probability of appearing in any empty cell outside the preferred zone

---

### User Story 3 - Visual Fruit Appearance (Priority: P3)

When a fruit spawns, it appears instantly on the grid with clear visual distinction from the snake and background. Players can immediately identify the fruit location and plan their movement to collect it.

**Why this priority**: While fruit spawning logic works in P1/P2, clear visual feedback enhances player experience. The game is functional without perfect visuals, but clarity improves playability and player satisfaction.

**Independent Test**: Can be tested by spawning multiple fruits and verifying each is immediately visible and distinguishable from other game elements. Delivers polished user experience.

**Acceptance Scenarios**:

1. **Given** fruit spawns on the grid, **When** player looks at the game screen, **Then** fruit is immediately visible with distinct color/shape from snake and background
2. **Given** new fruit replaces previously collected fruit, **When** spawn occurs, **Then** visual transition is instant (no delay between collection and new fruit appearance)
3. **Given** fruit exists on grid, **When** player is navigating snake, **Then** fruit remains consistently visible without flickering or disappearing
4. **Given** multiple game sessions, **When** fruit spawns in different locations, **Then** fruit appearance is consistent across all spawn locations

---

### Edge Cases

- What happens when the game grid is completely full (no empty cells anywhere)?
- How does system handle spawning when snake tail is at grid edge or corner (3x3 area extends beyond boundaries)?
- What happens if fruit spawns at the exact moment snake grows into that cell?
- How does system determine "empty" when checking spawn positions (what about walls or obstacles)?
- What happens when only 1 empty cell exists in the 3x3 tail area?
- How is the 3x3 grid calculated when tail is at position (0,0) or other boundary positions?
- What happens when fruit needs to spawn but game is paused or transitioning states?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST spawn fruit in a 3x3 grid area centered on the snake's tail position as the primary spawn strategy
- **FR-002**: System MUST identify all empty cells within the 3x3 tail-centered area before spawning
- **FR-003**: System MUST randomly select one empty cell from available cells in the 3x3 area for fruit placement
- **FR-004**: System MUST fallback to spawning fruit in any random empty cell on the entire grid when all cells in the 3x3 tail area are occupied
- **FR-005**: System MUST prevent fruit from spawning on cells already occupied by snake body segments
- **FR-006**: System MUST prevent fruit from spawning outside the game grid boundaries
- **FR-007**: System MUST handle edge cases where snake tail is at grid boundaries (adjusting 3x3 area to stay within bounds)
- **FR-008**: System MUST spawn exactly one fruit at a time on the grid
- **FR-009**: System MUST spawn new fruit immediately after previous fruit is collected by the snake
- **FR-010**: System MUST display fruit visually distinct from snake and background elements
- **FR-011**: System MUST handle the scenario where no empty cells exist anywhere on the grid (game over condition)

### Key Entities

- **Fruit**: Collectible game object that appears on the grid. Has a position (grid coordinates), visual representation, and exists in one of two states (active/collected).
- **Spawn Zone (3x3 Area)**: Calculated region centered on snake's tail, consisting of up to 9 cells (including tail position). Defines preferred fruit spawn locations.
- **Empty Cell**: Grid position not occupied by snake body, walls, or other obstacles. Eligible for fruit spawning.
- **Snake Tail**: The last segment of the snake body. Used as the center point for calculating the 3x3 spawn zone.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 95% of fruit spawns occur within the 3x3 tail-centered area when at least one cell in that area is empty
- **SC-002**: 100% of fruit spawns use fallback random placement when all cells in 3x3 tail area are occupied
- **SC-003**: Fruit spawns complete within 50 milliseconds of trigger event (fruit collection or game start)
- **SC-004**: Zero instances of fruit spawning on occupied cells (snake body, walls, or out of bounds)
- **SC-005**: Players can immediately identify fruit location within 500 milliseconds of spawn (visual clarity)
- **SC-006**: Random selection within available spawn cells is uniformly distributed (no bias toward specific positions)
- **SC-007**: Game handles boundary cases (tail at edges/corners) without crashes or errors in 100% of scenarios
- **SC-008**: Fruit spawning maintains 60 FPS game performance during spawn calculation and rendering

## Assumptions

- **Grid System**: Game uses a discrete grid system with defined rows and columns. Each cell can be in one of three states: empty, occupied by snake, or containing fruit.
- **Tail Definition**: Snake's tail is the last segment in the snake body array/list. Tail position is always valid and within grid boundaries.
- **3x3 Calculation**: The 3x3 area includes the tail cell itself plus the 8 immediately adjacent cells (up, down, left, right, and 4 diagonals). When tail is at boundaries, the 3x3 area is clipped to stay within grid bounds.
- **Single Fruit**: Only one fruit exists on the grid at any time. New fruit spawns only after previous fruit is collected.
- **Spawn Timing**: Fruit spawns occur at two events: (1) game start/initialization, (2) immediately after fruit collection by snake.
- **Occupied Cells**: Cells occupied by snake body are not eligible for fruit spawning. If walls or obstacles exist, they also block spawning.
- **Visual Representation**: Fruit has a distinct visual appearance (color, shape, or icon) that differentiates it from snake and background. Exact visual design is out of scope for this spec.

## Dependencies

- **Grid System**: Requires defined game grid with known dimensions (rows, columns) and cell state tracking
- **Snake Entity**: Requires snake with accessible tail position and body segment positions
- **Collision Detection**: Requires ability to query which cells are occupied vs empty
- **Random Number Generation**: Requires random selection capability for choosing spawn position from available cells
- **Rendering System**: Requires ability to display fruit at specified grid coordinates

## Out of Scope

- Multiple fruits spawning simultaneously
- Fruit types with different properties (e.g., special fruits, power-ups)
- Animated spawn effects or transitions
- Sound effects for fruit spawning
- Fruit expiration or time-limited fruits
- Player control over fruit spawn locations
- Difficulty-based spawn logic (varying spawn distance from tail)
- Fruit spawning in specific patterns or sequences
- Score or point values associated with fruit (covered in separate scoring feature)
