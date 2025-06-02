package com.autel.evops.widget.popover

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Outline
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.core.view.doOnLayout
import com.autel.alog.core.ALog
import com.autel.evops.R
import com.autel.evops.databinding.PopoverBinding
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
 * | Save |
 * + - - - - - - +
 * | Delete |
 * + - - - - - - +
 * | Cancel |
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
@Unstable
class Popover(context: Context) {
 private val binding = PopoverBinding.inflate(LayoutInflater.from(context))
 private val popupWindow = PopupWindow(
 binding.root,
 ViewGroup.LayoutParams.WRAP_CONTENT,
 ViewGroup.LayoutParams.WRAP_CONTENT,
 true,
 )

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

 private var anchorRef: WeakReference<Anchor>? = null

 private val anchor: Anchor? get() = anchorRef?.get()

 val contentContainer: FrameLayout
 get() = binding.content

 /**
 * 控制对齐方式和动画，详见 [Alignment]
 */
 var alignment: Alignment = Alignment.RelativeAlign()
 set(value) {
 field = value
 if (isShowing) {
 updateAlignment(value)
 }
 }

 init {
 binding.root.setOnClickListener {
 dismiss()
 }
 popupWindow.setBackgroundDrawable(ColorDrawable())
 popupWindow.isOutsideTouchable = true
 popupWindow.isClippingEnabled = false
 popupWindow.setOnDismissListener {
 onDismiss()
 }
 }

 val isShowing: Boolean
 get() = popupWindow.isShowing

 fun setContentView(view: View): Popover = apply {
 binding.content.apply {
 for (i in 0 until childCount) {
 val child = getChildAt(i)
 if (child.outlineProvider === roundedOutlineProvider) {
 child.outlineProvider = null
 child.clipToOutline = false
 }
 }
 removeAllViews()
 addView(view)
 if (view.outlineProvider !== roundedOutlineProvider) {
 view.outlineProvider = roundedOutlineProvider
 view.clipToOutline = true
 }
 }
 }

 @SuppressLint("RtlHardcoded")
 fun show(anchor: View) {
 val anchorDelegate = attachAnchor(anchor)

 preMeasure(anchor.rootView)

 val shadowSize = (binding.root.measuredWidth - binding.content.measuredWidth) / 2

 val offset = Point()

 alignment.calculateOffset(
 offset,
 anchorDelegate,
 binding.content.measuredWidth,
 binding.content.measuredHeight,
 )

 popupWindow.animationStyle = calculateDefaultAnimationStyle(offset)

 offset.x -= shadowSize
 offset.y -= shadowSize

 val l = intArrayOf(0, 0)
 anchor.getLocationInWindow(l)
 offset.offset(l[0], l[1] + anchor.height)

 popupWindow.width = binding.root.measuredWidth
 popupWindow.height = binding.root.measuredHeight
 popupWindow.showAtLocation(anchor, Gravity.TOP or Gravity.LEFT, offset.x, offset.y)
 ALog.d("POPOVER", "w1 = ${binding.root.measuredWidth}")
 binding.root.doOnLayout {
 ALog.d("POPOVER", "w2 = ${binding.root.measuredWidth}")
 }
 }

