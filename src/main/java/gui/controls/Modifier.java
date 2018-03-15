package gui.controls;

import javafx.scene.control.ToggleButton;

public class Modifier
{
	private boolean mainActivation;
	private boolean alternateActivation;
	private ToggleButton button;
	
	public Modifier(ToggleButton refButton)
	{
		mainActivation = false;
		alternateActivation = false;
		this.button = refButton;
	}
	
	public Modifier(boolean mainActivation, boolean alternateActivation)
	{
		this.mainActivation = mainActivation;
		this.alternateActivation = alternateActivation;
		this.button = null;
	}
	
	public Modifier(boolean mainActivation)
	{
		this.mainActivation = mainActivation;
		this.alternateActivation = false;
		this.button = null;
	}
	
	public Modifier(boolean mainActivation, ToggleButton refButton)
	{
		this.button = refButton;
		this.mainActivation = mainActivation;
		this.alternateActivation = false;
	}
	
	public boolean GetAlternate()
	{
		return alternateActivation;
	}
	
	public boolean GetMain()
	{
		return mainActivation;
	}
	
	public boolean GetActive()
	{
		return mainActivation || alternateActivation;
	}
	
	public void SetMainActive(boolean value)
	{
		mainActivation = value;
		
		if(button != null)
			button.setSelected(value);
	}
	
	public void SetAlternateActive(boolean value)
	{
		alternateActivation = value;
		
		if(button != null)
		{
			button.setSelected(value);
			if(!value)
			{
				button.setSelected(mainActivation);
			}
		}
	}
}
