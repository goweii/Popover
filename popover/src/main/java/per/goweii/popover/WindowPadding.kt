package per.goweii.popover

import android.graphics.Rect
import android.view.View
import per.goweii.popover.Popover.Anchor
import per.goweii.popover.Popover.Alignment
import per.goweii.popover.Popover.AlignmentWrapper

class WindowMarginAlignmentWrapper(
    original: Alignment,
    private val paddingStart: Int = 0,
    private val paddingEnd: Int = 0,
    private val paddingTop: Int = 0,
    private val paddingBottom: Int = 0,
) : AlignmentWrapper(original) {
    override fun compute(anchor: Anchor, target: Rect) {
        val windowRect = anchor.windowRect

        val rtl = anchor.view.layoutDirection == View.LAYOUT_DIRECTION_RTL
        if (rtl) {
            windowRect.left += paddingEnd
            windowRect.right -= paddingStart
        } else {
            windowRect.left += paddingStart
            windowRect.right -= paddingEnd
        }
        windowRect.top += paddingTop
        windowRect.bottom -= paddingBottom

        super.compute(anchor, target)
    }
}