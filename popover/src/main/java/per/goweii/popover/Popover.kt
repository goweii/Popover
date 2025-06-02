package per.goweii.popover

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Choreographer
import android.view.Gravity
import android.view.View
import android.view.View.MeasureSpec
import android.widget.PopupWindow
import java.lang.ref.WeakReference

/**
 * 浮动窗口
 *
 * 对 PopupWindow 的封装，支持多菜单项和自定义对齐方式
 *
 * 默认在 anchor 下方且右对齐，如果空间不足，则切换为上方或者左对齐
 *
 * ```
 * +--------+
 * | anchor |
 * +--------+
 * +-------------+
 * | Save        |
 * + - - - - - - +
 * | Delete      |
 * + - - - - - - +
 * | Cancel      |
 * +-------------+
 * ```
 *
 * 简单使用
 * ``` kotlin
 * Popover(context)
 * .setContentView(contentView)
 * .show(anchorView)
 * ```
 */
class Popover(context: Context) {
    private val popupWindow = PopupWindow(context)

    private var anchorRef: WeakReference<Anchor>? = null

    private val anchor: Anchor? get() = anchorRef?.get()

    private val rectTemp = Rect()

    val isShowing: Boolean
        get() = popupWindow.isShowing

    var contentView: View?
        get() = popupWindow.contentView
        set(value) {
            popupWindow.contentView = value
            if (isShowing) {
                update()
            }
        }

    /**
     * 控制对齐方式和动画，详见 [Alignment]
     */
    var alignment: Alignment = RelativeAlignment()
        set(value) {
            field = value
            if (isShowing) {
                update()
            }
        }

    init {
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable())
        popupWindow.isOutsideTouchable = true
        popupWindow.isClippingEnabled = false
        popupWindow.setOnDismissListener { onDismiss() }
    }

    @SuppressLint("RtlHardcoded")
    fun show(anchorView: View) {
        val contentView = contentView ?: return
        val anchor = attachAnchor(anchorView)

        val targetRect = computeTargetRect(anchor, contentView)

        popupWindow.animationStyle = computeDefaultAnimationStyle(anchor, targetRect)

        popupWindow.width = targetRect.width()
        popupWindow.height = targetRect.height()
        popupWindow.showAtLocation(
            anchorView,
            Gravity.TOP or Gravity.LEFT,
            targetRect.left,
            targetRect.top
        )
    }

    fun dismiss() {
        popupWindow.dismiss()
    }

    fun update() {
        val contentView = contentView ?: return
        val anchor = anchor ?: return

        val targetRect = computeTargetRect(anchor, contentView)

        if (targetRect.isEmpty) {
            return
        }

        popupWindow.animationStyle = computeDefaultAnimationStyle(anchor, targetRect)

        popupWindow.update(
            anchor.view,
            targetRect.left,
            targetRect.top,
            targetRect.width(),
            targetRect.height()
        )
    }

    private fun onDismiss() {
        detachAnchor()
    }

    private fun attachAnchor(anchor: View): Anchor {
        this.anchorRef?.clear()
        val referent = Anchor(anchor)
        this.anchorRef = WeakReference(referent)
        return referent
    }

    private fun detachAnchor() {
        this.anchorRef?.clear()
        this.anchorRef = null
    }

    @SuppressLint("CheckResult")
    private fun computeTargetRect(anchor: Anchor, contentView: View): Rect {
        val targetRect = rectTemp
        targetRect.setEmpty()

        val alignment = alignment

        val windowRect = anchor.windowRect
        val anchorRect = anchor.anchorRect

        val suggestWidth = windowRect.width()
        val suggestHeight = windowRect.height()

        // do {
        preMeasure(contentView, suggestWidth, suggestHeight)
        val measuredWidth = contentView.measuredWidth
        val measuredHeight = contentView.measuredHeight
        targetRect.set(0, 0, measuredWidth, measuredHeight)
        targetRect.offset(anchorRect.left, anchorRect.bottom)
        alignment.compute(anchor, targetRect)
        // targetRect.intersect(windowRect)
        //     suggestWidth = targetRect.width()
        //     suggestHeight = targetRect.height()
        //     val changed = suggestWidth != measuredWidth || suggestHeight != measuredHeight
        // } while (changed)

        return targetRect
    }

    /**
     * 预测量
     */
    private fun preMeasure(contentView: View, parentWidth: Int, parentHeight: Int) {
        val wSpec = MeasureSpec.makeMeasureSpec(parentWidth, MeasureSpec.AT_MOST)
        val hSpec = MeasureSpec.makeMeasureSpec(parentHeight, MeasureSpec.AT_MOST)
        contentView.measure(wSpec, hSpec)
    }

    private fun computeDefaultAnimationStyle(
        anchor: Anchor,
        target: Rect,
    ): Int {
        val anchorRect = anchor.anchorRect

        return if (target.centerY() > anchorRect.centerY()) {
            R.style.Popover_Animation_DropDownDown
        } else if (target.centerY() < anchorRect.centerY()) {
            R.style.Popover_Animation_DropDownUp
        } else {
            R.style.Popover_Animation_Fade
        }
    }

    class Anchor(val view: View) {
        private val locationTemp: IntArray = intArrayOf(0, 0)
        private val windowRectTemp: Rect = Rect()
        private val anchorRectTemp: Rect = Rect()
        private val remainingTemp: Rect = Rect()

        private val frameCallback = Choreographer.FrameCallback { maybeDirty = true }
        private var maybeDirty: Boolean = true

        val width: Int get() = view.width
        val height: Int get() = view.height

        val windowRect: Rect
            get() {
                ensureUpdateToDate()
                return windowRectTemp
            }

        val anchorRect: Rect
            get() {
                ensureUpdateToDate()
                return anchorRectTemp
            }

        val remaining: Rect
            get() {
                ensureUpdateToDate()
                return remainingTemp
            }

        private fun ensureUpdateToDate() {
            if (!maybeDirty) return
            updateInternal()
            maybeDirty = false
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }

        private fun updateInternal() {
            view.rootView.getWindowVisibleDisplayFrame(windowRectTemp)
            windowRectTemp.offsetTo(0, 0)

            view.getLocationInWindow(locationTemp)

            anchorRectTemp.left = locationTemp[0]
            anchorRectTemp.right = locationTemp[0] + view.width
            anchorRectTemp.top = locationTemp[1]
            anchorRectTemp.bottom = locationTemp[1] + view.height

            remainingTemp.left = anchorRectTemp.left - windowRectTemp.left
            remainingTemp.right = windowRectTemp.right - anchorRectTemp.right
            remainingTemp.top = anchorRectTemp.top - windowRectTemp.top
            remainingTemp.bottom = windowRectTemp.bottom - anchorRectTemp.bottom
        }
    }

    /**
     * 控制对齐方式和动画
     */
    interface Alignment {
        /**
         * 通过控制 offset 实现相对于 anchor 的对齐方式
         *
         * @param anchor 锚点
         * @param target 目标位置
         */
        fun compute(anchor: Anchor, target: Rect)
    }
}