package dk.sdu.sem.commonlevel.room;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomData {
	@JsonProperty("layers")
	public List<RoomLayer> layers;

	@JsonSetter("tilesets")
	public void setTilesetInfo(List<TilesetInfo> t) {
		ObjectMapper mapper = new ObjectMapper();
		tilesets.clear();
		for (TilesetInfo tileset : t) {
			try {
				String[] split = tileset.source.split("/");
				String fileName = split[split.length - 1];

				RoomTileset roomTileset = mapper.readValue(new File("Levels/tilesets/" + fileName), RoomTileset.class);
				tilesets.add(roomTileset);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	public List<RoomTileset> tilesets = new ArrayList<>();

	@JsonProperty("height")
	public int height;

	@JsonProperty("width")
	public int width;

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class TilesetInfo {
		@JsonProperty("source")
		public String source;
	}
}

