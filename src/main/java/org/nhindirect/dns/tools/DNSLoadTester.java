package org.nhindirect.dns.tools;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.xbill.DNS.Cache;
import org.xbill.DNS.DClass;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

//@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class,HibernateJpaAutoConfiguration.class})
public class DNSLoadTester implements CommandLineRunner
{
	protected static final int DEFAULT_NUM_CERT_THREADS = 20;
	
	protected static final int DEFAULT_NUM_MX_THREADS = 20;
	
	protected String dnsServerTarget;
	
	protected String targetDomain;
	
	protected int numCertLookupThreads;
	
	protected int numMXLookupThreads;
	
	protected DNSLookupJob[] certLookupThreads;
	
	protected DNSLookupJob[] mxLookupThreads;
	
	protected long startTime;
	
    public static void main(String[] args) 
    {
    	final SpringApplication app = new SpringApplication(DNSLoadTester.class);
    	app.setWebApplicationType(WebApplicationType.NONE);
    	app.run(args);
    }
    
    public DNSLoadTester()
    {
    	numCertLookupThreads = DEFAULT_NUM_CERT_THREADS;
    	numMXLookupThreads = DEFAULT_NUM_MX_THREADS;
    }
    
    @Override
    public void run(String... args) 
    {
        for (int i = 0; i < args.length; i++)
        {
            final String arg = args[i];
            
            // Options
            if (!arg.startsWith("-"))
            {
                System.err.println("Error: Unexpected argument [" + arg + "]\n");
                printUsage();
                System.exit(-1);
            }
            else if (arg.equalsIgnoreCase("-server"))
            {
                if (i == args.length - 1 || args[i + 1].startsWith("-"))
                {
                    System.err.println("Error: Missing DNS server");
                    System.exit(-1);
                }
                
                dnsServerTarget = args[++i];
            }
            else if (arg.equals("-domain"))
            {
                if (i == args.length - 1 || args[i + 1].startsWith("-"))
                {
                    System.err.println("Error: Missing domain");
                    System.exit(-1);
                }
                targetDomain = args[++i];
            }
            else if (arg.equals("-numCertLookups"))
            {
                if (i == args.length - 1 || args[i + 1].startsWith("-"))
                {
                    System.err.println("Error: Missing number of certificate lookup threads");
                    System.exit(-1);
                }
                numCertLookupThreads = Integer.parseInt(args[++i]);
            }
            else if (arg.equals("-numMXLookups"))
            {
                if (i == args.length - 1 || args[i + 1].startsWith("-"))
                {
                    System.err.println("Error: Missing number of MX lookup threads");
                    System.exit(-1);
                }
                numMXLookupThreads = Integer.parseInt(args[++i]);
            }            
            else if (arg.equals("-help"))
            {
                printUsage();
                System.exit(-1);
            }            
            else
            {
                System.err.println("Error: Unknown argument " + arg + "\n");
                printUsage();
                System.exit(-1);
            }            
        }
        
        if (StringUtils.isEmpty(dnsServerTarget))
        {
        	System.err.println("You must provide a target DNS server.");
        	printUsage();
        	System.exit(-1);
        }
        
        if (StringUtils.isEmpty(targetDomain))
        {
        	System.err.println("You must provide a target domain to query.");
        	printUsage();
        	System.exit(-1);
        }
        
        startTime = System.currentTimeMillis();
        /*
         * Start up the lookup threads
         */
        certLookupThreads = new DNSLookupJob[numCertLookupThreads];
        for(int i = 0; i < numCertLookupThreads; ++ i)
        {
        	certLookupThreads[i] = new DNSLookupJob(Type.CERT, true);
        	final Thread thread = new Thread(certLookupThreads[i]);
        	thread.setDaemon(true);
        	thread.start();
        }
        
        mxLookupThreads = new DNSLookupJob[numMXLookupThreads];
        for(int i = 0; i < numCertLookupThreads; ++ i)
        {
        	mxLookupThreads[i] = new DNSLookupJob(Type.MX, false);
        	final Thread thread = new Thread(mxLookupThreads[i]);
        	thread.setDaemon(true);
        	thread.start();
        }
        
        String command = "";
        System.out.println("DNS queries running.");
        System.out.println("\tTarget DNS server: " + dnsServerTarget);       
        System.out.println("\tTarget DNS domain: " + targetDomain);
        System.out.println("\t" + numCertLookupThreads + " certificate lookup threads running.");
        System.out.println("\t" + numCertLookupThreads + " MX lookup threads running.");
        final Scanner scanner = new Scanner(System.in);
        while(command.compareToIgnoreCase("QUIT") != 0)
        {
        	System.out.print("\r\n>");
        	command = scanner.nextLine();
        	
        	if (command.compareToIgnoreCase("AVERAGE") == 0)
        	{
        		computeRunningAverage();
        	}
        }
        
        System.out.println("\r\nShutting down DNS running queries.\r\n");
        
        scanner.close();
        
        /*
         *  shut down the threads
         */
        for(StopabbleLoopedJob job : certLookupThreads)
        	job.setRunning(false);
        
        for(StopabbleLoopedJob job : mxLookupThreads)
        	job.setRunning(false);
        
        computeRunningAverage();
    }    
    
