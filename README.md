# Overview

The DNS services implement a simple authoritative only DNS server for hosting DNS records for a Direct messaging domain. It is intentionally tuned to handle only certain type of DNS records including CERT type records as defined by the DirectProfject [specification](http://wiki.directproject.org/w/images/e/e6/Applicability_Statement_for_Secure_Health_Transport_v1.2.pdf).

The Direct Project Simple Health Transport specification outlines requirements for using DNS to resolve public certificates for the purpose of message encryption.  Several open source and commercial DNS services are already widely available, however they differ in both their support of the full DNS specification and the tooling available to configure and manage the DNS solution. The challenge is finding a solution the meets the needs of the domain both in terms of functional support and ease of use (good tooling). So why does the Direct Project provides its own DNS solution when are there already so many viable options. The simple answer is the use of the CERT record type. Investigation has shown that many popular, commercially available DNS services (both organizational and third party hosting, ex: GoDaddy) do not support CERT records. Conversely many of the open source services support a broader range of the DNS spec, however tooling and configuration support is limited. In some solutions, tooling is limited to editing a raw configuration file with a plain text editor. Text editor support is not a viable solution for large deployments where thousands of entries may exist.

The Direct Project DNS services are not intended to be a one stop shop for all DNS needs, but to compliment existing DNS service and fill the functional gaps not provided by existing solutions. In practice, the functional abilities of the Direct DNS services are limited to meet a small number of use cases. Specifically they provide a simple solution to respond to public cert requests (and a few other request types) and tooling to manage certificate storage and DNS record entries. In addition the services are not intended to host primary DNS zones; instead they are deployed in one or more sub zones that are generally intended only for Direct Project implementations.


## Guides

This document describes how to implement and configure the Direct Project Java DNS server for certificate distribution.

* [Deployment Guide](DepGuide) - This section describes how deploy and configure the DNS services.


Configuration is broken into two logical part: configuring the DNS specific protocol parameters and configuring/managing DNS records. The latter configuration may be dependent on the DNS hosting solution of the primary domain name.
* [DNS Protocol Configuration](DNSProtConfig)
* [DNS Record Configuration](DNSRecConfig)
* [GoDaddy Domain Hosting](GoDaddy)