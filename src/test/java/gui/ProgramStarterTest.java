package gui;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ProgramStarterTest {

	ProgramStarter program;
	
	@Before
	public void setup() {
		program = new ProgramStarter();
	}
	
	@Test
	public void testMain() {
		assertNotNull(program);
	}

}
