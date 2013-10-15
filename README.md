JHades - Your way out of Jar Hell!
======

A java classpath troubleshooting utility that allows to query the classpath for class duplicates, find specific resource locations, etc.

Documentation available at http://jhades.org


#### Example of a classpath problem

Let's say a java web application works fine in development, but when deploying it to a server the following problem occurs:

```

java.lang.ClassCastException: org.apache.bval.jsr303.ApacheValidationProvider cannot be cast to javax.validation.spi.ValidationProvider
	javax.validation.Validation$DefaultValidationProviderResolver.getValidationProviders(Validation.java:332)
	javax.validation.Validation$GenericBootstrapImpl.configure(Validation.java:256)
	javax.validation.Validation.buildDefaultValidatorFactory(Validation.java:111)
	classloaders.test.TestServlet.doGet(TestServlet.java:33)
	javax.servlet.http.HttpServlet.service(HttpServlet.java:621)
	javax.servlet.http.HttpServlet.service(HttpServlet.java:728)
	org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:91)

```

#### How to troubleshoot

JHades provides several commands that allow to extract from the runtime environment a lot of information 
which is important for understanding what is going on. Namelly these types of questions can be answered:

- are there any overlapping jars, with different versions of the same class?
- Are those multiple versions of the same class all identical or not ?
- where are the different versions of a given class located ?
- which class loader is loading which version of a given class ?
- What does the chain of classloaders look like, how are the classloaders configured ?

This is an example of how to answer several of these questions for the sample problem above:

```java
 
 new JHades()
	.printClassLoaders()
	.printClasspath()
	.overlappingJarsReport()
	.multipleClassVersionsReport()
	.findClassByName("org.apache.bval.jsr303.ApacheValidationProvider")
	.findClassByName("javax.validation.spi.ValidationProvider"); 
	
```

#### Jar overlap report 

One of the most common sources of classpath problems are overlapping jar files, that contain multiple versions of the same class:

```

>>>> Jar overlap report: 
 
aspectjrt-1.7.2.jar overlaps with aspectjweaver-1.7.2.jar - total overlapping classes: 129
ejb3-persistence-1.0.2.GA.jar overlaps with hibernate-jpa-2.0-api-1.0.1.Final.jar - total overlapping classes: 91
stax-api-1.0-2.jar overlaps with xml-apis-1.4.01.jar - total overlapping classes: 34
javax.xml.soap-api-1.3.5.jar overlaps with saaj-api-1.3.jar - total overlapping classes: 29
commons-beanutils-1.8.3.jar overlaps with commons-collections-3.2.1.jar - total overlapping classes: 10
xbean-2.2.0.jar overlaps with xml-apis-1.4.01.jar - total overlapping classes: 6
commons-logging-1.1.1.jar overlaps with jcl-over-slf4j-1.6.6.jar - total overlapping classes: 6
 
Total number of classes with more than one version: 305

```

Check http://jhades.org for more details!
