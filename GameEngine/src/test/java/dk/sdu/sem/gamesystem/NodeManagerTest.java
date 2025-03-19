package dk.sdu.sem.gamesystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.NodeFactory;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.components.SpriteRendererComponent;
import dk.sdu.sem.gamesystem.components.TransformComponent;
import dk.sdu.sem.gamesystem.data.RenderNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NodeManagerTest {
	private NodeManager nodeManager;
	private NodeFactory nodeFactory;

	private Entity entity;

	@BeforeEach
	void setUp() {
		nodeFactory = new NodeFactory();
		nodeManager = new NodeManager(nodeFactory);

		nodeManager.registerNodeType(RenderNode.class, Set.of(TransformComponent.class, SpriteRendererComponent.class));

		entity = new Entity();
	}

	@Test
	void testEntityWithoutComponentsIsNotAddedToNodeCollection() {
		// Without required components
		nodeManager.processEntity(entity);

		// Entity should not be in the RenderNode collection
		assertTrue(nodeManager.getNodeEntities(RenderNode.class).isEmpty());
	}

	@Test
	void testEntityWithRequiredComponentsIsAddedToNodeCollection() {
		// Add required components
		entity.addComponent(TransformComponent.class,
			new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1)));
		entity.addComponent(SpriteRendererComponent.class, new SpriteRendererComponent());

		// Process entity
		nodeManager.processEntity(entity);

		// Entity should be in the RenderNode collection
		Set<Entity> renderNodeEntities = nodeManager.getNodeEntities(RenderNode.class);
		assertEquals(1, renderNodeEntities.size());
		assertTrue(renderNodeEntities.contains(entity));
	}

	@Test
	void testRemovingComponentRemovesEntityFromNodeCollection() {
		// Required components
		entity.addComponent(TransformComponent.class, new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1)));
		entity.addComponent(SpriteRendererComponent.class, new SpriteRendererComponent());

		nodeManager.processEntity(entity);

		// Entity should be in the collection
		assertTrue(nodeManager.getNodeEntities(RenderNode.class).contains(entity));

		// Remove a required component
		entity.removeComponent(SpriteRendererComponent.class);
		nodeManager.onComponentRemoved(entity, SpriteRendererComponent.class);

		// Entity should not be in the collection
		assertFalse(nodeManager.getNodeEntities(RenderNode.class).contains(entity));
	}

	@Test
	void testCreateNodeForEntity() {
		// Add components
		entity.addComponent(TransformComponent.class,
			new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1)));
		entity.addComponent(SpriteRendererComponent.class, new SpriteRendererComponent());

		// Create node for entity
		RenderNode node = nodeManager.createNodeForEntity(RenderNode.class, entity);

		// Node should be created as expected
		assertNotNull(node);
		assertTrue(node.getRequiredComponents().contains(TransformComponent.class));
		assertTrue(node.getRequiredComponents().contains(SpriteRendererComponent.class));
		assertEquals(entity, node.getEntity());
	}

	@Test
	void testCreateNodeReturnsNullForIncompatibleEntity() {
		// Add only one of the required components
		entity.addComponent(TransformComponent.class,
			new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1)));

		// Create node for entity
		RenderNode node = nodeManager.createNodeForEntity(RenderNode.class, entity);

		// Node should be null since entity doesn't have all required components
		assertNull(node);
	}

	@Test
	void testClear() {
		// Add components
		entity.addComponent(TransformComponent.class, new TransformComponent(new Vector2D(0, 0), 0, new Vector2D(1, 1)));
		entity.addComponent(SpriteRendererComponent.class, new SpriteRendererComponent());

		nodeManager.processEntity(entity);

		// Entity should be in the collection
		assertFalse(nodeManager.getNodeEntities(RenderNode.class).isEmpty());

		// Clear all collections
		nodeManager.clear();

		// Collections should be empty
		assertTrue(nodeManager.getNodeEntities(RenderNode.class).isEmpty());
	}
}