package com.breaktimebuddy;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

public class ViewModel {

    // --- SESSION COUNT ---
    private final IntegerProperty sessions = new SimpleIntegerProperty(0);
    public IntegerProperty sessionsProperty() {
        return sessions;
    }

    // --- CONFIG FEEDBACK ---
    private final StringProperty configFeedbackMessage = new SimpleStringProperty("");
    private final StringProperty configFeedbackTimestamp = new SimpleStringProperty("");

    public String getConfigFeedbackMessage() {
        return configFeedbackMessage.get();
    }

    public StringProperty configFeedbackMessageProperty() {
        return configFeedbackMessage;
    }

    public String getConfigFeedbackTimestamp() {
        return configFeedbackTimestamp.get();
    }

    public StringProperty configFeedbackTimestampProperty() {
        return configFeedbackTimestamp;
    }

    public void setConfigFeedback(String timestamp, String message) {
        configFeedbackTimestamp.set(timestamp);
        configFeedbackMessage.set(message);
    }

    // --- SESSION ACTIVE FLAG ---
    private final BooleanProperty sessionActive = new SimpleBooleanProperty(false);

    public boolean isSessionActive() {
        return sessionActive.get();
    }

    public BooleanProperty sessionActiveProperty() {
        return sessionActive;
    }

    public void toggleSession() {
        sessionActive.set(!sessionActive.get());
    }

    // --- TAKE BREAK (placeholder) ---
    public void takeBreak() {
        // TODO: implement break logic later
        sessions.set(sessions.get() + 1);
    }

    // --- AI RECOMMENDATION ---
    private final StringProperty aiRecommendation = new SimpleStringProperty("");

    public StringProperty aiRecommendationProperty() {
        return aiRecommendation;
    }

    public void setAiRecommendation(String value) {
        aiRecommendation.set(value);
    }
}
