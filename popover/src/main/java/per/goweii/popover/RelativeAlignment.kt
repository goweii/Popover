package per.goweii.popover

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.View
import per.goweii.popover.Popover.Alignment
import per.goweii.popover.Popover.Anchor

/**
 * 按水平和垂直方向相对位置控制对齐方式
 */
@SuppressLint("CheckResult")
open class RelativeAlignment(
    private val vertical: Vertical = Vertical.BellowOrAbove(),
    private val horizontal: Horizontal = Horizontal.AlignStartOrEnd(),
) : Alignment {
    sealed class Vertical : Alignment {
        data class Above(
            private val spacingToAnchor: Int = 0,
            private val laidOutInWindow: Boolean = true
        ) : Vertical() {
            override fun compute(anchor: Anchor, target: Rect) {
                target.offset(0, -(target.height() + anchor.height))
                target.offset(0, -spacingToAnchor)
                if (laidOutInWindow) {
                    if (target.bottom > anchor.windowRect.bottom) {
                        target.offset(0, anchor.windowRect.bottom - target.bottom)
                    }
                    target.intersect(anchor.windowRect)
                }
            }
        }

        data class AlignTop(
            private val laidOutInWindow: Boolean = true
        ) : Vertical() {
            override fun compute(anchor: Anchor, target: Rect) {
                target.offset(0, -anchor.height)
                if (laidOutInWindow) {
                    if (target.top < anchor.windowRect.top) {
                        target.offset(0, anchor.windowRect.top - target.top)
                    }
                    target.intersect(anchor.windowRect)
                }
            }
        }

        data class Center(
            private val laidOutInWindow: Boolean = true
        ) : Vertical() {
            override fun compute(anchor: Anchor, target: Rect) {
                target.offset(0, -(target.height() / 2 + anchor.height / 2))
                if (laidOutInWindow) {
                    target.intersect(anchor.windowRect)
                }
            }
        }

        data class AlignBottom(
            private val laidOutInWindow: Boolean = true
        ) : Vertical() {
            override fun compute(anchor: Anchor, target: Rect) {
                target.offset(0, -target.height())
                if (laidOutInWindow) {
                    if (target.bottom > anchor.windowRect.bottom) {
                        target.offset(0, anchor.windowRect.bottom - target.bottom)
                    }
                    target.intersect(anchor.windowRect)
                }
            }
        }

        data class Bellow(
            private val spacingToAnchor: Int = 0,
            private val laidOutInWindow: Boolean = true
        ) : Vertical() {
            override fun compute(anchor: Anchor, target: Rect) {
                target.offset(0, spacingToAnchor)
                if (laidOutInWindow) {
                    if (target.top < anchor.windowRect.top) {
                        target.offset(0, anchor.windowRect.top - target.top)
                    }
                    target.intersect(anchor.windowRect)
                }
            }
        }

        data class BellowOrAbove(
            private val spacingToAnchor: Int = 0,
            private val laidOutInWindow: Boolean = true
        ) : Vertical() {
            private val bellow = Bellow(
                spacingToAnchor = spacingToAnchor,
                laidOutInWindow = laidOutInWindow,
            )
            private val above = Above(
                spacingToAnchor = spacingToAnchor,
                laidOutInWindow = laidOutInWindow,
            )

            override fun compute(anchor: Anchor, target: Rect) {
                val remaining = anchor.remaining
                if (remaining.bottom >= target.height()) {
                    bellow.compute(anchor, target)
                } else {
                    if (remaining.top >= target.height()) {
                        above.compute(anchor, target)
                    } else {
                        if (remaining.bottom >= remaining.top) {
                            bellow.compute(anchor, target)
                        } else {
                            above.compute(anchor, target)
                        }
                    }
                }
            }
        }
    }

    sealed class Horizontal : Alignment {
        data class ToLeft(
            private val spacingToAnchor: Int = 0,
            private val laidOutInWindow: Boolean = true
        ) : Horizontal() {
            override fun compute(anchor: Anchor, target: Rect) {
                target.offset(-target.width(), 0)
                target.offset(-spacingToAnchor, 0)
                if (laidOutInWindow) {
                    if (target.right > anchor.windowRect.right) {
                        target.offset(anchor.windowRect.right - target.right, 0)
                    }
                    target.intersect(anchor.windowRect)
                }
            }
        }

        data class AlignLeft(
            private val laidOutInWindow: Boolean = true
        ) : Horizontal() {
            override fun compute(anchor: Anchor, target: Rect) {
                if (laidOutInWindow) {
                    if (target.left < anchor.windowRect.left) {
                        target.offset(anchor.windowRect.left - target.left, 0)
                    }
                    target.intersect(anchor.windowRect)
                }
            }
        }

        data class Center(
            private val laidOutInWindow: Boolean = true
        ) : Horizontal() {
            override fun compute(anchor: Anchor, target: Rect) {
                target.offset(-(target.width() / 2 - anchor.width / 2), 0)
                if (laidOutInWindow) {
                    target.intersect(anchor.windowRect)
                }
            }
        }

        data class AlignRight(
            private val laidOutInWindow: Boolean = true
        ) : Horizontal() {
            override fun compute(anchor: Anchor, target: Rect) {
                target.offset(-(target.width() - anchor.width), 0)
                if (laidOutInWindow) {
                    if (target.right > anchor.windowRect.right) {
                        target.offset(anchor.windowRect.right - target.right, 0)
                    }
                    target.intersect(anchor.windowRect)
                }
            }
        }

        data class ToRight(
            private val spacingToAnchor: Int = 0,
            private val laidOutInWindow: Boolean = true
        ) : Horizontal() {
            override fun compute(anchor: Anchor, target: Rect) {
                target.offset(anchor.width, 0)
                target.offset(spacingToAnchor, 0)
                if (laidOutInWindow) {
                    if (target.left < anchor.windowRect.left) {
                        target.offset(anchor.windowRect.left - target.left, 0)
                    }
                    target.intersect(anchor.windowRect)
                }
            }
        }

        data class ToStart(
            private val spacingToAnchor: Int = 0,
            private val laidOutInWindow: Boolean = true
        ) : Horizontal() {
            private val toLeft = ToLeft(
                spacingToAnchor = spacingToAnchor,
                laidOutInWindow = laidOutInWindow,
            )
            private val toRight = ToRight(
                spacingToAnchor = spacingToAnchor,
                laidOutInWindow = laidOutInWindow,
            )

            override fun compute(anchor: Anchor, target: Rect) {
                val rtl = anchor.view.layoutDirection == View.LAYOUT_DIRECTION_RTL
                if (rtl) {
                    toRight.compute(anchor, target)
                } else {
                    toLeft.compute(anchor, target)
                }
            }
        }

        data class AlignStart(
            private val spacingToAnchor: Int = 0,
            private val laidOutInWindow: Boolean = true
        ) : Horizontal() {
            private val alignLeft = AlignLeft(
                laidOutInWindow = laidOutInWindow,
            )
            private val alignRight = AlignRight(
                laidOutInWindow = laidOutInWindow,
            )

            override fun compute(anchor: Anchor, target: Rect) {
                val rtl = anchor.view.layoutDirection == View.LAYOUT_DIRECTION_RTL
                if (rtl) {
                    alignRight.compute(anchor, target)
                } else {
                    alignLeft.compute(anchor, target)
                }
            }
        }

        data class AlignEnd(
            private val laidOutInWindow: Boolean = true
        ) : Horizontal() {
            private val alignLeft = AlignLeft(
                laidOutInWindow = laidOutInWindow,
            )
            private val alignRight = AlignRight(
                laidOutInWindow = laidOutInWindow,
            )

            override fun compute(anchor: Anchor, target: Rect) {
                val rtl = anchor.view.layoutDirection == View.LAYOUT_DIRECTION_RTL
                if (!rtl) {
                    alignRight.compute(anchor, target)
                } else {
                    alignLeft.compute(anchor, target)
                }
            }
        }

        data class ToEnd(
            private val spacingToAnchor: Int = 0,
            private val laidOutInWindow: Boolean = true
        ) : Horizontal() {
            private val toLeft = ToLeft(
                spacingToAnchor = spacingToAnchor,
                laidOutInWindow = laidOutInWindow,
            )
            private val toRight = ToRight(
                spacingToAnchor = spacingToAnchor,
                laidOutInWindow = laidOutInWindow,
            )

            override fun compute(anchor: Anchor, target: Rect) {
                val rtl = anchor.view.layoutDirection == View.LAYOUT_DIRECTION_RTL
                if (rtl) {
                    toLeft.compute(anchor, target)
                } else {
                    toRight.compute(anchor, target)
                }
            }
        }

        data class AlignStartOrEnd(
            private val laidOutInWindow: Boolean = true
        ) : Horizontal() {
            private val alignEnd = AlignEnd(
                laidOutInWindow = laidOutInWindow,
            )
            private val alignStart = AlignStart(
                laidOutInWindow = laidOutInWindow,
            )

            override fun compute(anchor: Anchor, target: Rect) {
                val rtl = anchor.view.layoutDirection == View.LAYOUT_DIRECTION_RTL
                val remaining = anchor.remaining
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

                if (rtl) {
                    if (startEdge >= target.width()) {
                        alignEnd.compute(anchor, target)
                    } else if (endEdge >= target.width()) {
                        alignStart.compute(anchor, target)
                    } else {
                        if (startEdge >= endEdge) {
                            alignEnd.compute(anchor, target)
                        } else {
                            alignStart.compute(anchor, target)
                        }
                    }
                } else {
                    if (endEdge >= target.width()) {
                        alignStart.compute(anchor, target)
                    } else if (startEdge >= target.width()) {
                        alignEnd.compute(anchor, target)
                    } else {
                        if (endEdge >= startEdge) {
                            alignStart.compute(anchor, target)
                        } else {
                            alignEnd.compute(anchor, target)
                        }
                    }
                }
            }
        }
    }

    override fun compute(anchor: Anchor, target: Rect) {
        val l = target.left
        val r = target.right
        val t = target.top
        val b = target.bottom

        horizontal.compute(anchor, target)
        val nl = target.left
        val nr = target.right

        target.set(l, t, r, b)

        vertical.compute(anchor, target)
        val nt = target.top
        val nb = target.bottom

        target.set(nl, nt, nr, nb)
        target.intersect(anchor.windowRect)
    }
}