package dk.sdu.sem.commonlevel.room;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomLayer {
	@JsonProperty("data")
	public List<Integer> data;

	@JsonProperty("name")
	public String name;

	@JsonProperty("width")
	public int width;

	@JsonProperty("height")
	public int height;

	@JsonProperty("objects")
	public List<TileObject> objects;
}