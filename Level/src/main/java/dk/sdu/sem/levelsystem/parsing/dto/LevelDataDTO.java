package dk.sdu.sem.levelsystem.parsing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LevelDataDTO {
	@JsonProperty("layers")
	public List<LayerDTO> layers;

	@JsonProperty("tilesets")
	public List<TilesetDTO> tilesets;

	@JsonProperty("height")
	public int height;

	@JsonProperty("width")
	public int width;


}

;

