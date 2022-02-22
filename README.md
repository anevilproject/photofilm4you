# PhotoFilm4You

## Summary

This project uses _maven_ to build and package the application.

You can follow the instructions [here](https://maven.apache.org/install.html) to install _maven_.

This project is structured into modules:
 * photofilm4you-integration: Contains entities. Packaged as a jar file.
 * photofilm4you-business: Contains business logic and is declared as an EJB module. Packaged as a jar file.
 * photofilm4you-presentation: Contains web resources and JSF beans. Packaged into a war file.
 * photofilm4you-ear: Declares the configuration to assemble the modules and generate an ear file.
 * photofilm4you-tpv: Contains the payment platform used to complete a booking.
 
## How to build?

In order to compile the project and build the final ear just run the following command:

```cmd
mvn clean install
```

This will clear build directories, compile, copy resources, run the tests and package the application.

In order to skip test execution (to reduce time) add `-DskipTests` as an argument.


## How to deploy?

The ear module configures a deployment plugin for jboss wildfly, and it will do so during the install phase of the build if the `wildfly` profile is specified.

```cmd
mvn clean install -P wildfly
```

This will copy the resulting artifacts to the jboss deployment folder.