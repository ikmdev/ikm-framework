Demonstration plugin framework for orchestrating the Komet application. 

### Team Ownership - Product Owner
FDA Shield - Client

## Getting Started

Follow the instructions below to set up the local environment for `ikm-framework`:

1. Download and install Open JDK Java 19

2. Download and install Apache Maven 3.9 or greater

3. There are dependencies to building `ikm-framework`. Please ensure you have a reliable internet connection when cloning and building to get all dependencies from Maven Central.

4. [Komet](https://github.com/ikmdev/komet) is a dependency for `ikm-framework` which is not currently provided on Maven Central. Please refer to the [Komet README](https://github.com/ikmdev/komet/blob/main/README.md) for building instructions.
</br>NOTE: The `komet.version` property in the [ikm-framework/pom.xml](https://github.com/ikmdev/ikm-framework/blob/main/pom.xml) file must align with the Komet version built in this step.

## Building and Running IKM-Framework

Follow the steps below to build and run `ikm-framework` on your local machine:

1. Clone the [ikm-framework](https://github.com/ikmdev/ikm-framework) project from GitHub to your local machine

```bash
git clone [Repo URL]
```

2. Navigate to the `ikm-framework` directory

3. Enter the following command to build the application:

```bash
mvn clean install
```

## Issues and Contributions
Technical and non-technical issues can be reported to the [Issue Tracker](https://github.com/ikmdev/ikm-framework/issues).

Contributions can be submitted via pull requests. Please check the [contribution guide](doc/how-to-contribute.md) for more details.
