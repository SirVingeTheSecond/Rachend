package dk.sdu.sem.levelsystem.systems;

import dk.sdu.sem.commonlevel.Direction;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonsystem.events.EventDispatcher;
import dk.sdu.sem.commonsystem.events.EventListener;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.scenes.SceneManager;
import dk.sdu.sem.gamesystem.services.IUpdate;
import dk.sdu.sem.commonlevel.components.RoomComponent;
import dk.sdu.sem.commonlevel.components.SceneRenderStateComponent;
import dk.sdu.sem.commonlevel.components.TransitionComponent;
import dk.sdu.sem.commonlevel.events.TransitionCompleteEvent;
import dk.sdu.sem.commonlevel.events.TransitionStartEvent;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;
import dk.sdu.sem.player.PlayerComponent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * System that manages transitions between rooms.
 * Focuses only on transition logic without direct rendering knowledge.
 */
public class TransitionSystem implements IUpdate, EventListener<TransitionStartEvent> {
	private static final Logging LOGGER = Logging.createLogger("TransitionSystem", LoggingLevel.DEBUG);

	// Transition parameters
	private static final float ROOM_TRANSITION_DURATION = 0.4f;
	private static final float PLAYER_ENTRANCE_DURATION = 0.4f;

	// Transition state type
	private enum TransitionPhase {
		NONE,           // No transition active
		ROOM_SLIDING,   // Rooms are sliding (main transition)
		PLAYER_ENTRANCE // Player is entering the new room
	}

	// Current active transitions
	private final Map<Entity, TransitionState> activeTransitions = new HashMap<>();

	public TransitionSystem() {
		LOGGER.debug("TransitionSystem constructor called");
		EventDispatcher.getInstance().addListener(TransitionStartEvent.class, this);
	}

	@Override
	public void onEvent(TransitionStartEvent event) {
		// Start a new transition
		Entity fromRoom = event.getFromRoom();
		Entity toRoom = event.getToRoom();
		Direction direction = event.getDirection();
		Entity transitionEntity = event.getTransitionEntity();

		LOGGER.debug("Received TransitionStartEvent for entity " + transitionEntity.getID());

		// Create transition state
		TransitionState state = new TransitionState(
			fromRoom, toRoom, direction, transitionEntity, TransitionPhase.ROOM_SLIDING
		);

		// Add to active transitions
		activeTransitions.put(transitionEntity, state);

		// Add/update transition component on the entity
		TransitionComponent transComp = transitionEntity.getComponent(TransitionComponent.class);
		if (transComp == null) {
			transComp = new TransitionComponent();
			transitionEntity.addComponent(transComp);
		}
		transComp.setTransitioning(true);

		// Disable input on the transition entity if it has a player component
		if (transitionEntity.hasComponent(PlayerComponent.class)) {
			PlayerComponent playerComp = transitionEntity.getComponent(PlayerComponent.class);
			playerComp.setInputEnabled(false);
		}

		LOGGER.debug("Started transition for entity " + transitionEntity.getID() +
			" from room " + fromRoom.getComponent(RoomComponent.class).getRoomId() +
			" to room " + toRoom.getComponent(RoomComponent.class).getRoomId() +
			" in direction " + direction);
	}