	protected ExtendedResolver createExResolver(String[] servers, int retries, int timeout, boolean useTCP)
	{
		// create a default ExtendedResolver
		final ExtendedResolver extendedResolver = new ExtendedResolver();


		// remove all resolvers from default ExtendedResolver
		Resolver[] resolvers = extendedResolver.getResolvers();
		if (!ArrayUtils.isEmpty(resolvers)) 
		{
			for (Resolver resolver : resolvers) 
			{
				extendedResolver.deleteResolver(resolver);
			}
		}

		// add the specified servers
		if (!ArrayUtils.isEmpty(servers)) 
		{
			for (String server : servers) 
			{
				// support for IP addresses instead of names
				server = server.replaceFirst("\\.$", "");

				try 
				{
					// create and add a SimpleResolver for each server
					SimpleResolver simpleResolver = new SimpleResolver(server);
					extendedResolver.addResolver(simpleResolver);
				} 
				catch (UnknownHostException e) 
				{
					continue;
				}
			}
			extendedResolver.setRetries(retries);
			extendedResolver.setTimeout(Duration.ofSeconds(timeout));
			extendedResolver.setTCP(useTCP);
		}

		return extendedResolver;
	}
    

	protected class DNSLookupJob extends StopabbleLoopedJob
	{
		protected Lookup lu;
		
		protected AtomicLong numQueries;
		
		protected AtomicLong failedQuries;
		
		public DNSLookupJob(int lookupType, boolean useTCP)
		{
			super();
			
			numQueries = new AtomicLong();
			failedQuries = new AtomicLong();
			
			try 
			{
	        	lu = new Lookup(new Name(targetDomain), lookupType);
	        	
	        	String[] server = {dnsServerTarget};
				lu.setResolver(createExResolver(server, 3, 2, useTCP)); // default retries is 3, limite to 2
				lu.setSearchPath((String[])null);
			}
			catch (Exception e)
			{
				this.setRunning(false);
			}

		}
		
		@Override
		public void executeJob()
		{
			numQueries.getAndIncrement();
			
			lu.setCache(new Cache(DClass.IN));
			
			Record[] retRecords = lu.run();
			
			if (retRecords != null && retRecords.length > 0)
			{
				// success, no-op
			}
			else
			{
				failedQuries.getAndIncrement();
			}
		}
		
		public long getNumQueries()
		{
			return numQueries.get();
		}
		
		public long getNumFailedQueries()
		{
			return failedQuries.get();
		}
	}

	protected void computeRunningAverage()
	{
		long totalCERTQueries = 0;
		long totalMXQueries = 0;
		long totalFailedMXQueires = 0;
		long totalFailedCERTQueires = 0;
		
        for(DNSLookupJob job : certLookupThreads)
        {
        	totalCERTQueries += job.getNumQueries();
        	totalFailedCERTQueires += job.getNumFailedQueries();
        }
        
        for(DNSLookupJob job : mxLookupThreads)
        {
        	totalMXQueries += job.getNumQueries();
        	totalFailedMXQueires += job.getNumFailedQueries();
        }
        
        long totalQueries = totalCERTQueries + totalMXQueries;
        long elapsedTimeInSeconds = (System.currentTimeMillis() - startTime) / 1000;
        
        long avg = totalQueries / elapsedTimeInSeconds;
        
        System.out.println("Average queires per second: " + avg);
        System.out.println("\tTotal queires: " + totalQueries);
        System.out.println("\t\tMX queires: " + totalMXQueries);
        System.out.println("\t\tCERT queires: " + totalCERTQueries);
        

        System.out.println("\r\n\tTotal MX failed queires: " + totalFailedMXQueires);        
        System.out.println("\tTotal CERT failed queires: " + totalFailedCERTQueires);
	}
	
    private static void printUsage()
    {
        StringBuffer use = new StringBuffer();
        use.append("Usage:\n");
        use.append("DNSLoadTester (options)...\n\n");
        use.append("options:\n");
        use.append("-server server          The target DNS server to load test.\n");
        use.append("\n");
        use.append("-domain domain          A valid domain that should create successful DNS lookups\n"); 
        use.append("\n");
        use.append("-numCertLookups threads Number of thread to perform certificate lookups.\n");    
        use.append("\n");
        use.append("-numMXLookups threads   Number of thread to perform MX lookups.\n"); 
        
        System.err.println(use);        
    }	
}
