# Break Time Buddy — Installation Guide

This guide explains how to install, configure, run, test, and package Break Time Buddy, a Java 17 and JavaFX desktop application for tracking work and break sessions and providing break recommendations.

AI-powered recommendations through Groq are optional. Break Time Buddy can run without a Groq API key by using its rule-based fallback recommendation service.

## Prerequisites

Install the following before running the project:

- Java 17
- Apache Maven
- Git if you plan to clone the repository using Git

Groq configuration is not required to run the application.

## Obtain the Project

Clone the team repository:

```bash
git clone https://github.com/fordlarry13-stack/BreakTimeBuddy.git
```

Enter the project directory:

```bash
cd BreakTimeBuddy
```

The primary development branch is `develop`. To use it:

```bash
git switch develop
```

To retrieve the latest changes:

```bash
git pull
```

## Verify Java and Maven

Check the installed Java version:

```bash
java -version
```

Break Time Buddy targets Java 17.

Check that Maven is available:

```bash
mvn -version
```

The Maven output should also indicate that the project is running with a Java installation compatible with the Java 17 target.

## Run the Application

From the project root directory, run:

```bash
mvn clean javafx:run
```

Maven will resolve the required project dependencies and start the JavaFX desktop application.

## Optional Groq AI Configuration

Break Time Buddy supports AI-powered break recommendations through Groq.

This configuration is optional. The application does not require a Groq API key to start or remain functional.

The application reads the Groq credential from the `GROQ_API_KEY` environment variable.

### macOS or Linux

Set the environment variable in the terminal session that will run the application:

```bash
export GROQ_API_KEY="your_groq_api_key_here"
```

Then start Break Time Buddy from the same environment:

```bash
mvn clean javafx:run
```

### Windows PowerShell

Set the environment variable for the current PowerShell session:

```powershell
$env:GROQ_API_KEY="your_groq_api_key_here"
```

Then run:

```powershell
mvn clean javafx:run
```

`your_groq_api_key_here` is only a placeholder. Replace it with your own Groq API key.

## Run Without a Groq API Key

A Groq API key is not required.

If `GROQ_API_KEY` is not available to the application, Break Time Buddy remains functional and uses its rule-based fallback recommendation service instead of the Groq-powered recommendation service.

No API key should be added to the source code or `config.json`.

## Run Automated Tests

Run the automated test suite from the project root:

```bash
mvn test
```

Maven will compile the required project and test sources and execute the configured test suite.

## Build and Package the Application

To perform a clean Maven build and package the project, run:

```bash
mvn clean package
```

Maven build output is produced under the project's `target/` directory.

This command should not be interpreted as creating a platform-specific native installer unless such packaging is separately configured and verified.

## Local Configuration

Break Time Buddy uses a local file named `config.json`.

The application uses this file for locally saved application configuration, including supported session-related preferences and data.

The Groq API key does not belong in `config.json`. Configure it only through the `GROQ_API_KEY` environment variable.

## Troubleshooting

### Java version is incorrect

Check the active Java installation:

```bash
java -version
```

Also check which Java installation Maven is using:

```bash
mvn -version
```

The project targets Java 17. If another Java version is active and causes compatibility problems, configure the environment to use an appropriate Java 17 installation.

### Maven is not recognized

Verify Maven:

```bash
mvn -version
```

If the command is unavailable, install Maven and ensure that the Maven executable is available through the system's command path.

### Maven cannot resolve dependencies or complete the build

Confirm that Maven is installed correctly and that the computer has network access for dependency resolution. Then retry the appropriate Maven command.

For a clean build:

```bash
mvn clean package
```

### The application does not start

First verify Java and Maven:

```bash
java -version
mvn -version
```

Then run the application from the repository's root directory:

```bash
mvn clean javafx:run
```

Review any Maven or Java error output in the terminal before making configuration changes.

### Groq AI recommendations are unavailable

Check whether `GROQ_API_KEY` is available in the same environment used to launch the application.

On macOS or Linux:

```bash
echo "$GROQ_API_KEY"
```

On Windows PowerShell:

```powershell
$env:GROQ_API_KEY
```

Do not share the resulting value.

If the key is absent, Break Time Buddy can continue operating through its rule-based fallback recommendation service.

## Security Notes

Treat the Groq API key as a secret.

- Never commit `GROQ_API_KEY` to Git or GitHub.
- Never hardcode the API key in Java source code.
- Never store the API key in `config.json`.
- Do not expose the key in screenshots, logs, documentation, presentations, or other shared materials.
- Configure the credential through the `GROQ_API_KEY` environment variable.

Using an environment variable reduces the risk of accidentally committing a credential to source control. However, Break Time Buddy is currently a desktop client that communicates with the AI provider, so environment-variable configuration should not be considered production-grade secret protection for a distributed desktop application.

A server-side/backend proxy architecture may be considered as a future security improvement, but it is not part of the current installation or application architecture.
