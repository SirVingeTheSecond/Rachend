package dk.sdu.sem;

import dk.sdu.sem.commonsystem.INodeProvider;

public class MeleeEnemyNodeProvider implements INodeProvider<MeleeEnemyNode>{

	@Override
	public Class getNodeType() {
		return MeleeEnemyNode.class;
	}

	@Override
	public MeleeEnemyNode create() {
		return new MeleeEnemyNode();
	}


}
