# Feature Specification: Grid Size Selection

**Feature Branch**: `008-grid-size-selection`
**Created**: 2026-01-18
**Status**: Draft
**Input**: User description: "snake game 맵은 15*15, 17*17, 19*19 크기를 사용하며 게임 시작 전에 선택하여 실행한다."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Grid Size Selection Before Game Start (Priority: P1)

Before starting a new game, players choose from three available grid sizes: 15×15, 17×17, or 19×19. The selected grid size determines the play area dimensions for that game session. This allows players to choose difficulty level through grid size variation.

**Why this priority**: This is the core selection mechanism that enables variable difficulty gameplay. Without grid selection, players have no control over play area size. This represents the MVP for customizable game difficulty.

**Independent Test**: Can be fully tested by accessing size selection from start menu, choosing each size option, and verifying the selected grid is used. Delivers player choice and difficulty customization.

**Acceptance Scenarios**:

1. **Given** player is on start menu, **When** accessing grid size selection, **Then** three options are displayed: 15×15, 17×17, 19×19
2. **Given** player selects 15×15 grid, **When** game starts, **Then** game uses 15×15 grid (225 total cells)
3. **Given** player selects 17×17 grid, **When** game starts, **Then** game uses 17×17 grid (289 total cells)
4. **Given** player selects 19×19 grid, **When** game starts, **Then** game uses 19×19 grid (361 total cells)

---

### User Story 2 - Grid Size Persistence Through Session (Priority: P2)

Once a grid size is selected, that size persists for the current game session and any restarts from the game over screen. Players don't need to reselect grid size when using the restart button, streamlining repeated attempts at the same difficulty.

**Why this priority**: This enhances usability by maintaining player's choice across restarts. While functional without persistence, remembering the selection reduces friction for players attempting multiple games at the same difficulty.

**Independent Test**: Can be tested by selecting a grid size, playing to game over, restarting, and verifying the same grid size is used. Delivers streamlined replay experience.

**Acceptance Scenarios**:

1. **Given** player selected 17×17 and played game, **When** restarting from game over screen, **Then** new game uses 17×17 grid (same as previous)
2. **Given** player used restart button twice, **When** third game starts, **Then** grid size remains unchanged from initial selection
3. **Given** player returns to start menu, **When** starting new selection process, **Then** grid size can be changed for next session
4. **Given** multiple restarts with 19×19, **When** each new game initializes, **Then** all games use 19×19 grid

---

### User Story 3 - Clear Visual Size Indication (Priority: P3)

The grid size selection interface clearly displays each size option with visual distinction and current selection highlighting. Players can easily identify which size is selected before starting the game.

**Why this priority**: While basic selection works in P1, clear visual feedback ensures players understand their choice. The game is functional with simple buttons, but visual clarity prevents selection errors.

**Independent Test**: Can be tested by viewing selection interface and verifying size options are clearly labeled and selection state is obvious. Delivers polished selection experience.

**Acceptance Scenarios**:

1. **Given** grid selection interface displays, **When** player views options, **Then** each size (15×15, 17×17, 19×19) is clearly labeled
2. **Given** player taps 17×17 option, **When** selection changes, **Then** 17×17 option shows visual selected state
3. **Given** 19×19 is selected, **When** viewing interface, **Then** selected option is visually distinct from unselected options
4. **Given** player changes selection from 15×15 to 19×19, **When** change occurs, **Then** visual selection indicator moves to 19×19

---

### Edge Cases

- What happens if no grid size is explicitly selected (default behavior)?
- How does grid size selection integrate with start menu layout?
- What happens when player selects size, goes back to menu, then starts game?
- How does grid size affect victory condition calculation (body = grid cells - 1)?
- What happens if player rapidly taps multiple grid size options?
- How does interface handle different screen sizes displaying grid selection?
- What happens when grid size changes between app sessions (persistence across app restarts)?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide exactly three grid size options: 15×15, 17×17, and 19×19
- **FR-002**: System MUST display grid size selection interface before game start
- **FR-003**: System MUST allow player to select one grid size option
- **FR-004**: System MUST initialize game with the selected grid size dimensions
- **FR-005**: System MUST persist selected grid size through restart button usage
- **FR-006**: System MUST allow grid size change when returning to start menu for new session
- **FR-007**: System MUST provide visual indication of currently selected grid size
- **FR-008**: System MUST use default grid size (15×15) if no explicit selection is made
- **FR-009**: System MUST update victory condition threshold based on selected grid size
- **FR-010**: System MUST respond to grid size selection within 100 milliseconds
- **FR-011**: System MUST render selected grid size correctly on all target device screen sizes

### Key Entities

- **Grid Size**: Configuration option defining play area dimensions. Three available values: 15×15 (225 cells), 17×17 (289 cells), 19×19 (361 cells).
- **Selection State**: Currently chosen grid size for upcoming game session. Persists through restarts until player returns to menu.
- **Size Option**: Interactive UI element representing one grid size choice. Has unselected and selected visual states.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: All three grid size options (15×15, 17×17, 19×19) are selectable with 100% reliability
- **SC-002**: Selected grid size applies correctly to game initialization in 100% of starts
- **SC-003**: Grid size persists through restart button in 100% of game over → restart transitions
- **SC-004**: Players can identify currently selected grid size within 500 milliseconds in 95% of tests
- **SC-005**: Selection responds to player tap within 100 milliseconds in 95% of interactions
- **SC-006**: Victory condition updates correctly for each grid size (224, 288, 360 max body length respectively) with 100% accuracy
- **SC-007**: Interface renders correctly on target device screens in 100% of tested sizes
- **SC-008**: 90% of players successfully select intended grid size on first attempt

## Assumptions

- **Three Fixed Sizes**: Only three grid sizes are available (15×15, 17×17, 19×19). No custom sizes or additional preset sizes.
- **Square Grids**: All grids are square (width = height). No rectangular grid options.
- **Default Size**: If player starts game without explicit selection, 15×15 is used as default (smallest/easiest difficulty).
- **Selection Timing**: Grid size must be selected before tapping play button. Cannot change mid-game.
- **Session Persistence**: Selected size persists within current app session but may or may not persist across app restarts (implementation choice).
- **Restart Behavior**: Restart button maintains current grid size. Returning to start menu allows changing size.
- **Victory Calculation**: Victory threshold adapts to grid size: 15×15 = 224 max body, 17×17 = 288 max body, 19×19 = 360 max body.
- **Selection Location**: Grid size selection is accessible from start menu before game begins (exact UI placement defined during implementation).
- **Single Selection**: Only one grid size can be active at a time (radio button-style selection, not checkboxes).

## Dependencies

- **Start Menu**: Requires start menu interface where grid selection can be placed
- **Game Initialization**: Requires game initialization that accepts grid size parameter
- **Grid System**: Requires grid rendering that adapts to different dimensions
- **Victory System**: Requires victory threshold calculation that updates with grid size changes
- **State Management**: Requires ability to store selected grid size across game restarts

## Out of Scope

- Custom grid sizes (player-defined dimensions)
- Additional preset sizes beyond three specified (e.g., 13×13, 21×21)
- Rectangular grids (non-square dimensions like 15×20)
- Grid size preview before selection
- Grid size-based scoring multipliers
- Grid size statistics tracking (most played size, etc.)
- Recommended size based on skill level
- Grid size naming (e.g., "Easy", "Medium", "Hard" labels)
- Animated grid size transitions
- Save/remember last used grid size across app sessions (may be added later)
- Different grid sizes for different game modes
