# DNS Service Deployment

The DNS server is deployable on a number of different operating environments and can be launched either interactively (for debugging) or as a background service.

The DNS server are assembled and packaged as a SpringBoot jar application. In the Java RI stock assembly, the services are bundled using in the following directory structure.

```
  +-- DirectDNSServer
      +-- conf
```

The conf directory contains a logback xml file used for configuring logging options.

## Service Installation

To install, first download the Direct Project stock assembly and unpack the contents into the desired location using your archiver of choice (tar, WinZip, WinRar, File Roller, etc).

The DNS server runs as a background process and can be optionally configured to run as a service daemon (NOTE: On Windows systems, the DNS server can currently only be run interactively). To start the service manually:

1. Open a terminal shell and navigate to the DirectDNSServer directory.
2. Run the command *./dnsServer start*

NOTE: If you get an error of "Permissioned denied" you will need to set the executable flag on the script files:

```
chmod +x dnsServer
```

You can also optionally configure the server as a service. There are different ways to do this depending on your linux distribution. On possibility is to create a script file in the /etc/init.d directory.

Assuming you have deployed the server in the /opt directory and you are running Ubuntu, create the file /etc/init.d/DirectDNSServer using the editor of you choice paste the following content:

```
 #  DirectDNSServer auto-start
 #
 # description: Auto-starts the DirectDNSServer

 case $1 in
    start)
            sh /opt/DirectDNSServer/dnsServer start
            ;;
    stop)
            sh /opt/DirectDNSServer/dnsServer stop
            ;;
    restart)
            sh /opt/DirectDNSServer/dnsServer start
            sh /opt/DirectDNSServer/dnsServer stop
            ;;

 esac
 exit 0
```

Make the script executable using the following command:

```
sudo chmod 755 /etc/init.d/DirectDNSServer
```

You can then start the service by running the command:

```
service DirectDNSServer start
```

Conversely you can stop the service by running the command:

```
service DirectDNSServer stop
```

**Running Interactively**

For debugging or troubleshooting purposes, you may need to run the service interactively. Running interactively is the same across all platforms.

1. Open a terminal shell and navigate to the DirectDNSServer/bin directory.
2. Run the command *./dnsServer console*

The service will output all logging to the current console and the log file. To terminate the interactive service, simply press CTRL+C (Control C).

** Service Deployment Configuration

The DNS server uses an internal properties file to bootstrap its default settings.  Settings can be overridden by externalizing the properties using SpringBoot [external configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) options.  The simplest way is to create an file named *application.properties* in the DirectDNSServer directory and set the necessary properties that you wish to override.  The sever also supports connecting to a SpringCloud configuration server which can be configured either using an application.properties file or environment parameters.

The configuration in most cases does not need a lot of modification, however there a few settings that will need adjustment depending on your deployment.  The following table lists configurable properties that can be overridden:

* direct.config.service.url - The URL of the DirectProject configuration server.  Default value is  *http://localhost:8080/config-service*
* direct.webservices.security.basic.user.name: Username to authenticate to the DirectProject configuration server.  Default value is *admin*
* direct.webservices.security.basic.user.password: Password to authenticate to the DirectProject configuration server.  Default value is *d1r3ct*
* direct.dns.binding.port - The port that the DNS server binds to for listening for DNS requests.  Default value is *53*
* direct.dns.binding.address - The local IP address that the DNS server binds to for listening for DNS requests.  Default value is *0.0.0.0*
* direct.dns.certPolicyName - The name of a policy used to filter certificate query responses.  This is generally used for configuring single use certificates.  Default value is empty.

## Service Logging

Logging is configured in the DirectDNSServer/conf/logback.xml file and by default are written to the file DirectDNSServer/log/dns-server.log; by default the file uses a rolling log scheme.  Changes can be made by updating the logback.xml file.