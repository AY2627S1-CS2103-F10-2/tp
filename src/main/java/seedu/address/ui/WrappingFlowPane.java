package seedu.address.ui;

import javafx.scene.Node;
import javafx.scene.layout.FlowPane;

/**
 * A horizontal flow that measures wrapping children at the width they will actually receive.
 * Measurement must not depend on a previous layout width: virtualized cells can be measured
 * before their first layout, or after being reused at a different width.
 */
public class WrappingFlowPane extends FlowPane {
    @Override
    protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    @Override
    protected double computePrefHeight(double width) {
        double contentWidth = width < 0 ? getPrefWrapLength()
                : Math.max(0, width - snappedLeftInset() - snappedRightInset());
        return snappedTopInset() + layoutRows(contentWidth, false) + snappedBottomInset();
    }

    @Override
    protected void layoutChildren() {
        layoutRows(Math.max(0, getWidth() - snappedLeftInset() - snappedRightInset()), true);
    }

    /** Uses identical row sizes for preferred-height calculation and actual layout. */
    private double layoutRows(double availableWidth, boolean positionChildren) {
        double x = 0;
        double y = 0;
        double rowHeight = 0;
        double horizontalGap = snapSpaceX(getHgap());
        double verticalGap = snapSpaceY(getVgap());
        boolean firstInRow = true;
        for (Node child : getManagedChildren()) {
            double childWidth = Math.min(availableWidth, snapSizeX(child.prefWidth(-1)));
            double childHeight = snapSizeY(child.prefHeight(childWidth));
            double nextX = firstInRow ? 0 : x + horizontalGap;
            if (!firstInRow && nextX + childWidth > availableWidth) {
                y += rowHeight + verticalGap;
                nextX = 0;
                rowHeight = 0;
            }
            if (positionChildren) {
                child.resizeRelocate(snappedLeftInset() + nextX, snappedTopInset() + y, childWidth, childHeight);
            }
            x = nextX + childWidth;
            rowHeight = Math.max(rowHeight, childHeight);
            firstInRow = false;
        }
        return y + rowHeight;
    }
}
