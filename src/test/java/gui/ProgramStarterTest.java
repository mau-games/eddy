package gui;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import runners.ParameterGUI;

public class ProgramStarterTest {

	ParameterGUI program;
	
	@Before
	public void setup() {
		program = new ParameterGUI();
	}
	
	@Test
	public void testMain() {
		assertNotNull(program);
	}

}
