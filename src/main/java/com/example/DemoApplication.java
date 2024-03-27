package com.example;

import java.io.IOException;
import java.nio.file.Paths;

public class DemoApplication {

	public static void main(String[] args) throws IOException {
		SaveFile file = SaveFile.from(Paths.get("ER0000.sl2"));
		System.err.println(file.prettyPrint());
	}
}