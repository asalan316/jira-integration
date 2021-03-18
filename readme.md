# JIRA Integration using JIRA REST Client Library

This repository contains a sample implementation in order
to modify custom fields within JIRA Server using JIRA REST Client library (version 5.2.2)

Currently, modifications of some fields are allowed for a particular issue


### Usage:

- In the TestJiraCRUD.java, provide the project key and issue key in the onHandle method as follows

````
   /* Define your JIRA configutation */
    private static final String JIRA_URL = "http://localhost::9082";
    private static final String USERNAME = "arsalan.khan";
    private static final String PASSWORD = "<password>";

    // define project key
    private static final String PROJECT_KEY = "ATP";
   
````
- run the app from the app directory `mvnw spring-boot:run`

### Dependencies

````
<dependency>
<groupId>com.atlassian.jira</groupId>
<artifactId>jira-rest-java-client-app</artifactId>
<version>5.2.2</version>
</dependency>
<!-- https://mvnrepository.com/artifact/com.atlassian.fugue/fugue -->
<dependency>
<groupId>com.atlassian.fugue</groupId>
<artifactId>fugue</artifactId>
<version>2.6.1</version>
<scope>provided</scope>
</dependency>

````



