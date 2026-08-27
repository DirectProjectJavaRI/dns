---
title: DNS Service Deployment
---

# DNS Service Deployment

The DNS server is a standalone, authoritative-only DNS service. It answers DNS queries — most importantly the CERT record queries used for Direct certificate discovery — from the records managed in the Direct Project configuration service. It is assembled and packaged as a Spring Boot jar application and can be launched either interactively (for debugging) or as a background service.

Two deployment models exist:

* **Cloud Native deployment** — the only supported model starting with version 9.0.0. The DNS server is deployed as an individual Spring Boot micro-service alongside the other Direct micro-services.
* **Legacy deployment** — supported only for version 8.1.x and earlier. The DNS server is deployed from the Direct Project stock assembly as a self-contained `DirectDNSServer` directory with its own `dnsServer` control script.

## Cloud Native Deployment

Starting with version 9.0.0, the DNS server is deployed the same way as every other Direct micro-service, as described in the [Cloud Native HISP Deployment Model](https://directprojectjavari.github.io/docs/direct-project-stock/cloud-native-deployment) guide. Refer to that guide for the full walkthrough — deploying binaries, the `service.sh` / `service.ps1` control scripts, the `conf/logback.xml` logging file, and overriding configuration with an `application.yml`. This section covers only what is specific to the DNS service.

1. Create a directory named `dns` for the service, following the [Download Micro-service Binaries](https://directprojectjavari.github.io/docs/direct-project-stock/cloud-native-deployment#download-micro-service-binaries) step of the Cloud Native guide.
2. Download [dns-sboot-9.0.0.jar](https://repo.maven.apache.org/maven2/org/nhind/dns-sboot/9.0.0/dns-sboot-9.0.0.jar) from Maven Central into that directory.
3. Copy the `service.sh` (Unix/Linux/macOS) or `service.ps1` (Windows) template from the Cloud Native guide into the `dns` directory and replace the `<binary>` placeholder with `dns-sboot-9.0.0.jar`. Add a `conf/logback.xml` file as described in that guide.
4. Start the service with `./service.sh start` (or `.\service.ps1 start`). Use `./service.sh console` to run it interactively for troubleshooting, and `./service.sh stop` to stop it.

**Binding to port 53:** the DNS server listens on UDP/TCP port 53 by default, which is a privileged port on most operating systems. Either start the service with sufficient privileges to bind low ports (run as `root`, or grant the Java executable the `CAP_NET_BIND_SERVICE` capability on Linux), or set `direct.dns.binding.port` to a non-privileged port and forward port 53 to it.

### Configuration

The DNS service is configured with an `application.yml` file placed in the `dns` directory, the same as the other micro-services. The full set of configurable properties is documented in the [DNS Service configuration table](https://directprojectjavari.github.io/docs/direct-project-stock/cloud-native-deployment#dns-service) of the Cloud Native guide. The settings you are most likely to change are:

* `direct.config.service.url` — URL of the configuration service API. Default `http://localhost:8082/`.
* `direct.webservices.security.basic.user.name` / `direct.webservices.security.basic.user.password` — credentials used to authenticate to the configuration service. Defaults `admin` / `d1r3ct;`.
* `direct.dns.binding.port` — the port the DNS server binds to for listening for DNS requests. Default `53`.
* `direct.dns.binding.address` — the local IP address the DNS server binds to. Default `0.0.0.0` (all interfaces).
* `direct.dns.binding.maxReconnectAttempts` — number of times the server attempts to re-bind its listener socket after an I/O failure before giving up. Default `10`.
* `direct.dns.certPolicyName` — the name of a policy (defined in the configuration service via the [Policy Enablement module](https://directprojectjavari.github.io/direct-policy/)) used to filter the certificates the server returns for CERT record queries. This is generally used for single-use certificate deployments, where you only want to publish the key-encipherment certificate over DNS. See [Single-Use Certificates](https://directprojectjavari.github.io/docs/direct-project-stock/single-use-certs#dns-certificate-distribution) in the Bare Metal deployment guide for the background on why this is needed, and [Policy Enforcement](dns-rec-config#policy-enforcement) for the DNS-specific configuration steps. Default is empty (no filtering).

## Legacy Deployment (version 8.1.x and earlier)

> **Note:** This deployment model applies only to version 8.1.x and earlier. For version 9.0.0 and later, use the Cloud Native deployment described above.

The DNS server is deployable on a number of different operating environments and can be launched either interactively (for debugging) or as a background service. In the Java RI stock assembly, the service is bundled using the following directory structure:

```
  +-- DirectDNSServer
      +-- conf
```

The conf directory contains a logback xml file used for configuring logging options.

### Service Installation

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

### Running Interactively

For debugging or troubleshooting purposes, you may need to run the service interactively. Running interactively is the same across all platforms.

1. Open a terminal shell and navigate to the DirectDNSServer/bin directory.
2. Run the command *./dnsServer console*

The service will output all logging to the current console and the log file. To terminate the interactive service, simply press CTRL+C (Control C).

### Service Deployment Configuration

The DNS server uses an internal properties file to bootstrap its default settings.  Settings can be overridden by externalizing the properties using SpringBoot [external configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) options.  The simplest way is to create an file named *application.properties* in the DirectDNSServer directory and set the necessary properties that you wish to override.  The sever also supports connecting to a SpringCloud configuration server which can be configured either using an application.properties file or environment parameters.

The configuration in most cases does not need a lot of modification, however there a few settings that will need adjustment depending on your deployment.  The following table lists configurable properties that can be overridden:

* direct.config.service.url - The URL of the DirectProject configuration server.  Default value is  `http://localhost:8080/config-service`
* direct.webservices.security.basic.user.name: Username to authenticate to the DirectProject configuration server.  Default value is *admin*
* direct.webservices.security.basic.user.password: Password to authenticate to the DirectProject configuration server.  Default value is *d1r3ct*
* direct.dns.binding.port - The port that the DNS server binds to for listening for DNS requests.  Default value is *53*
* direct.dns.binding.address - The local IP address that the DNS server binds to for listening for DNS requests.  Default value is *0.0.0.0*
* direct.dns.certPolicyName - The name of a policy used to filter certificate query responses.  This is generally used for configuring single use certificates; see [Single-Use Certificates](https://directprojectjavari.github.io/docs/direct-project-stock/single-use-certs#dns-certificate-distribution) for the background and [Policy Enforcement](dns-rec-config#policy-enforcement) for the configuration steps.  Default value is empty.

### Service Logging

Logging is configured in the DirectDNSServer/conf/logback.xml file and by default are written to the file DirectDNSServer/log/dns-server.log; by default the file uses a rolling log scheme.  Changes can be made by updating the logback.xml file.
