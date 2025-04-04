package dk.sdu.sem.commonlevel.room;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomData {
	@JsonProperty("layers")
	public List<RoomLayer> layers;

	@JsonProperty("tilesets")
	public List<RoomTileset> tilesets;

	@JsonProperty("height")
	public int height;

	@JsonProperty("width")
	public int width;
}

