package dk.sdu.sem.props;

import dk.sdu.sem.collision.ITriggerListener;
import dk.sdu.sem.collision.components.CircleColliderComponent;
import dk.sdu.sem.collision.events.*;
import dk.sdu.sem.commonstats.StatType;
import dk.sdu.sem.commonstats.StatsComponent;
import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.IComponent;
import dk.sdu.sem.commonweapon.BulletComponent;
import dk.sdu.sem.gamesystem.GameConstants;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;
import dk.sdu.sem.gamesystem.components.PhysicsComponent;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;

import java.util.function.Consumer;

public class PropBreakComponent implements IComponent {
	private SpriteReference brokenSprite;

	public PropBreakComponent(SpriteReference brokenSprite) {
		this.brokenSprite = brokenSprite;
	}

    public SpriteReference getBrokenSprite() {
        return brokenSprite;
    }

    public void setBrokenSprite(SpriteReference brokenSprite) {
        this.brokenSprite = brokenSprite;
    }
}
