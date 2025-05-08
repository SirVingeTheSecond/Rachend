package dk.sdu.sem.props;

import com.fasterxml.jackson.annotation.*;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Prop {
	@JsonProperty("name")
	public String name;
	@JsonProperty("spritePath")
	public String spritePath;
	@JsonProperty("brokenSpritePath")
	public String brokenSpritePath;
	@JsonProperty("collisionShape")
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@type")
	@JsonIgnoreProperties("bounds")
	public ICollisionShape collisionShape;
	@JsonProperty("minSize")
	public float minSize;
	@JsonProperty("maxSize")
	public float maxSize;

	@JsonIgnore
	private SpriteReference spriteReference;
	@JsonIgnore
	private SpriteReference brokenSpriteReference;

	@JsonSetter("spritePath")
	public void setSpritePath(String spritePath) {
		this.spritePath = spritePath;

		//Register sprite
		AssetFacade.createSprite(name)
			.withImagePath(spritePath)
			.load();

		spriteReference = new SpriteReference(name + "_sprite");
	}

	@JsonSetter("brokenSpritePath")
	public void setBrokenSpritePath(String brokenSpritePath) {
		this.brokenSpritePath = brokenSpritePath;

		//Breakable sprite
		if (brokenSpritePath != null && !brokenSpritePath.isEmpty()) {
			AssetFacade.createSprite(name + "_broken")
				.withImagePath(brokenSpritePath)
				.load();

			brokenSpriteReference = new SpriteReference(name + "_broken_sprite");
		}
	}

	@JsonIgnore
	public SpriteReference getSpriteReference() {
		return spriteReference;
	}

	@JsonIgnore
	public SpriteReference getBrokenSpriteReference() {
		return brokenSpriteReference;
	}

	public Prop() {

	}

	public Prop(
		String spritePath,
		ICollisionShape collisionShape,
		float minSize,
		float maxSize
	) {
		this.spritePath = spritePath;
		this.collisionShape = collisionShape;
		this.minSize = minSize;
		this.maxSize = maxSize;
	}
}
