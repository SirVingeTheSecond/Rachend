package dk.sdu.sem.collisionsystem;

import dk.sdu.sem.collision.*;
import dk.sdu.sem.commonsystem.NodeManager;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.gamesystem.Time;
import dk.sdu.sem.gamesystem.components.TilemapComponent;
import dk.sdu.sem.gamesystem.services.IFixedUpdate;

import java.util.Set;

/**
 * System that handles collision detection and resolution.
 */
public class CollisionSystem implements ICollisionSPI, IFixedUpdate {

    @Override
    public void fixedUpdate() {
        // Process all entity-tilemap collisions
        handleEntityTilemapCollisions();
    }

    /**
     * Processes collisions between entities and tilemaps.
     */
    private void handleEntityTilemapCollisions() {
        // Get all entities with physics and colliders
        Set<PhysicsColliderNode> physicsNodes = NodeManager.active().getNodes(PhysicsColliderNode.class);

        // Get all tilemap colliders
        Set<TilemapColliderNode> tilemapNodes = NodeManager.active().getNodes(TilemapColliderNode.class);

        // Skip if either set is empty
        if (physicsNodes.isEmpty() || tilemapNodes.isEmpty()) {
            return;
        }

        // Process each physics entity against each tilemap
        for (PhysicsColliderNode physicsNode : physicsNodes) {
            // Skip if not moving
            if (physicsNode.physicsComponent.getVelocity().magnitudeSquared() == 0) {
                continue;
            }

            // Calculate proposed position after movement
            Vector2D currentPos = physicsNode.transform.getPosition();
            Vector2D velocity = physicsNode.physicsComponent.getVelocity();
            Vector2D proposedPos = currentPos.add(
                    velocity.scale((float)Time.getFixedDeltaTime())
            );

            // Check against all tilemaps
            boolean collision = false;
            for (TilemapColliderNode tilemapNode : tilemapNodes) {
                if (wouldCollideWithTilemap(physicsNode, tilemapNode, proposedPos)) {
                    collision = true;
                    break;
                }
            }

            // If collision detected, zero out velocity
            if (collision) {
                physicsNode.physicsComponent.setVelocity(new Vector2D(0, 0));
            }
        }
    }

    /**
     * Checks if an entity would collide with a tilemap at the proposed position.
     *
     * @param entityNode The entity node
     * @param tilemapNode The tilemap node
     * @param proposedPos The proposed position
     * @return True if collision would occur
     */
    private boolean wouldCollideWithTilemap(PhysicsColliderNode entityNode,
                                            TilemapColliderNode tilemapNode,
                                            Vector2D proposedPos) {
        // Get tilemap properties
        TilemapComponent tilemap = tilemapNode.tilemap;
        TilemapColliderComponent tilemapCollider = tilemapNode.tilemapCollider;
        Vector2D tilemapPos = tilemapNode.transform.getPosition();
        int tileSize = tilemap.getTileSize();

        // Get entity collider properties
        ColliderComponent collider = entityNode.collider;

        // For a CircleShape, we need to check surrounding tiles
        if (collider.getCollisionShape() instanceof CircleShape) {
            CircleShape circleShape = (CircleShape) collider.getCollisionShape();
            float radius = circleShape.getRadius();

            // Calculate world position (entity position + collider offset)
            Vector2D worldPos = proposedPos.add(collider.getOffset());

            // Calculate tilemap-relative position
            Vector2D relativePos = worldPos.subtract(tilemapPos);

            // Find the tile coordinates
            int centerTileX = (int)(relativePos.getX() / tileSize);
            int centerTileY = (int)(relativePos.getY() / tileSize);

            // Calculate how many tiles to check based on radius
            int tilesCheck = (int)Math.ceil(radius / tileSize) + 1;

            // Check surrounding tiles
            for (int y = centerTileY - tilesCheck; y <= centerTileY + tilesCheck; y++) {
                for (int x = centerTileX - tilesCheck; x <= centerTileX + tilesCheck; x++) {
                    // Skip if this tile is not solid
                    if (!tilemapCollider.isSolid(x, y)) {
                        continue;
                    }

                    // Calculate tile position in world space
                    Vector2D tilePos = tilemapPos.add(new Vector2D(
                            x * tileSize,
                            y * tileSize
                    ));

                    // Create tile shape
                    RectangleShape tileShape = new RectangleShape(
                            tilePos,
                            tileSize,
                            tileSize
                    );

                    // Check for collision with proposed position
                    CircleShape proposedCircle = circleShape.withCenter(worldPos);
                    if (proposedCircle.intersects(tileShape)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean checkCollision(ICollider a, ICollider b) {
        return a.getCollisionShape().intersects(b.getCollisionShape());
    }

    @Override
    public boolean checkTileCollision(ICollider collider, int tileX, int tileY, int tileSize) {
        // Create a rectangle shape for the tile
        RectangleShape tileShape = new RectangleShape(
                new Vector2D(tileX * tileSize, tileY * tileSize),
                tileSize,
                tileSize
        );

        return collider.getCollisionShape().intersects(tileShape);
    }

    @Override
    public boolean isPositionValid(ICollider collider, Vector2D proposedPosition) {
        // Get all tilemap colliders
        Set<TilemapColliderNode> tilemapNodes = NodeManager.active().getNodes(TilemapColliderNode.class);

        // If no tilemaps, position is valid
        if (tilemapNodes.isEmpty()) {
            return true;
        }

        // Check against all tilemaps
        for (TilemapColliderNode tilemapNode : tilemapNodes) {
            // Get tilemap properties
            TilemapComponent tilemap = tilemapNode.tilemap;
            TilemapColliderComponent tilemapCollider = tilemapNode.tilemapCollider;
            Vector2D tilemapPos = tilemapNode.transform.getPosition();
            int tileSize = tilemap.getTileSize();

            // For a CircleShape, we need to check surrounding tiles
            if (collider.getCollisionShape() instanceof CircleShape) {
                CircleShape circleShape = (CircleShape) collider.getCollisionShape();
                CircleShape proposedCircle = circleShape.withCenter(proposedPosition);
                float radius = circleShape.getRadius();

                // Calculate tilemap-relative position
                Vector2D relativePos = proposedPosition.subtract(tilemapPos);

                // Find the tile coordinates
                int centerTileX = (int)(relativePos.getX() / tileSize);
                int centerTileY = (int)(relativePos.getY() / tileSize);

                // Calculate how many tiles to check based on radius
                int tilesCheck = (int)Math.ceil(radius / tileSize) + 1;

                // Check surrounding tiles
                for (int y = centerTileY - tilesCheck; y <= centerTileY + tilesCheck; y++) {
                    for (int x = centerTileX - tilesCheck; x <= centerTileX + tilesCheck; x++) {
                        // Skip if this tile is not solid
                        if (!tilemapCollider.isSolid(x, y)) {
                            continue;
                        }

                        // Calculate tile position in world space
                        Vector2D tilePos = tilemapPos.add(new Vector2D(
                                x * tileSize,
                                y * tileSize
                        ));

                        // Create tile shape
                        RectangleShape tileShape = new RectangleShape(
                                tilePos,
                                tileSize,
                                tileSize
                        );

                        // Check for collision with proposed position
                        if (proposedCircle.intersects(tileShape)) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }
}