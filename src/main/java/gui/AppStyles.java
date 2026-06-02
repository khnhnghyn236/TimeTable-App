package gui;

import javafx.animation.FadeTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class AppStyles {
    public static final String BG_PAGE = "#F5F7FA";
    public static final String WHITE = "#FFFFFF";
    public static final String BG_FIELD = "#EEF2F7";
    public static final String STATUS_APPROVED = "#10B981"; // Green
    public static final String STATUS_DECLINED = "#EF4444"; // Red
    public static final String STATUS_PENDING = "#F59E0B";  // Amber
    public static final String TEXT_MUTED = "#64748B";
    public static final String BLUE_MID = "#3B82F6";
    public static final String STATUS_DEFAULT = "#CBD5E1";
    public static final String ERROR_RED = "#EF4444";
    public static final String WARN_BG = "#FEF3C7";

    // STEP 1: Define shared text styles
    public static Label headerLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        return l;
    }

    // STEP 2: Define shared button styles
    public static Button ghostButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: transparent; -fx-border-color: " + STATUS_DEFAULT + "; -fx-border-radius: 4; -fx-cursor: hand;");
        return b;
    }

    public static Button primaryButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + BLUE_MID + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
        return b;
    }

    public static Button successButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + STATUS_APPROVED + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
        return b;
    }

    public static Button dangerButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + ERROR_RED + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
        return b;
    }

    // STEP 3: Define utility method for fading status messages
    public static void showStatus(Label lbl, String msg, String textColor, String bgColor) {
        lbl.setText(msg);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: " + textColor + "; -fx-background-color: " + bgColor + "; -fx-background-radius: 6; -fx-padding: 8 12 8 12;");
        FadeTransition ft = new FadeTransition(Duration.millis(300), lbl);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }
}
