package per.goweii.popover.shadow

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import per.goweii.popover.Popover
import per.goweii.popover.Popover.Anchor
import per.goweii.roundedshadowlayout.RoundedShadowLayout.RoundedShadowOutlineProvider
import per.goweii.shadowlayout.ShadowLayout
import kotlin.math.abs

class ShadowPopover(context: Context) {
    private val popover = Popover(context)

    private val container = ShadowLayout(context)

    private val shadowProvider = RoundedShadowOutlineProvider()

    var roundedCornerRadius: Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        12F,
        context.resources.displayMetrics
    )
        set(value) {
            field = value
            if (popover.isShowing) {
                popover.update()
            }
        }

    var shadowRadius: Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        12F,
        context.resources.displayMetrics
    )
        set(value) {
            field = value
            if (popover.isShowing) {
                popover.update()
            }
        }

    var contentView: View?
        get() {
            return if (container.childCount > 0) {
                container.getChildAt(0)
            } else {
                null
            }
        }
        set(value) {
            container.removeAllViewsInLayout()
            container.addView(value)
        }

    var alignment: Popover.Alignment
        get() {
            val alignment = popover.alignment
            if (alignment is ShadowAlignment) {
                return alignment.alignment
            }
            return alignment
        }
        set(value) {
            popover.alignment = ShadowAlignment(value)
        }

    val isShowing: Boolean
        get() = popover.isShowing

    init {
        container.shadowRadius = abs(shadowRadius)
        container.shadowOutlineProvider = shadowProvider.apply {
            setCornerRadius(roundedCornerRadius.coerceAtLeast(0F))
        }

        popover.contentView = container

        alignment = popover.alignment
    }

    @SuppressLint("RtlHardcoded")
    fun show(anchorView: View) {
        popover.show(anchorView)
    }

    fun dismiss() {
        popover.dismiss()
    }

    private inner class ShadowAlignment(
        val alignment: Popover.Alignment
    ) : Popover.Alignment {
        private val rectTemp = Rect()

        override fun compute(anchor: Anchor, target: Rect) {
            val shadowRadius = abs(shadowRadius).toInt()

            var oldShadowLeft: Int
            var oldShadowTop: Int
            var oldShadowRight: Int
            var oldShadowBottom: Int

            do {
                oldShadowLeft = container.shadowInsets.left.toInt()
                oldShadowTop = container.shadowInsets.top.toInt()
                oldShadowRight = container.shadowInsets.right.toInt()
                oldShadowBottom = container.shadowInsets.bottom.toInt()

                rectTemp.set(target)
                rectTemp.offset(-oldShadowLeft, -oldShadowTop)

                rectTemp.left += oldShadowLeft
                rectTemp.top += oldShadowTop
                rectTemp.right -= oldShadowRight
                rectTemp.bottom -= oldShadowBottom

                alignment.compute(anchor, rectTemp)

                val windowRect = anchor.windowRect
                if (windowRect.height() > 0) {
                    val fy = (rectTemp.top.toFloat() / windowRect.height().toFloat()).coerceIn(0F, 1F)
                    container.shadowOffsetY = shadowRadius.toFloat() * fy
                }
                // if (windowRect.width() > 0) {
                //     val fx = (rectTemp.left.toFloat() / windowRect.width().toFloat()).coerceIn(0F, 1F)
                //     container.shadowOffsetX = shadowRadius.toFloat() * fx
                // }

                rectTemp.left -= oldShadowLeft
                rectTemp.top -= oldShadowTop
                rectTemp.right += oldShadowRight
                rectTemp.bottom += oldShadowBottom

                val newShadowLeft = container.shadowInsets.left.toInt()
                val newShadowTop = container.shadowInsets.top.toInt()

                val changed = newShadowLeft != oldShadowLeft || newShadowTop != oldShadowTop
            } while (changed)

            target.set(rectTemp)
        }
    }
}