package com.breaktimebuddy;

import java.time.LocalTime;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Presentation model for the UI.
 *
 * Exposes observable properties bound to the view. Updated by the {@link Controller} in response to
 * {@link Interactor} state changes.
 */
public class ViewModel {
  private final BooleanProperty inSession = new SimpleBooleanProperty();
  private final ReadOnlyStringWrapper sessionStatusText = new ReadOnlyStringWrapper();
  private final IntegerProperty sessions = new SimpleIntegerProperty();
  private final BooleanProperty breakRecommendationRequested = new SimpleBooleanProperty();
  private final ObjectProperty<DialogState> dialogState = new SimpleObjectProperty<>();
  private final ObjectProperty<LocalTime> configFeedbackTimestamp = new SimpleObjectProperty<>();
  private final StringProperty configFeedbackMessage = new SimpleStringProperty();

  /**
   * Creates a view model with default values and binds the derived properties.
   */
  public ViewModel() {
    sessionStatusText.bind(Bindings.when(inSession).then("In session").otherwise("Not in session"));
  }

  /**
   * Returns whether a work session is currently active.
   *
   * @return true if a session is in progress, false otherwise
   */
  public boolean getInSession() {
    return inSession.get();
  }

  /**
   * Returns the property for whether a work session is currently active.
   *
   * @return the in-session property
   */
  public BooleanProperty isSessionProperty() {
    return inSession;
  }

  /**
   * Sets whether a work session is currently active.
   *
   * @param inSession true if a session is in progress, false otherwise
   */
  public void setInSession(boolean inSession) {
    this.inSession.set(inSession);
  }

  /**
   * Returns the session status text.
   *
   * @return the session status text
   */
  public String getSessionStatusText() {
    return sessionStatusText.get();
  }

  /**
   * Returns the read-only property for the session status text.
   *
   * @return the session status text property
   */
  public ReadOnlyStringProperty sessionStatusTextProperty() {
    return sessionStatusText.getReadOnlyProperty();
  }

  /**
   * Returns the number of completed work sessions.
   *
   * @return the session count
   */
  public int getSessions() {
    return sessions.get();
  }

  /**
   * Returns the property for the number of completed work sessions.
   *
   * @return the sessions property
   */
  public IntegerProperty sessionsProperty() {
    return sessions;
  }

  /**
   * Sets the number of completed work sessions.
   *
   * @param sessions the session count
   */
  public void setSessions(int sessions) {
    this.sessions.set(sessions);
  }

  /**
   * Returns whether a break recommendation has been requested.
   *
   * @return true if a recommendation was requested, false otherwise
   */
  public boolean getBreakRecommendationRequested() {
    return breakRecommendationRequested.get();
  }

  /**
   * Returns the property for whether a break recommendation has been requested.
   *
   * @return the break recommendation requested property
   */
  public BooleanProperty breakRecommendationRequestedProperty() {
    return breakRecommendationRequested;
  }

  /**
   * Sets whether a break recommendation has been requested.
   *
   * @param breakRecommendationRequested true if a recommendation was requested, false otherwise
   */
  public void setBreakRecommendationRequested(boolean breakRecommendationRequested) {
    this.breakRecommendationRequested.set(breakRecommendationRequested);
  }

  /**
   * Returns the current dialog state.
   *
   * @return the dialog state, or null if no dialog is shown
   */
  public DialogState getDialogState() {
    return dialogState.get();
  }

  /**
   * Returns the property for the current dialog state.
   *
   * @return the dialog state property
   */
  public ObjectProperty<DialogState> dialogStateProperty() {
    return dialogState;
  }

  /**
   * Sets the current dialog state.
   *
   * @param dialogState the dialog state to display, or null to hide the dialog
   */
  public void setDialogState(DialogState dialogState) {
    this.dialogState.set(dialogState);
  }

  /**
   * Returns the timestamp of the last configuration feedback.
   *
   * @return the feedback timestamp, or null if no feedback has been set
   */
  public LocalTime getConfigFeedbackTimestamp() {
    return configFeedbackTimestamp.get();
  }

  /**
   * Returns the property for the configuration feedback timestamp.
   *
   * @return the config feedback timestamp property
   */
  public ObjectProperty<LocalTime> configFeedbackTimestampProperty() {
    return configFeedbackTimestamp;
  }

  /**
   * Sets the timestamp of the last configuration feedback.
   *
   * @param configFeedbackTimestamp the timestamp to set
   */
  public void setConfigFeedbackTimestamp(LocalTime configFeedbackTimestamp) {
    this.configFeedbackTimestamp.set(configFeedbackTimestamp);
  }

  /**
   * Returns the last configuration feedback message.
   *
   * @return the feedback message, or null if no feedback has been set
   */
  public String getConfigFeedbackMessage() {
    return configFeedbackMessage.get();
  }

  /**
   * Returns the property for the configuration feedback message.
   *
   * @return the config feedback message property
   */
  public StringProperty configFeedbackMessageProperty() {
    return configFeedbackMessage;
  }

  /**
   * Sets the last configuration feedback message.
   *
   * @param configFeedbackMessage the message to set
   */
  public void setConfigFeedbackMessage(String configFeedbackMessage) {
    this.configFeedbackMessage.set(configFeedbackMessage);
  }
}
