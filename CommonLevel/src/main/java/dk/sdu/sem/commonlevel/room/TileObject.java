package dk.sdu.sem.commonlevel.room;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TileObject {
	public double height;
	public String name;
	public double width;
	public double x;
	public double y;
	public boolean point;
}
