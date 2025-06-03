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

    private var target: Target? = null

    val isShowing: Boolean
        get() = popupWindow.isShowing

    var contentView: View?
        get() = target?.view
        set(value) {
            target = value?.let { Target(it) }
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
        val target = target ?: return
        val anchor = attachAnchor(anchorView)

        computeTarget(anchor, target)

        popupWindow.animationStyle = computeDefaultAnimationStyle(anchor, target)

        val targetRect = target.targetRect

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
        val target = target ?: return
        val anchor = anchor ?: return

        computeTarget(anchor, target)

        popupWindow.animationStyle = computeDefaultAnimationStyle(anchor, target)

        popupWindow.update(
            anchor.view,
            target.targetRect.left,
            target.targetRect.top,
            target.targetRect.width(),
            target.targetRect.height()
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
    private fun computeTarget(anchor: Anchor, target: Target) {
        target.targetRect.setEmpty()
        do {
            val suggestWidth = anchor.windowRect.width()
            val suggestHeight = anchor.windowRect.height()
            target.preMeasure(suggestWidth, suggestHeight)
            val measuredWidth = target.measuredWidth
            val measuredHeight = target.measuredHeight
            target.targetRect.set(0, 0, measuredWidth, measuredHeight)
            target.targetRect.offset(anchor.anchorRect.left, anchor.anchorRect.bottom)
            alignment.compute(anchor, target)
        } while (target.isMeasureRequested)
    }

    private fun computeDefaultAnimationStyle(anchor: Anchor, target: Target): Int {
        val anchorRect = anchor.anchorRect
        val targetRect = target.targetRect

        return if (targetRect.centerY() > anchorRect.centerY()) {
            R.style.Popover_Animation_DropDownDown
        } else if (targetRect.centerY() < anchorRect.centerY()) {
            R.style.Popover_Animation_DropDownUp
        } else {
            R.style.Popover_Animation_Fade
        }
    }

    class Target(val view: View) {
        private val targetRectTemp = Rect()

        private var needMeasure: Boolean = true

        val targetRect: Rect get() = targetRectTemp

        val measuredWidth: Int get() = view.measuredWidth
        val measuredHeight: Int get() = view.measuredHeight

        val isMeasureRequested: Boolean get() = needMeasure

        fun markNeedMeasure() {
            needMeasure = true
        }

        fun preMeasure(parentWidth: Int, parentHeight: Int) {
            val wSpec = MeasureSpec.makeMeasureSpec(parentWidth, MeasureSpec.AT_MOST)
            val hSpec = MeasureSpec.makeMeasureSpec(parentHeight, MeasureSpec.AT_MOST)
            view.measure(wSpec, hSpec)
            needMeasure = false
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
         * @param target 目标
         */
        fun compute(anchor: Anchor, target: Target)
    }
}