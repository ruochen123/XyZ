package com.willing.xyz.entity;

public class Catelog
{
	private String name;
	private int count;
	
	public Catelog()
	{
		name = "δ֪";
		count = 0;
	}
	
	public Catelog(String n, int c)
	{
		name = n;
		count = c;
	}
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
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
		if (o instanceof Catelog)
		{
			return getName().equals(((Catelog)o).getName());
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return getName().hashCode();
	}


}
