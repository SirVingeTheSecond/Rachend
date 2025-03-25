package dk.sdu.sem.gamesystem.data;

import dk.sdu.sem.commonsystem.INodeProvider;

public class AnimatorNodeProvider implements INodeProvider<AnimatorNode> {
	@Override
	public Class<AnimatorNode> getNodeType() {
		return AnimatorNode.class;
	}

	@Override
	public AnimatorNode create() {
		return new AnimatorNode();
	}
}