	@Override
	public void update() {
		if (activeTransitions.isEmpty()) {
			return;
		}

		float deltaTime = (float) Time.getDeltaTime();

		// Process all active transitions
		Iterator<Map.Entry<Entity, TransitionState>> iterator = activeTransitions.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Entity, TransitionState> entry = iterator.next();
			Entity entity = entry.getKey();
			TransitionState state = entry.getValue();

			// Update the transition based on its current phase
			boolean completed = updateTransition(entity, state, deltaTime);

			// If transition is complete, remove it
			if (completed) {
				iterator.remove();
			}
		}
	}

	/**
	 * Updates a single transition state
	 * @return true if the transition is complete
	 */
	private boolean updateTransition(Entity entity, TransitionState state, float deltaTime) {
		// Get the transition duration based on phase
		float duration = state.phase == TransitionPhase.ROOM_SLIDING ?
			ROOM_TRANSITION_DURATION : PLAYER_ENTRANCE_DURATION;

		// Update progress
		state.progress += deltaTime / duration;
		state.progress = Math.min(state.progress, 1.0f);

		// Handle phase-specific updates
		switch (state.phase) {
			case ROOM_SLIDING:
				updateRoomSliding(entity, state);

				// If this phase is complete, move to next
				if (state.progress >= 1.0f) {
					completeRoomSliding(entity, state);
					return false; // Not fully completed yet
				}
				break;

			case PLAYER_ENTRANCE:
				updatePlayerEntrance(entity, state);

				// If this phase is complete, end transition
				if (state.progress >= 1.0f) {
					completePlayerEntrance(entity, state);
					return true; // Transition fully completed
				}
				break;

			default:
				return true; // Unknown phase, consider it complete
		}

		return false; // Transition still in progress
	}

	/**
	 * Updates the room sliding phase of a transition
	 */
	private void updateRoomSliding(Entity entity, TransitionState state) {
		// Apply easing for smoother motion
		float easedProgress = ease(state.progress);

		// Get room dimensions
		float roomWidth = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x();
		float roomHeight = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y();

		// Calculate offsets for both rooms
		Vector2D fromOffset = Vector2D.ZERO.lerp(
			state.direction.getOpposite().getRoomOffset(roomWidth, roomHeight),
			easedProgress
		);

		Vector2D toOffset = state.direction.getRoomOffset(roomWidth, roomHeight).lerp(
			Vector2D.ZERO,
			easedProgress
		);

		// Update the SceneRenderStateComponents
		SceneRenderStateComponent fromRenderState = state.fromRoom.getComponent(SceneRenderStateComponent.class);
		SceneRenderStateComponent toRenderState = state.toRoom.getComponent(SceneRenderStateComponent.class);

		if (fromRenderState != null) {
			fromRenderState.setPosition(fromOffset);
		}

		if (toRenderState != null) {
			toRenderState.setPosition(toOffset);
		}
	}

	/**
	 * Completes the room sliding phase and begins player entrance
	 */
	private void completeRoomSliding(Entity entity, TransitionState state) {
		LOGGER.debug("Room sliding complete, beginning player entrance");

		// Make target room active
		Entity toRoom = state.toRoom;
		SceneManager.getInstance().setActiveScene(toRoom.getScene());

		// Prepare for player entrance
		setupPlayerEntrancePhase(entity, state);

		// Deactivate room rendering states
		SceneRenderStateComponent fromRenderState = state.fromRoom.getComponent(SceneRenderStateComponent.class);
		if (fromRenderState != null) {
			fromRenderState.setActive(false);
		}

		SceneRenderStateComponent toRenderState = state.toRoom.getComponent(SceneRenderStateComponent.class);
		if (toRenderState != null) {
			toRenderState.setActive(false);
		}

		// Next phase
		state.phase = TransitionPhase.PLAYER_ENTRANCE;
		state.progress = 0.0f;
	}

	/**
	 * Sets up the player entrance animation
	 */
	private void setupPlayerEntrancePhase(Entity entity, TransitionState state) {
		TransformComponent transform = entity.getComponent(TransformComponent.class);
		if (transform == null) {
			LOGGER.error("Entity has no TransformComponent for entrance animation");
			return;
		}

		// Get the target room component
		RoomComponent roomComponent = state.toRoom.getComponent(RoomComponent.class);
		if (roomComponent == null) {
			LOGGER.error("Target room has no RoomComponent");
			return;
		}

		// Get entrance position from opposite direction
		Direction entryDirection = state.direction.getOpposite();
		Vector2D entrancePos = roomComponent.getEntrance(entryDirection);

		if (entrancePos == null) {
			LOGGER.error("No entrance position found for direction " + entryDirection);
			float roomWidth = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.x();
			float roomHeight = GameConstants.TILE_SIZE * GameConstants.WORLD_SIZE.y();
			entrancePos = new Vector2D(roomWidth / 2, roomHeight / 2); // Fallback
		}

		// Calculate offset position outside the entrance
		float offset = GameConstants.TILE_SIZE * 2;
		Vector2D offsetPos = entrancePos.add(entryDirection.getUnitVector().scale(offset));

		// Set start and target positions
		state.startPosition = offsetPos;
		state.targetPosition = entrancePos;

		// Set entity position
		transform.setPosition(offsetPos);

		// Reset entity velocity
		PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
		if (physics != null) {
			physics.setVelocity(Vector2D.ZERO);
		}

		// Update transition component
		TransitionComponent transComp = entity.getComponent(TransitionComponent.class);
		if (transComp != null) {
			transComp.setStartPosition(offsetPos);
			transComp.setTargetPosition(entrancePos);
		}

		LOGGER.debug("Player entrance animation from " + offsetPos + " to " + entrancePos);
	}

	/**
	 * Updates the player entrance phase of a transition
	 */
	private void updatePlayerEntrance(Entity entity, TransitionState state) {
		TransformComponent transform = entity.getComponent(TransformComponent.class);
		if (transform == null) {
			return;
		}

		// Apply easing for smoother motion
		float easedProgress = easeOut(state.progress);

		// Calculate new position
		Vector2D newPosition = state.startPosition.lerp(state.targetPosition, easedProgress);

		// Update entity position
		transform.setPosition(newPosition);

		// Update transition component
		TransitionComponent transComp = entity.getComponent(TransitionComponent.class);
		if (transComp != null) {
			transComp.setTransitionProgress(easedProgress);
		}
	}

	/**
	 * Completes the player entrance phase, ending the transition
	 */
	private void completePlayerEntrance(Entity entity, TransitionState state) {
		LOGGER.debug("Player entrance complete, transition ending");

		// Ensure entity is at final position
		TransformComponent transform = entity.getComponent(TransformComponent.class);
		if (transform != null) {
			transform.setPosition(state.targetPosition);
		}

		// Re-enable input if it's a player
		if (entity.hasComponent(PlayerComponent.class)) {
			PlayerComponent playerComp = entity.getComponent(PlayerComponent.class);
			playerComp.setInputEnabled(true);
		}

		// Clear transition component state
		TransitionComponent transComp = entity.getComponent(TransitionComponent.class);
		if (transComp != null) {
			transComp.setTransitioning(false);
			transComp.setTransitionProgress(0f);
		}

		// Dispatch transition complete event
		EventDispatcher.getInstance().dispatch(
			new TransitionCompleteEvent(state.toRoom, entity)
		);
	}

	/**
	 * Apply easing function for smoother transitions
	 */
	private static float ease(float t) {
		// Cubic easing: smooths the start and end of the transition
		return t < 0.5f ? 4 * t * t * t : 1 - (float)Math.pow(-2 * t + 2, 3) / 2;
	}

	/**
	 * Apply ease-out function for player entrance
	 */
	private static float easeOut(float t) {
		// Cubic ease-out: starts fast and slows down
		return 1 - (float)Math.pow(1 - t, 3);
	}

	/**
	 * Checks if an entity is currently transitioning
	 */
	public boolean isTransitioning(Entity entity) {
		return activeTransitions.containsKey(entity);
	}

	/**
	 * State class for tracking transition state
	 */
	private static class TransitionState {
		final Entity fromRoom;
		final Entity toRoom;
		final Direction direction;
		final Entity entity;
		TransitionPhase phase;
		float progress;
		Vector2D startPosition;
		Vector2D targetPosition;

		TransitionState(Entity fromRoom, Entity toRoom, Direction direction,
						Entity entity, TransitionPhase phase) {
			this.fromRoom = fromRoom;
			this.toRoom = toRoom;
			this.direction = direction;
			this.entity = entity;
			this.phase = phase;
			this.progress = 0.0f;
		}
	}
}