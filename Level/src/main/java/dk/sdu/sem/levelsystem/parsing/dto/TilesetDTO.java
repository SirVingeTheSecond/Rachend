package dk.sdu.sem.levelsystem.parsing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TilesetDTO {
	@JsonProperty("image")
	public String imagePath;

	@JsonProperty("columns")
	public int columns;

	@JsonProperty("tileheight")
	public int tileHeight;

	@JsonProperty("tilewidth")
	public int tileWidth;

	@JsonProperty("tilecount")
	public int tileCount;

	@JsonProperty("tiles")
	public List<Tile> tiles;

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Tile {
		@JsonProperty("id")
		public int id;

		@JsonProperty("properties")
		public List<Property> properties;

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
