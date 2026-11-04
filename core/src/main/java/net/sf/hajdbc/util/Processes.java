package net.sf.hajdbc.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import net.sf.hajdbc.logging.Level;
import net.sf.hajdbc.logging.Logger;
import net.sf.hajdbc.logging.LoggerFactory;
import net.sf.hajdbc.messages.Messages;
import net.sf.hajdbc.messages.MessagesFactory;

public class Processes
{
	private static final Messages messages = MessagesFactory.getMessages();
	private static final Logger logger = LoggerFactory.getLogger(Processes.class);

	public static Map<String, String> environment(final ProcessBuilder builder)
	{
		return builder.environment();
	}
	
	public static void run(final ProcessBuilder processBuilder) throws Exception
	{
		processBuilder.redirectErrorStream(true);
		
		logger.log(Level.DEBUG, Strings.join(processBuilder.command(), " "));
		
		Process process = processBuilder.start();
		
		try
		{
			int status = process.waitFor();
			
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line = reader.readLine();
				while (line != null)
				{
					logger.log(Level.DEBUG, line);
					line = reader.readLine();
				}
			}
			
			if (status != 0)
			{
				throw new Exception(messages.status(processBuilder, status));
			}
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			
			throw new Exception(processBuilder.toString(), e);
		}
	}

	private Processes()
	{
		// Hide
	}
}
