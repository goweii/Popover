package per.goweii.popover

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Outline
import android.util.TypedValue
import android.view.View
import android.view.ViewOutlineProvider

class RoundedPopover(context: Context) {
    private val popover = Popover(context)

    private val roundedCornerRadius = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        10F,
        context.resources.displayMetrics
    )

    private val roundedOutlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, roundedCornerRadius)
        }
    }

    var contentView: View
        get() = popover.contentView
        set(value) {
            popover.contentView = value
        }

    var alignment: Popover.Alignment
        get() = popover.alignment
        set(value) {
            popover.alignment = value
        }

    val isShowing: Boolean
        get() = popover.isShowing

    @SuppressLint("RtlHardcoded")
    fun show(anchor: View) {
    }

    fun dismiss() {
        popover.dismiss()
    }

}