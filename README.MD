Roster League
=============
This example is adapted from the persistence example from https://github.com/eclipse-ee4j/jakartaee-examples

The tutorial documentation is available at https://jakartaee.github.io/jakartaee-documentation/jakartaee-tutorial/current/persist/persistence-basicexamples/persistence-basicexamples.html#_the_roster_application

Roster League is a simple web application to manage a sports league roster.
It allows users to create teams, add players, and view team rosters.

The application is built using Jakarta EE technologies including Servlets, JPA, and EJB.
The purpose of this example is to demonstrate the use of JPA.

I tested the application with GlassFish 7.0.25 and Java 21.

# Running the Application

This application is designed to run on a GlassFish server. It can be downloaded from https://glassfish.org/download.html
To simplify the database setup, it uses the JavaDB (Derby) as a database which comes bundled with GlassFish.

The next steps outline using the IntelliJ IDEA IDE to run the application.

Make sure you have the "GlassFish" plugin installed in IntelliJ IDEA.
To run the application locally, you need to add GlassFish as a run configuration in your IDE.
Click edit configurations on the Run menu, add new configuration - select GlassFish Server Local, select your GlassFish 
installation path and domain from the dropdown (domain1 by default), and then select the deployment artifact as this 
project (RosterLeague-1.0-SNAPSHOT.war)

Before running the application, ensure that the database is started. To start it, open a terminal and navigate to 
the `bin` directory of your GlassFish installation. Example path: `C:\Users\Alex\glassfish7\bin`

Depending on your operating system or terminal choice, run the appropriate command to start the database:

In Windows Command Prompt:
```cmd
asadmin.bat start-database
```

In Windows PowerShell:
```powershell
.\asadmin start-database
```

In Git Bash, Unix/Linux or macOS terminal:
```sh
./asadmin start-database
```

If the command is successful, you should see a message indicating that the command completed successfully.
Stopping the database can be done similarly using the `stop-database` command.

Once the configuration is set up, you can run the application directly from your IDE. 
The application will be accessible at http://localhost:8080/RosterLeague-1.0-SNAPSHOT/ 
(the default IDE configuration will open the browser automatically after the server started and the application is deployed).

Troubleshooting
===============

Error: IntelliJ - Glassfish server not available as run configuration

Solution: Settings>Plugins>Market Place: search and install the 'Glassfish' plugin


`asadmin start-database`

Error:
```
The java command "%~$PATH;i" is not executable.
It was configured as PATH="C:\Program Files\Common.... a lot of paths here.."
'"%~$PATH;i"' is not recognized as an internal or external command,
operable program or batch file.
```
Solution: add the JAVA_HOME environment variable to your system environment variables with the path to the JDK, 
OR edit <glassfish>/glassfish/config/config.bat (or config.sh) and set the java home path, for example:
`set JAVA_HOME=C:\Program Files\Java\jdk-21\`


Error: When running the application, if you encounter an error related to the database connection.
An example error message might look like this:
```
Internal Exception: 
 java.sql.SQLException: Failed to obtain/create connection
```
Solution: ensure that the JavaDB (Derby) is started as described in "Running the Application".
After starting the database, redeploy the application - there is no need to restart the server.

Notes
=====
The criteria query builder API uses metamodel classes to provide type-safe queries. 
These classes are generated at compile time by an annotation processor.

The changes to the build process to generate the metamodel classes from the database schema:
- I added
`annotationProcessor('org.eclipse.persistence:org.eclipse.persistence.jpa.modelgen.processor:4.0.7')`
in the dependencies section of the build.gradle file.
- to call the preprocessor I added the `-processor` compile argument to call the CanonicalModelProcessor.

Differences from the original example:
- The project uses Gradle as the build tool instead of Maven.
- The project structure is adapted to be a single module project - I removed the `@Remote` annotation from the Request bean.
- The client is a simple text returning servlet mapped to the root path instead of a console application
- The "Details" classes now have the fields marked as `final` (could be records in Java 21)
- I made the RequestBean `@Stateless` instead of `@Stateful` as there is no state to maintain
- I replaced the iterator loops with enhanced for loops
- Each time the endpoint is accessed, the database is cleared and repopulated (for simplicity). 
Redeploying the application continues to drop and re-create the tables.
- Code formatting and fixes for warnings in the code
- Exception reporting is simplified - exceptions are printed in the servlet output
