package seedu.address.ui;

import javafx.scene.text.TextFlow;

/** A wrapping heading whose height stays consistent in virtualized list rows. */
public class WrappingTextFlow extends TextFlow {

    @Override
    protected double computePrefHeight(double width) {
        // Fractional text heights can make VirtualFlow stop before the requested last row.
        return Math.ceil(super.computePrefHeight(width));
    }
}
