# Break Time Buddy — User manual

## Overview

Break Time Buddy is a JavaFX desktop application that helps you track work and break sessions and obtain break recommendations.

AI-powered recommendations through Groq are optional. If Groq is not configured, is unavailable, or does not return a valid response, the application can use its rule-based fallback recommendation service.

## Starting the application

Follow [INSTALLATION.md](INSTALLATION.md) for complete installation and configuration instructions.

From the project root directory, the normal Maven launch command is:

```bash
mvn clean javafx:run
```

## Main interface

The main window contains the following controls and status information:

- **In session** or **Not in session**: A session toggle whose text shows whether a work session is active.
- **Sessions: [count]**: The number of completed work sessions.
- **Preferred work length (1-300)**: An editable control for the preferred work-session length in minutes.
- **History ([count])**: Completed work and break entries, including their start and end timestamps.
- **Pending break recommendation: true/false**: Whether a recommendation request is currently pending.
- **Request break recommendation now**: Requests a recommendation while a work session is active.
- **Recommendation: [message]**: The recommendation display, shown when a recommendation is available.
- **Start Break**: Accepts the displayed recommendation and starts a break.
- **Not Now**: Dismisses the displayed recommendation without ending the active work session.
- **Save config**: Saves supported local settings and completed history to `config.json`.
- **Load config**: Loads supported local settings and completed history from `config.json`.
- Configuration feedback: A timestamped **Save success**, **Load success**, **Save error: ...**, or **Load error: ...** message after a save or load attempt.

## Setting the preferred work length

Use the **Preferred work length (1-300)** control to set the desired work-session length in minutes. You may use the spinner arrows or edit the number directly.

The supported range is 1 through 300 minutes. The default is 50 minutes. Values are stored as whole minutes, and an entered value is constrained to the supported range.

The preferred length is included in recommendation requests. Changing it does not automatically start, stop, or time a session.

## Tracking a work or break session

The session toggle initially reads **Not in session**. Select it to begin a work session; it then reads **In session**.

Select **In session** to finish the active work session and begin a break. At that point:

- The completed work period is added to **History**.
- The **Sessions** count increases by one.
- Any pending or displayed break recommendation is cleared.
- A break period begins.

Select **Not in session** again to finish the break and begin the next work session. The completed break is then added to **History**. The **Sessions** count represents completed work sessions, not the combined number of work and break entries.

Only completed periods appear in **History**. Each entry shows `WORK` or `BREAK` followed by its start and end timestamps. Break Time Buddy does not perform automatic idle detection or activity monitoring; you control session changes with the toggle.

## Requesting a break recommendation

Recommendation requests are associated with an active work session. While the session toggle reads **In session**, select **Request break recommendation now**.

**Pending break recommendation: true** indicates that a request is in progress. The status returns to `false` when processing finishes or the request is canceled. Selecting the request button outside an active work session does not start a request. Repeated selections while a request is already pending do not create additional simultaneous requests.

When `GROQ_API_KEY` is configured, the application first attempts to obtain a recommendation from Groq. Without a configured key—or if the Groq request fails, the service is unavailable, or the response is invalid—the application uses its rule-based fallback. The fallback selects a short break suggestion based on the completed-work-session count.

A recommendation is displayed only when a response is successfully associated with the still-active work session. Ending the work session while a request is pending cancels the pending request, so no recommendation is then displayed.

## Responding to a recommendation

When a recommendation is available, the application displays **Recommendation: [message]** with two choices:

- **Start Break**: Accepts the recommendation. If the work session is still active, it completes that work session, increases the **Sessions** count, records the completed work period in **History**, and begins a break.
- **Not Now**: Dismisses the recommendation and leaves the active work session unchanged.

## Saving and loading configuration

Select **Save config** to write the following data to the local `config.json` file:

- The completed-work-session count.
- The preferred work length.
- Up to 20 completed work and break history entries.

The current active work or break period is not part of the saved history until that period is completed by toggling sessions.

Select **Load config** to restore the saved session count, preferred work length, and completed history. Loaded values are validated: the session count cannot be negative, the preferred work length is constrained to 1–300 whole minutes, and invalid history records are omitted.

After an operation, the interface shows a timestamped **Save success** or **Load success** message. A failed operation shows **Save error: ...** or **Load error: ...** with error details.

The application reads and writes `config.json` in its current working directory. The Groq API key is not stored in this file; `GROQ_API_KEY` is read from the environment when the application starts.

## Understanding history

The **History ([count])** section displays completed session entries from newest to oldest. Each line contains:

- The phase: `WORK` or `BREAK`.
- The start timestamp.
- The end timestamp.

The application retains and displays at most 20 completed entries. When another entry is completed after the limit is reached, the oldest entry is removed. An active work or break period is not displayed until it ends.

## AI and recommendation disclosure

When `GROQ_API_KEY` is configured, Break Time Buddy sends recommendation requests to Groq. The request prompt includes the following:

- The number of completed work sessions.
- The preferred work-session length.
- How long the current work session has been active.
- The phase and duration of recent completed work and break sessions.

AI-generated output can be inaccurate or unsuitable. Recommendations are general break and wellness suggestions, not medical advice.

If Groq is not configured or available, or if a valid AI response cannot be obtained, the application can use its rule-based fallback. Using an environment variable reduces the risk of accidentally committing the Groq credential to source control, but it should not be considered production-grade secret protection for a distributed desktop application.

## Troubleshooting

### The application does not start

Run the application from the project root with `mvn clean javafx:run`. Review any Maven or Java errors in the terminal and consult [INSTALLATION.md](INSTALLATION.md).

### A recommendation does not appear

Confirm that a work session is active and the toggle reads **In session** before selecting **Request break recommendation now**. Check **Pending break recommendation** for the request state. If you end the work session while the request is pending, the request is canceled.

### Groq is unavailable or not configured

Groq is optional. If `GROQ_API_KEY` is absent, blank, or the Groq service cannot provide a valid response, Break Time Buddy can use its rule-based fallback. See [INSTALLATION.md](INSTALLATION.md) for optional Groq configuration.

### Configuration cannot be saved or loaded

Check the timestamped configuration feedback message for error details. Run the application from a directory where it can create and write `config.json`. To load configuration, confirm that `config.json` exists, is readable, and contains valid configuration data.

### The Java or Maven environment is incorrect

Break Time Buddy targets Java 17. Verify Java with `java -version` and Maven with `mvn -version`. Follow the environment guidance in [INSTALLATION.md](INSTALLATION.md).

## Current limitations

- Break Time Buddy is a local desktop application with a basic interface.
- Session changes require manual use of the session toggle; there is no automatic idle detection or activity tracking.
- Recommendation behavior depends on the available and configured recommendation service.
- Configuration is stored locally in `config.json`; there are no user accounts or cloud storage.
- The Groq credential is configured through an environment variable. This reduces accidental source-control exposure but is not production-grade secret protection for a distributed desktop application.

## Quick usage example

1. Start the application.
2. Set the preferred work length.
3. Select **Not in session** to start a work session.
4. Work for a period of time.
5. Select **Request break recommendation now**.
6. If a recommendation is displayed, select **Start Break** or **Not Now**.
7. Review **History** after completing work or break periods.
8. Select **Save config** if you want to save the supported local configuration and completed history.
