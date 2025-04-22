package dk.sdu.sem.props;

import com.fasterxml.jackson.annotation.*;
import dk.sdu.sem.collision.shapes.ICollisionShape;
import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.references.SpriteReference;


public class Prop {
	@JsonProperty("name")
	public String name;
	@JsonProperty("spritePath")
	public String spritePath;
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

	@JsonSetter("spritePath")
	public void setSpritePath(String spritePath) {
		this.spritePath = spritePath;

		//Register sprite
		AssetFacade.createSprite(name)
			.withImagePath(spritePath)
			.load();

		spriteReference = new SpriteReference(name + "_sprite");
	}

	@JsonIgnore
	public SpriteReference getSpriteReference() {
		return spriteReference;
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
