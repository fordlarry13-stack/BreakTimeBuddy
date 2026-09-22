package com.breaktimebuddy;

import java.time.Duration;
import java.time.LocalTime;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

// TODO: Rename
public class ViewModel {
  private final BooleanProperty inSession = new SimpleBooleanProperty();
  private final ReadOnlyStringWrapper sessionStatusText = new ReadOnlyStringWrapper();
  private final IntegerProperty sessions = new SimpleIntegerProperty();
  private final ObjectProperty<Duration> preferredWorkLength = new SimpleObjectProperty<>();
  private final IntegerProperty preferredWorkLengthInMinutes = new SimpleIntegerProperty();
  private boolean updatingPreferredWorkLength;
  private final ObjectProperty<Duration> defaultPreferredWorkLength = new SimpleObjectProperty<>();
  private final IntegerProperty defaultPreferredWorkLengthInMinutes = new SimpleIntegerProperty();
  private boolean updatingDefaultPreferredWorkLength;
  private final ReadOnlyIntegerWrapper minPreferredWorkLengthInMinutes =
      new ReadOnlyIntegerWrapper((int) Duration.of(PreferencesHelper.MIN_PREFERRED_WORK_LENGTH,
          PreferencesHelper.UNIT_PREFERRED_WORK_LENGTH).toMinutes());
  private final ReadOnlyIntegerWrapper maxPreferredWorkLengthInMinutes =
      new ReadOnlyIntegerWrapper((int) Duration.of(PreferencesHelper.MAX_PREFERRED_WORK_LENGTH,
          PreferencesHelper.UNIT_PREFERRED_WORK_LENGTH).toMinutes());
  private final BooleanProperty breakRecommendationRequested = new SimpleBooleanProperty();
  private final ObjectProperty<DialogState> dialogState = new SimpleObjectProperty<>();
  private final ObjectProperty<LocalTime> configFeedbackTimestamp = new SimpleObjectProperty<>();
  private final StringProperty configFeedbackMessage = new SimpleStringProperty();

  public ViewModel() {
    sessionStatusText.bind(Bindings.when(inSession).then("In session").otherwise("Not in session"));
    preferredWorkLength.addListener((_0, _1, value) -> {
      if (updatingPreferredWorkLength || value == null)
        return;
      try {
        updatingPreferredWorkLength = true;
        preferredWorkLengthInMinutes.set((int) value.toMinutes());
      } finally {
        updatingPreferredWorkLength = false;
      }
    });
    preferredWorkLengthInMinutes.addListener((_0, _1, value) -> {
      if (updatingPreferredWorkLength || value == null)
        return;
      try {
        updatingPreferredWorkLength = true;
        preferredWorkLength.set(Duration.ofMinutes(value.longValue()));
      } finally {
        updatingPreferredWorkLength = false;
      }
    });
    defaultPreferredWorkLength.addListener((_0, _1, value) -> {
      if (updatingDefaultPreferredWorkLength || value == null)
        return;
      try {
        updatingDefaultPreferredWorkLength = true;
        defaultPreferredWorkLengthInMinutes.set((int) value.toMinutes());
      } finally {
        updatingDefaultPreferredWorkLength = false;
      }
    });
    defaultPreferredWorkLengthInMinutes.addListener((_0, _1, value) -> {
      if (updatingDefaultPreferredWorkLength || value == null)
        return;
      try {
        updatingDefaultPreferredWorkLength = true;
        defaultPreferredWorkLength.set(Duration.ofMinutes(value.longValue()));
      } finally {
        updatingDefaultPreferredWorkLength = false;
      }
    });
  }

  public boolean getInSession() {
    return inSession.get();
  }

  public BooleanProperty isSessionProperty() {
    return inSession;
  }

  public void setInSession(boolean inSession) {
    this.inSession.set(inSession);
  }

  public String getSessionStatusText() {
    return sessionStatusText.get();
  }

  public ReadOnlyStringProperty sessionStatusTextProperty() {
    return sessionStatusText.getReadOnlyProperty();
  }

