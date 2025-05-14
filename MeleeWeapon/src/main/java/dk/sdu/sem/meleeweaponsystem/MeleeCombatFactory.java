package dk.sdu.sem.meleeweaponsystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.TransformComponent;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.assets.references.IAssetReference;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;
import dk.sdu.sem.gamesystem.components.AnimatorComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.rendering.Sprite;
import dk.sdu.sem.logging.Logging;
import dk.sdu.sem.logging.LoggingLevel;

public class MeleeCombatFactory {
	private static final Logging LOGGER = Logging.createLogger("MeleeCombatFactory", LoggingLevel.DEBUG);

	public Entity createMeleeEffect(Vector2D position, Vector2D direction, float attackScale, Entity owner) {
		Entity meleeEffect = new Entity();

		float rotation = direction.angle();

		Vector2D effectPosition = position.add(direction.scale(attackScale * 0.6f));
		TransformComponent transform = new TransformComponent(
			effectPosition,
			rotation,
			new Vector2D(attackScale / 20f, attackScale / 20f)
		);
		meleeEffect.addComponent(transform);

		IAssetReference<Sprite> defaultSpriteRef = new SpriteReference("sweep_anim_f0_sprite");

		// Add sprite renderer with the initial frame
		SpriteRendererComponent renderer = new SpriteRendererComponent(defaultSpriteRef);
		renderer.setRenderLayer(GameConstants.LAYER_EFFECTS);
		meleeEffect.addComponent(renderer);

		// Create animator component with states
		AnimatorComponent animator = new AnimatorComponent();

		// Add animation states
		animator.addState("telegraph", "beg_partialSwipe");
		animator.addState("strike", "melee_swipe");

		// Set initial state
		animator.setCurrentState("telegraph");

		// Add transition parameter that we'll set in the MeleeSystem
		animator.addTransition("telegraph", "strike", "shouldStrike", true);

		meleeEffect.addComponent(animator);

		// Add melee effect component
		MeleeEffectComponent effectComponent = new MeleeEffectComponent(
			0.5f, // lifetime in seconds
			owner,
			attackScale
		);
		meleeEffect.addComponent(effectComponent);

		return meleeEffect;
	}
}