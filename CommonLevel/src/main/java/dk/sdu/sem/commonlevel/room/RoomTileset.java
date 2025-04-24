package dk.sdu.sem.commonlevel.room;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomTileset {
	@JsonProperty("image")
	public String imagePath;

	@JsonProperty("columns")
	public int columns;

	@JsonProperty("firstgid")
	public int firstgid;

	@JsonProperty("tileheight")
	public int tileHeight;

	@JsonProperty("tilewidth")
	public int tileWidth;

	@JsonProperty("tilecount")
	public int tileCount;

	@JsonProperty("tiles")
	public List<Tile> tiles = new ArrayList<>();

	public int rows() {
		return tileCount / columns;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Tile {
		@JsonProperty("id")
		public int id;

		@JsonProperty("properties")
		public List<Property> properties = new ArrayList<>();

		@JsonProperty("animation")
		public List<Frame> animation = new ArrayList<>();

		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Frame {
			@JsonProperty("tileid")
			public int tileId;

			@JsonProperty("duration") // milliseconds
			public int duration;
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Property {
			@JsonProperty("name")
			public String name;

			@JsonProperty("type")
			public String type;

			@JsonProperty("value")
			public Object value;
		}
	}
}
