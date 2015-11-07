package com.willing.xyz.entity;


public class Singer
{
	private String name;
	private int count;
	
	public Singer()
	{
		name = "δ֪";
		count = 0;
	}
	
	public Singer(String n, int c)
	{
		name = getString(n);
		count = c;
	}
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = getString(name);
	}
	public int getCount()
	{
		return count;
	}
	public void setCount(int count)
	{
		this.count = count;
	}
	
	public void inc()
	{
		count++;
	}
	
	public String getString(String str)
	{
		if (str.trim() == "")
		{
			return "δ֪";
		}
		return str;
	}
	
	@Override
	public boolean equals(Object o)
	{
		
		if (o == null)
		{
			return false;
		}
		if (o == this)
		{
			return true;
		}
		if (o instanceof Singer)
		{
			return getName().equals(((Singer)o).getName());
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return getName().hashCode();
	}


}
