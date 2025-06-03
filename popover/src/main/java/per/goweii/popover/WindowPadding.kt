package per.goweii.popover

import android.graphics.Rect
import android.view.View

class WindowPadding(
    private val alignment: Popover.Alignment,
    private val paddingStart: Int = 0,
    private val paddingEnd: Int = 0,
    private val paddingTop: Int = 0,
    private val paddingBottom: Int = 0,
) : Popover.Alignment {
    companion object {
        fun all(
            alignment: Popover.Alignment,
            padding: Int,
        ) = WindowPadding(
            alignment = alignment,
            paddingStart = padding,
            paddingEnd = padding,
            paddingTop = padding,
            paddingBottom = padding,
        )
    }

    private val rectTemp = Rect()

    override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
        val windowRect = anchor.windowRect

        // save
        rectTemp.set(windowRect)

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

        alignment.compute(anchor, target)

        // restore
        windowRect.set(rectTemp)
    }
}