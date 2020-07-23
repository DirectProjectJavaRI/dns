package org.nhindirect.dns.tools;

public abstract class StopabbleLoopedJob implements Runnable
{
	protected boolean running;
	
	public StopabbleLoopedJob()
	{
		running = true;	
	}
	
	@Override
	public void run()
	{
		while(isRunning())
		{
			executeJob();
		}
	}
	
	public synchronized void setRunning(boolean running)
	{
		this.running = running;
	}
	
	public synchronized boolean isRunning()
	{
		return running;
	}
	
	public abstract void executeJob();
}
