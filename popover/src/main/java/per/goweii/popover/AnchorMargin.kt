package per.goweii.popover

import android.graphics.Rect
import per.goweii.popover.Popover.Anchor
import per.goweii.popover.Popover.Alignment

class AnchorMargin(
    private val alignment: Alignment,
    private val margin: Int = 0,
) : Alignment {
    private val rectTemp = Rect()

    override fun compute(anchor: Anchor, target: Rect) {
        rectTemp.set(target)

        val anchorRect = anchor.anchorRect

        var dx = 0
        var dy = 0

        do {
            target.set(rectTemp)
            target.offset(dx, dy)

            dx = 0
            dy = 0

            alignment.compute(anchor, target)

            if (target.right <= anchorRect.left) {
                val g = anchorRect.left - target.right
                if (g < margin) {
                    dx = -(margin - g)
                }
            } else if (target.left >= anchorRect.right) {
                val g = target.left - anchorRect.right
                if (g < margin) {
                    dx = margin - g
                }
            }

            if (target.bottom <= anchorRect.top) {
                val g = anchorRect.top - target.bottom
                if (g < margin) {
                    dy = -(margin - g)
                }
            } else if (target.top >= anchorRect.bottom) {
                val g = target.top - anchorRect.bottom
                if (g < margin) {
                    dy = margin - g
                }
            }

        } while (dx != 0 || dy != 0)
    }
}