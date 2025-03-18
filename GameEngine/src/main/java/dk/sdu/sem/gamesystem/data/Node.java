package dk.sdu.sem.gamesystem.data;

import dk.sdu.sem.gamesystem.components.IComponent;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

@Deprecated
public abstract class Node {
	public abstract boolean matches(Entity entity);
	public abstract Node createNode(Entity entity);
}
