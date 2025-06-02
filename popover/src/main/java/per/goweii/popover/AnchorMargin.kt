package per.goweii.popover

import android.graphics.Rect
import android.view.View
import per.goweii.popover.Popover.Anchor
import per.goweii.popover.Popover.Alignment
import per.goweii.popover.Popover.AlignmentWrapper

class AnchorMarginAlignmentWrapper(
    original: Alignment,
    private val gap: Int = 0,
) : AlignmentWrapper(original) {
    override fun compute(anchor: Anchor, target: Rect) {
        val anchorRect = anchor.anchorRect

        val rtl = anchor.view.layoutDirection == View.LAYOUT_DIRECTION_RTL
        if (rtl) {
            anchorRect.left -= gap
            anchorRect.right += gap
        } else {
            anchorRect.left -= gap
            anchorRect.right += gap
        }
        anchorRect.top -= gap
        anchorRect.bottom += gap

        super.compute(anchor, target)
    }
}