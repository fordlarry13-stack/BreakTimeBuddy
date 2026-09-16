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

public class ViewModel {

    // --- EXISTING PROPERTIES ---
    private final BooleanProperty inSession = new SimpleBooleanProperty();
    private final ReadOnlyStringWrapper sessionStatusText = new ReadOnlyStringWrapper();
    private final IntegerProperty sessions = new SimpleIntegerProperty();
    private final ObjectProperty<LocalTime> configFeedbackTimestamp = new SimpleObjectProperty<>();
    private final StringProperty configFeedbackMessage = new SimpleStringProperty();

    // --- NEW: AI Recommendation ---
    private final StringProperty aiRecommendation = new SimpleStringProperty("");

    public ViewModel() {
        sessionStatusText.bind(
                Bindings.when(inSession)
                        .then("In session")
                        .otherwise("Not in session")
        );
    }

    // --- EXISTING GETTERS/SETTERS ---
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

    // -------------------------------------------------------------------------
    // ⭐ NEW METHODS REQUIRED BY ViewBuilder
    // -------------------------------------------------------------------------

    // 1. Wrapper for session state
    public boolean isSessionActive() {
        return inSession.get();
    }

    // 2. Placeholder break logic
    public void takeBreak() {
        sessions.set(sessions.get() + 1);
    }

    // 3. AI Recommendation property
    public StringProperty aiRecommendationProperty() {
        return aiRecommendation;
    }

    public void setAiRecommendation(String value) {
        aiRecommendation.set(value);
    }
}