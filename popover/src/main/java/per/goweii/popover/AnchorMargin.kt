package per.goweii.popover

import android.graphics.Rect

class AnchorMargin(
    private val alignment: Popover.Alignment,
    private val margin: Int = 0,
) : Popover.Alignment {
    private val rectTemp = Rect()

    override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
        rectTemp.set(target.targetRect)

        val targetRect = target.targetRect
        val anchorRect = anchor.anchorRect

        var dx = 0
        var dy = 0

        do {
            targetRect.set(rectTemp)
            targetRect.offset(dx, dy)

            dx = 0
            dy = 0

            alignment.compute(anchor, target)

            if (targetRect.right <= anchorRect.left) {
                val g = anchorRect.left - targetRect.right
                if (g < margin) {
                    dx = -(margin - g)
                }
            } else if (targetRect.left >= anchorRect.right) {
                val g = targetRect.left - anchorRect.right
                if (g < margin) {
                    dx = margin - g
                }
            }

            if (targetRect.bottom <= anchorRect.top) {
                val g = anchorRect.top - targetRect.bottom
                if (g < margin) {
                    dy = -(margin - g)
                }
            } else if (targetRect.top >= anchorRect.bottom) {
                val g = targetRect.top - anchorRect.bottom
                if (g < margin) {
                    dy = margin - g
                }
            }

        } while (dx != 0 || dy != 0)
    }
}