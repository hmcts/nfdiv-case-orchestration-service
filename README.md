# Divorce Case Orchestration Service [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

This application orchestrates a workflow based on the requested business requirement.

## Setup


**Prerequisites**

- [JDK 11](https://openjdk.java.net/)
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli)

**Building**

The project uses [Gradle](https://gradle.org) as a build tool but you don't have to install it locally since there is a
`./gradlew` wrapper script.

To build project please execute the following command:

```bash
    ./gradlew build
```

To get the project to build in IntelliJ IDEA, you have to:

 - Install the Lombok plugin: Preferences -> Plugins
 - Enable Annotation Processing: Preferences -> Build, Execution, Deployment -> Compiler -> Annotation Processors

**Running**

Running the service locally requires several APIs in the AAT environment. You will need an active VPN, to have permission to read the nfdiv-aat vault and to be logged into azure CLI.

```
./gradlew bootRun
```
The application will start locally on `http://localhost:4012`

**API documentation**

API documentation is provided with Swagger:
 - `http://localhost:4012/swagger-ui.html` - UI to interact with the API resources

## Testing

**Unit tests**

To run all unit tests and local functional tests respectively please execute following command:

```bash
    ./gradlew test
```

**Coding style tests**

To run all checks (including unit tests) please execute following command:

```bash
    ./gradlew check
```

**Mutation tests**

To run all mutation tests execute the following command:

```
./gradlew pitest

```

**Integration tests**

To run all mocked integration tests locally:

```
./gradlew functional
```

**E2E Integration tests**

To run all E2E integration tests locally:
```
./gradlew bootRun
./gradlew nightlyFunctional
```

**Faster PR builds**

If you're making a very small change which does not require ExtendTests to run, you may put `[FAST]` at the start of the pull request title to only run non-extended testes
If you have already created the PR without `[FAST]` in the title, you can add it but need to make a code change for Jenkins to pick it up
You can also run non-extended test only locally by setting the `CHANGE_TITLE` env variable to `[FAST]` (i.e simulating a PR with a title starting with `[FAST]`)

## Developing

**Enable schedule**

Schedule service is disabled by default in local environment. 
If you want to enable it, set the environment variable `SCHEDULER_ENABLED` to `true` or override application property `scheduler.enabled`

**Handling database**

Database will get initiated when you run ```docker-compose up``` for the first time by execute all scripts from database directory.

You don't need to migrate database manually since migrations are executed every time the application bootstraps.

[scheduler config](./job-scheduler)

**Versioning**

We use [SemVer](http://semver.org/) for versioning.
For the versions available, see the tags on this repository.


**Standard API**

We follow [RESTful API standards](https://hmcts.github.io/restful-api-standards/).

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.