package dk.sdu.sem.levelsystem.parsing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LayerDTO {
	@JsonProperty("data")
	public List<Integer> data;

	@JsonProperty("name")
	public String name;

	@JsonProperty("width")
	public int width;

	@JsonProperty("height")
	public int height;
}
