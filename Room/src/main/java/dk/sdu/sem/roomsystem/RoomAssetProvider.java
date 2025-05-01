package dk.sdu.sem.roomsystem;

import dk.sdu.sem.gamesystem.assets.AssetFacade;
import dk.sdu.sem.gamesystem.assets.providers.IAssetProvider;
import dk.sdu.sem.gamesystem.rendering.SpriteMap;

public class RoomAssetProvider implements IAssetProvider {
	static SpriteMap forceFieldMap;

	@Override
	public void provideAssets() {
		forceFieldMap = AssetFacade.createSpriteMap("force-field")
			.withImagePath("force-field-tileset.png")
			.withGrid(8,1,16,16)
			.load();
	}
}
