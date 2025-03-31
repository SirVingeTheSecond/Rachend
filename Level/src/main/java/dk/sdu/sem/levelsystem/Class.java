package dk.sdu.sem.levelsystem;

import dk.sdu.sem.levelsystem.parsing.LevelParser;

import java.io.File;

public class Class {
	public static void main(String[] args) {
		LevelParser parser = new LevelParser();
		parser.parse(new File("C:\\Users\\davi3\\Documents\\Levels\\stage1\\leveldata.json"));
	}
}
