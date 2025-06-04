package per.goweii.popover.shadow

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.widget.PopupWindow
import androidx.appcompat.widget.ThemeUtils
import androidx.appcompat.widget.TintTypedArray
import androidx.core.content.ContextCompat
import per.goweii.popover.Popover
import per.goweii.popupshadowlayout.PopupShadowLayout
import per.goweii.popupshadowlayout.PopupShadowLayout.PopupShadowOutlineProvider

class ShadowPopover(context: Context) {
    private val popover = Popover(context)

    private val container = PopupShadowLayout(context)

    val window: PopupWindow get() = popover.window

    var roundedCornerRadius: Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        12F,
        context.resources.displayMetrics
    )
        set(value) {
            field = value
            container.cornerRadius = value.toInt().coerceAtLeast(0)
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

    var arrowWidth: Float = 0F
        set(value) {
            field = value
            container.arrowWidth = field.toInt().coerceAtLeast(0)
            if (popover.isShowing) {
                popover.update()
            }
        }

    var arrowHeight: Float = 0F
        set(value) {
            field = value
            container.arrowHeight = field.toInt().coerceAtLeast(0)
            if (popover.isShowing) {
                popover.update()
            }
        }

    var arrowCornerRadius: Float = 0F
        set(value) {
            field = value
            container.arrowRadius = field.toInt().coerceAtLeast(0)
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

    var background: Drawable?
        get() = container.background
        set(value) {
            container.background = value
        }

    val isShowing: Boolean
        get() = popover.isShowing

    init {
        try {
            val colorBackground = context.theme
                .obtainStyledAttributes(intArrayOf(android.R.attr.popupBackground))
                .use { it.getColor(0, Color.WHITE) }
            container.setBackgroundColor(colorBackground)
        } catch (ignore: Throwable) {
        }
        container.shadowRadius = shadowRadius.coerceAtLeast(0F)
        container.cornerRadius = roundedCornerRadius.toInt().coerceAtLeast(0)
        container.arrowWidth = arrowWidth.toInt().coerceAtLeast(0)
        container.arrowHeight = arrowHeight.toInt().coerceAtLeast(0)
        container.arrowRadius = arrowCornerRadius.toInt().coerceAtLeast(0)

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
        private val targetTemp = Rect()

        override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
            targetTemp.set(target.targetRect)

            val shadowRadius = shadowRadius.toInt().coerceAtLeast(0)

            val anchorRect = anchor.anchorRect

            var oldShadowLeft: Int
            var oldShadowTop: Int
            var oldShadowRight: Int
            var oldShadowBottom: Int

            do {
                oldShadowLeft = container.shadowInsets.left.toInt()
                oldShadowTop = container.shadowInsets.top.toInt()
                oldShadowRight = container.shadowInsets.right.toInt()
                oldShadowBottom = container.shadowInsets.bottom.toInt()

                target.targetRect.set(targetTemp)
                target.targetRect.offset(-oldShadowLeft, -oldShadowTop)

                target.targetRect.left += oldShadowLeft
                target.targetRect.top += oldShadowTop
                target.targetRect.right -= oldShadowRight
                target.targetRect.bottom -= oldShadowBottom

                alignment.compute(anchor, target)

                var newArrowSide = PopupShadowOutlineProvider.ARROW_SIDE_NONE
                if (arrowWidth > 0 && arrowHeight > 0) {
                    if (target.targetRect.bottom <= anchorRect.top) {
                        newArrowSide = PopupShadowOutlineProvider.ARROW_SIDE_BOTTOM
                    } else if (target.targetRect.top >= anchorRect.bottom) {
                        newArrowSide = PopupShadowOutlineProvider.ARROW_SIDE_TOP
                    } else if (target.targetRect.right <= anchorRect.left) {
                        newArrowSide = PopupShadowOutlineProvider.ARROW_SIDE_RIGHT
                    } else if (target.targetRect.left >= anchorRect.right) {
                        newArrowSide = PopupShadowOutlineProvider.ARROW_SIDE_LEFT
                    }
                }

                if (newArrowSide != container.arrowSide) {
                    container.arrowSide = newArrowSide
                    target.markNeedMeasure()
                }

                container.arrowAlign = PopupShadowOutlineProvider.ARROW_ALIGN_CENTER
                when (newArrowSide) {
                    PopupShadowOutlineProvider.ARROW_SIDE_TOP,
                    PopupShadowOutlineProvider.ARROW_SIDE_BOTTOM,
                        -> {
                        container.arrowOffset = anchorRect.centerX() - target.targetRect.centerX()
                    }

                    PopupShadowOutlineProvider.ARROW_SIDE_LEFT,
                    PopupShadowOutlineProvider.ARROW_SIDE_RIGHT,
                        -> {
                        container.arrowOffset = anchorRect.centerY() - target.targetRect.centerY()
                    }
                }

                val windowRect = anchor.windowRect
                if (windowRect.height() > 0) {
                    val fy = (target.targetRect.top.toFloat() / windowRect.height().toFloat())
                    container.shadowOffsetY = shadowRadius.toFloat() * fy
                }
                // if (windowRect.width() > 0) {
                //     val fx = (target.targetRect.left.toFloat() / windowRect.width().toFloat())
                //     container.shadowOffsetX = shadowRadius.toFloat() * fx
                // }

                target.targetRect.left -= oldShadowLeft
                target.targetRect.top -= oldShadowTop
                target.targetRect.right += oldShadowRight
                target.targetRect.bottom += oldShadowBottom

                val newShadowLeft = container.shadowInsets.left.toInt()
                val newShadowTop = container.shadowInsets.top.toInt()
                val newShadowRight = container.shadowInsets.right.toInt()
                val newShadowBottom = container.shadowInsets.bottom.toInt()

                val changed = newShadowLeft != oldShadowLeft
                        || newShadowTop != oldShadowTop
                        || newShadowRight != oldShadowRight
                        || newShadowBottom != oldShadowBottom
            } while (changed)
        }
    }
}