 fun dismiss() {
 popupWindow.dismiss()
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

 private fun calculateDefaultAnimationStyle(offset: Point): Int {
 val anchor = anchor ?: return -1

 val anchorRect = Rect(0, 0, anchor.width, anchor.height)
 val l = intArrayOf(0, 0).also { anchor.view.getLocationOnScreen(it) }
 anchorRect.offset(l[0], l[1])

 val contentRect = Rect(0, 0, binding.content.measuredWidth, binding.content.measuredHeight)
 contentRect.offset(anchorRect.left, anchorRect.bottom)
 contentRect.offset(offset.x, offset.y)

 return if (contentRect.top >= anchorRect.bottom) {
 R.style.PopoverAnimation_FromTop
 } else if (contentRect.bottom <= anchorRect.top) {
 R.style.PopoverAnimation_FromBottom
 } else {
 R.style.PopoverAnimation_FromCenter
 }
 }

 private fun updateAlignment(alignment: Alignment) {
 val anchor = anchor ?: return

 val shadowSize = (binding.root.measuredWidth - binding.content.measuredWidth) / 2

 val offset = Point()

 alignment.calculateOffset(
 offset,
 anchor,
 binding.content.measuredWidth,
 binding.content.measuredHeight,
 )

 offset.x -= shadowSize
 offset.y -= shadowSize

 popupWindow.animationStyle = calculateDefaultAnimationStyle(offset)

 popupWindow.update(
 anchor.view, offset.x, offset.y,
 binding.root.measuredWidth, binding.root.measuredHeight
 )
 }

 /**
 * 预测量
 */
 private fun preMeasure(rootView: View) {
 val wSpec = MeasureSpec.makeMeasureSpec(rootView.width, MeasureSpec.UNSPECIFIED)
 val hSpec = MeasureSpec.makeMeasureSpec(rootView.height, MeasureSpec.UNSPECIFIED)
 this.binding.root.measure(wSpec, hSpec)
 }

 class Anchor(val view: View) {
 private val remaining: Rect = Rect()
 private val remainingTemp: Rect = Rect()
 private var remainingUpdateTime: Long = 0L

 val width: Int get() = view.width
 val height: Int get() = view.height

 fun getRemaining(): Rect {
 val currentTime = System.currentTimeMillis()

 if (currentTime - remainingUpdateTime > 16) {
 remainingUpdateTime = currentTime
 updateAnchorRemainingInternal(view, remaining)
 }

 remainingTemp.set(remaining)

 return remainingTemp
 }

 private fun updateAnchorRemainingInternal(anchor: View, remaining: Rect) {
 remaining.setEmpty()

 val rootView = anchor.rootView
 val displayFrame = Rect()
 rootView.getWindowVisibleDisplayFrame(displayFrame)

 val anchorPos: IntArray = intArrayOf(0, 0)
 anchor.getLocationOnScreen(anchorPos)

 remaining.left = (anchorPos[0] - displayFrame.left)
 .coerceAtLeast(0)
 remaining.right = (displayFrame.right - (anchorPos[0] + anchor.width))
 .coerceAtLeast(0)
 remaining.top = (anchorPos[1] - displayFrame.top)
 .coerceAtLeast(0)
 remaining.bottom = (displayFrame.bottom - (anchorPos[1] + anchor.height))
 .coerceAtLeast(0)
 }
 }

 /**
 * 控制对齐方式和动画
 */
 interface Alignment {
 /**
 * 通过控制 offset 实现相对于 anchor 的对齐方式
 *
 * @param offset 相对于 anchor 的对齐方式
 * @param anchor 锚点 view
 * @param width 菜单浮窗的宽度
 * @param height 菜单浮窗的高度
 */
 fun calculateOffset(
 offset: Point,
 anchor: Anchor,
 width: Int,
 height: Int,
 ) = Unit

 /**
 * 按水平和垂直方向相对位置控制对齐方式
 */
 class RelativeAlign(
 private val vertical: Vertical = Vertical.BellowOrAbove,
 private val horizontal: Horizontal = Horizontal.AlignEndOrStart,
 ) : Alignment {
 sealed class Vertical {
 abstract fun calculateOffset(
 anchor: Anchor,
 width: Int,
 height: Int
 ): Int

 data object Above : Vertical() {
 override fun calculateOffset(
 anchor: Anchor,
 width: Int,
 height: Int
 ): Int {
 return height - anchor.view.height
 }
 }

 data object AlignTop : Vertical() {
 override fun calculateOffset(
 anchor: Anchor,
 width: Int,
 height: Int
 ): Int {
 return -anchor.view.height
 }
 }

 data object Center : Vertical() {
 override fun calculateOffset(
 anchor: Anchor,
 width: Int,
 height: Int
 ): Int {
 return -height / 2 - anchor.view.height / 2
 }
 }

 data object AlignBottom : Vertical() {
 override fun calculateOffset(
 anchor: Anchor,
 width: Int,
 height: Int
 ): Int {
 return -height
 }
 }

 data object Bellow : Vertical() {
 override fun calculateOffset(
 anchor: Anchor,
 width: Int,
 height: Int
 ): Int {
 return 0
 }
 }

 data object BellowOrAbove : Vertical() {
 override fun calculateOffset(
 anchor: Anchor,
 width: Int,
 height: Int
 ): Int {
 val remaining = anchor.getRemaining()
 return if (remaining.bottom > height) {
 Bellow.calculateOffset(anchor, width, height)
 } else if (remaining.top > height) {
 Above.calculateOffset(anchor, width, height)
 } else {
 if (remaining.bottom >= remaining.top) {
 Bellow.calculateOffset(anchor, width, height)
 } else {
 Above.calculateOffset(anchor, width, height)
 }
 }
 }
 }
 }

 sealed class Horizontal {
 abstract fun calculateOffset(
 anchor: Anchor,
 width: Int,
 height: Int
 ): Int

 data object ToLeft : Horizontal() {
 override fun calculateOffset(
 anchor: Anchor,
 width: Int,
 height: Int
 ): Int {
 return -width
 }
 }

 data object AlignLeft : Horizontal() {
 override fun calculateOffset(
 anchor: Anchor,
 width: Int,
 height: Int
 ): Int {
 return 0
 }
 }

 data object Center : Horizontal() {
 override fun calculateOffset(
 anchor: Anchor,
 width: Int,
 height: Int
 ): Int {
 return -width / 2 + anchor.width / 2
 }
 }

 data object AlignRight : Horizontal() {
 override fun calculateOffset(
 anchor: Anchor,
 width: Int,
 height: Int
 ): Int {
 return -width + anchor.width
 }
 }

 data object ToRight : Horizontal() {
 override fun calculateOffset(
 anchor: Anchor,
 width: Int,
 height: Int
 ): Int {
 return anchor.width
 }
 }

 data object ToStart : Horizontal() {
 override fun calculateOffset(
 anchor: Anchor,
 width: Int,
 height: Int
 ): Int {
 val rtl = anchor.view.layoutDirection == View.LAYOUT_DIRECTION_RTL
 return if (rtl) anchor.width else -width
 }
 }

 data object AlignStart : Horizontal() {
 override fun calculateOffset(
 anchor: Anchor,
 width: Int,
 height: Int
 ): Int {
 val rtl = anchor.view.layoutDirection == View.LAYOUT_DIRECTION_RTL
 return if (rtl) (-width + anchor.width) else 0
 }
 }

 data object AlignEnd : Horizontal() {
 override fun calculateOffset(
 anchor: Anchor,
 width: Int,
 height: Int
 ): Int {
 val rtl = anchor.view.layoutDirection == View.LAYOUT_DIRECTION_RTL
 return if (rtl) 0 else (-width + anchor.width)
 }
 }

 data object ToEnd : Horizontal() {
 override fun calculateOffset(
 anchor: Anchor,
 width: Int,
 height: Int
 ): Int {
 val rtl = anchor.view.layoutDirection == View.LAYOUT_DIRECTION_RTL
 return if (rtl) -width else anchor.width
 }
 }

 data object AlignEndOrStart : Horizontal() {
 override fun calculateOffset(
 anchor: Anchor,
 width: Int,
 height: Int
 ): Int {
 val rtl = anchor.view.layoutDirection == View.LAYOUT_DIRECTION_RTL
 val remaining = anchor.getRemaining()
 val startEdge = if (rtl) {
 remaining.right + anchor.width
 } else {
 remaining.left + anchor.width
 }
 val endEdge = if (rtl) {
 remaining.left + anchor.width
 } else {
 remaining.right + anchor.width
 }

 return if (startEdge > width) {
 AlignEnd.calculateOffset(anchor, width, height)
 } else if (endEdge > width) {
 AlignStart.calculateOffset(anchor, width, height)
 } else {
 if (startEdge >= endEdge) {
 AlignEnd.calculateOffset(anchor, width, height)
 } else {
 AlignStart.calculateOffset(anchor, width, height)
 }
 }
 }
 }
 }

 override fun calculateOffset(
 offset: Point,
 anchor: Anchor,
 width: Int,
 height: Int
 ) {
 offset.x += horizontal.calculateOffset(anchor, width, height)
 offset.y += vertical.calculateOffset(anchor, width, height)
 }
 }

 object BellowAndAlignLeft : Alignment

 /**
 * 下方 & 右对齐
 * ```
 * +--------+
 * | anchor |
 * +--------+
 * +--------------+
 * | |
 * | popup menu |
 * | |
 * +--------------+
 * ```
 */
 object BellowAndAlignRight : Alignment {
 override fun calculateOffset(offset: Point, anchor: Anchor, width: Int, height: Int) {
 offset.x -= (width - anchor.width)
 }
 }

 /**
 * 下方 & 左侧
 * ```
 * +--------+
 * | anchor |
 * +--------+
 * +--------------+
 * | |
 * | popup menu |
 * | |
 * +--------------+
 * ```x
 */
 object BellowAndToLeft : Alignment {
 override fun calculateOffset(offset: Point, anchor: Anchor, width: Int, height: Int) {
 offset.x -= width
 }
 }

 /**
 * 下方 & 起始边对齐
 *
 * - 在 LTR 布局中，同 [BellowAndAlignLeft]
 * - 在 RTL 布局中，同 [BellowAndAlignRight]
 */
 object BellowAndAlignStart : Alignment {
 override fun calculateOffset(offset: Point, anchor: Anchor, width: Int, height: Int) {
 val ld = anchor.view.layoutDirection
 if (ld == View.LAYOUT_DIRECTION_LTR) {
 BellowAndAlignLeft.calculateOffset(offset, anchor, width, height)
 } else if (ld == View.LAYOUT_DIRECTION_RTL) {
 BellowAndAlignRight.calculateOffset(offset, anchor, width, height)
 }
 }
 }

 /**
 * 下方 & 结束边对齐
 *
 * - 在 LTR 布局中，同 [BellowAndAlignRight]
 * - 在 RTL 布局中，同 [BellowAndAlignLeft]
 */
 object BellowAndAlignEnd : Alignment {
 override fun calculateOffset(offset: Point, anchor: Anchor, width: Int, height: Int) {
 val ld = anchor.view.layoutDirection
 if (ld == View.LAYOUT_DIRECTION_LTR) {
 BellowAndAlignRight.calculateOffset(offset, anchor, width, height)
 } else if (ld == View.LAYOUT_DIRECTION_RTL) {
 BellowAndAlignLeft.calculateOffset(offset, anchor, width, height)
 }
 }
 }

/**
 * 下方 & 右侧
 * ```
 * +--------+
 * | anchor |
 * +--------+
 * +--------------+
 * | |
 * | popup menu |
 * | |
 * +--------------+
 * ```
 */
 object BellowAndToRight : Alignment {
 override fun calculateOffset(offset: Point, anchor: Anchor, width: Int, height: Int) {
 offset.x += anchor.width
 }
 }

 /**
 * 下方 & 居中
 * ```
 * +--------+
 * | anchor |
 * +--------+
 * +--------------+
 * | |
 * | popup menu |
 * | |
 * +--------------+
 * ```
 */
 object BellowAndCenter : Alignment {
 override fun calculateOffset(offset: Point, anchor: Anchor, width: Int, height: Int) {
 offset.x -= width / 2
 offset.x += anchor.width / 2
 }
 }

 /**
 * 下方 & 起始边外侧
 *
 * - 在 LTR 布局中，同 [BellowAndToLeft]
 * - 在 RTL 布局中，同 [BellowAndToRight]
 */
 object BellowAndToStart : Alignment {
 override fun calculateOffset(offset: Point, anchor: Anchor, width: Int, height: Int) {
 val ld = anchor.view.layoutDirection
 if (ld == View.LAYOUT_DIRECTION_LTR) {
 BellowAndToLeft.calculateOffset(offset, anchor, width, height)
 } else if (ld == View.LAYOUT_DIRECTION_RTL) {
 BellowAndToRight.calculateOffset(offset, anchor, width, height)
 }
 }
 }

 /**
 * 下方 & 结束边外侧
 *
 * - 在 LTR 布局中，同 [BellowAndToRight]
 * - 在 RTL 布局中，同 [BellowAndToLeft]
 */
 object BellowAndToEnd : Alignment {
 override fun calculateOffset(offset: Point, anchor: Anchor, width: Int, height: Int) {
 val ld = anchor.view.layoutDirection
 if (ld == View.LAYOUT_DIRECTION_LTR) {
 BellowAndToRight.calculateOffset(offset, anchor, width, height)
 } else if (ld == View.LAYOUT_DIRECTION_RTL) {
 BellowAndToLeft.calculateOffset(offset, anchor, width, height)
 }
 }
 }

 /**
 * 上方 & 左对齐
 * ```
 * +--------------+
 * | |
 * | popup menu |
 * | |
 * +--------------+
 * +--------+
 * | anchor |
 * +--------+
 * ```
 */
 object AboveAndAlignLeft : Alignment {
 override fun calculateOffset(offset: Point, anchor: Anchor, width: Int, height: Int) {
 offset.y -= (anchor.height + height)
 }
 }

 /**
 * 上方 & 右对齐
 * ```
 * +--------------+
 * | |
 * | popup menu |
 * | |
 * +--------------+
 * +--------+
 * | anchor |
 * +--------+
 * ```
 */
 object AboveAndAlignRight : Alignment {
 override fun calculateOffset(offset: Point, anchor: Anchor, width: Int, height: Int) {
 offset.y -= (anchor.height + height)
 offset.x -= (width - anchor.width)
 }
 }

 /**
 * 上方 & 左侧
 * ```
 * +--------------+
 * | |
 * | popup menu |
 * | |
 * +--------------+
 * +--------+
 * | anchor |
 * +--------+
 * ```x
 */
 object AboveAndToLeft : Alignment {
 override fun calculateOffset(offset: Point, anchor: Anchor, width: Int, height: Int) {
 offset.y -= (anchor.height + height)
 offset.x -= width
 }
 }

 /**
 * 上方 & 起始边对齐
 *
 * - 在 LTR 布局中，同 [AboveAndAlignLeft]
 * - 在 RTL 布局中，同 [AboveAndAlignRight]
 */
 object AboveAndAlignStart : Alignment {
 override fun calculateOffset(offset: Point, anchor: Anchor, width: Int, height: Int) {
 val ld = anchor.view.layoutDirection
 if (ld == View.LAYOUT_DIRECTION_LTR) {
 AboveAndAlignLeft.calculateOffset(offset, anchor, width, height)
 } else if (ld == View.LAYOUT_DIRECTION_RTL) {
 AboveAndAlignRight.calculateOffset(offset, anchor, width, height)
 }
 }
 }

 /**
 * 上方 & 结束边对齐
 *
 * - 在 LTR 布局中，同 [AboveAndAlignRight]
 * - 在 RTL 布局中，同 [AboveAndAlignLeft]
 */
 object AboveAndAlignEnd : Alignment {
 override fun calculateOffset(offset: Point, anchor: Anchor, width: Int, height: Int) {
 val ld = anchor.view.layoutDirection
 if (ld == View.LAYOUT_DIRECTION_LTR) {
 AboveAndAlignRight.calculateOffset(offset, anchor, width, height)
 } else if (ld == View.LAYOUT_DIRECTION_RTL) {
 AboveAndAlignLeft.calculateOffset(offset, anchor, width, height)
 }
 }
 }

 /**
 * 上方 & 右侧
 * ```
 * +--------------+
 * | |
 * | popup menu |
 * | |
 * +--------------+
 * +--------+
 * | anchor |
 * +--------+
 * ```
 */
 object AboveAndToRight : Alignment {
 override fun calculateOffset(offset: Point, anchor: Anchor, width: Int, height: Int) {
 offset.y -= (anchor.height + height)
 offset.x += anchor.width
 }
 }

 /**
 * 上方 & 居中
 * ```
 * +--------------+
 * | |
 * | popup menu |
 * | |
 * +--------------+
 * +--------+
 * | anchor |
 * +--------+
 * ```
 */
 object AboveAndCenter : Alignment {
 override fun calculateOffset(offset: Point, anchor: Anchor, width: Int, height: Int) {
 offset.y -= (anchor.height + height)
 offset.x -= width / 2
 offset.x += anchor.width / 2
 }
 }

 /**
 * 上方 & 起始边外侧
 *
 * - 在 LTR 布局中，同 [AboveAndToLeft]
 * - 在 RTL 布局中，同 [AboveAndToRight]
 */
 object AboveAndToStart : Alignment {
 override fun calculateOffset(offset: Point, anchor: Anchor, width: Int, height: Int) {
 val ld = anchor.view.layoutDirection
 if (ld == View.LAYOUT_DIRECTION_LTR) {
 AboveAndToLeft.calculateOffset(offset, anchor, width, height)
 } else if (ld == View.LAYOUT_DIRECTION_RTL) {
 AboveAndToRight.calculateOffset(offset, anchor, width, height)
 }
 }
 }

 /**
 * 上方 & 结束边外侧
 *
 * - 在 LTR 布局中，同 [AboveAndToRight]
 * - 在 RTL 布局中，同 [AboveAndToLeft]
 */
 object AboveAndToEnd : Alignment {
 override fun calculateOffset(offset: Point, anchor: Anchor, width: Int, height: Int) {
 val ld = anchor.view.layoutDirection
 if (ld == View.LAYOUT_DIRECTION_LTR) {
 AboveAndToRight.calculateOffset(offset, anchor, width, height)
 } else if (ld == View.LAYOUT_DIRECTION_RTL) {
 AboveAndToLeft.calculateOffset(offset, anchor, width, height)
 }
 }
 }
 }

}