  public int getSessions() {
    return sessions.get();
  }

  public IntegerProperty sessionsProperty() {
    return sessions;
  }

  public void setSessions(int sessions) {
    this.sessions.set(sessions);
  }

  public Duration getPreferredWorkLength() {
    return preferredWorkLength.get();
  }

  public ObjectProperty<Duration> preferredWorkLengthProperty() {
    return preferredWorkLength;
  }

  public void setPreferredWorkLength(Duration preferredWorkLength) {
    this.preferredWorkLength.set(preferredWorkLength);
  }

  public int getPreferredWorkLengthInMinutes() {
    return preferredWorkLengthInMinutes.get();
  }

  public IntegerProperty preferredWorkLengthInMinutesProperty() {
    return preferredWorkLengthInMinutes;
  }

  public void setPreferredWorkLengthInMinutes(int preferredWorkLengthInMinutes) {
    this.preferredWorkLengthInMinutes.set(preferredWorkLengthInMinutes);
  }

  public Duration getDefaultPreferredWorkLength() {
    return defaultPreferredWorkLength.get();
  }

  public ObjectProperty<Duration> defaultPreferredWorkLengthProperty() {
    return defaultPreferredWorkLength;
  }

  public void setDefaultPreferredWorkLength(Duration defaultPreferredWorkLength) {
    this.defaultPreferredWorkLength.set(defaultPreferredWorkLength);
  }

  public int getDefaultPreferredWorkLengthInMinutes() {
    return defaultPreferredWorkLengthInMinutes.get();
  }

  public IntegerProperty defaultPreferredWorkLengthInMinutesProperty() {
    return defaultPreferredWorkLengthInMinutes;
  }

  public void setDefaultPreferredWorkLengthInMinutes(int defaultPreferredWorkLengthInMinutes) {
    this.defaultPreferredWorkLengthInMinutes.set(defaultPreferredWorkLengthInMinutes);
  }

  public int getMinPreferredWorkLengthInMinutes() {
    return minPreferredWorkLengthInMinutes.get();
  }

  public ReadOnlyIntegerProperty minPreferredWorkLengthInMinutesProperty() {
    return minPreferredWorkLengthInMinutes.getReadOnlyProperty();
  }

  public int getMaxPreferredWorkLengthInMinutes() {
    return maxPreferredWorkLengthInMinutes.get();
  }

  public ReadOnlyIntegerProperty maxPreferredWorkLengthInMinutesProperty() {
    return maxPreferredWorkLengthInMinutes.getReadOnlyProperty();
  }

  public boolean getBreakRecommendationRequested() {
    return breakRecommendationRequested.get();
  }

  public BooleanProperty breakRecommendationRequestedProperty() {
    return breakRecommendationRequested;
  }

  public void setBreakRecommendationRequested(boolean breakRecommendationRequested) {
    this.breakRecommendationRequested.set(breakRecommendationRequested);
  }

  public DialogState getDialogState() {
    return dialogState.get();
  }

  public ObjectProperty<DialogState> dialogStateProperty() {
    return dialogState;
  }

  public void setDialogState(DialogState dialogState) {
    this.dialogState.set(dialogState);
  }

  public LocalTime getConfigFeedbackTimestamp() {
    return configFeedbackTimestamp.get();
  }

  public ObjectProperty<LocalTime> configFeedbackTimestampProperty() {
    return configFeedbackTimestamp;
  }

  public void setConfigFeedbackTimestamp(LocalTime configFeedbackTimestamp) {
    this.configFeedbackTimestamp.set(configFeedbackTimestamp);
  }

  public String getConfigFeedbackMessage() {
    return configFeedbackMessage.get();
  }

  public StringProperty configFeedbackMessageProperty() {
    return configFeedbackMessage;
  }

  public void setConfigFeedbackMessage(String configFeedbackMessage) {
    this.configFeedbackMessage.set(configFeedbackMessage);
  }
}
