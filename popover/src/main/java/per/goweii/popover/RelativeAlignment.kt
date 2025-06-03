package per.goweii.popover

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.View

/**
 * 按水平和垂直方向相对位置控制对齐方式
 */
@SuppressLint("CheckResult")
open class RelativeAlignment(
    private val vertical: Vertical = Vertical.BellowOrAbove(),
    private val horizontal: Horizontal = Horizontal.AlignStartOrEnd(),
) : Popover.Alignment {
    sealed class Vertical : Popover.Alignment {
        data class Above(
            private val spacingToAnchor: Int = 0,
            private val laidOutInWindow: Boolean = true
        ) : Vertical() {
            override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
                target.targetRect.offset(0, -(target.targetRect.height() + anchor.height))
                target.targetRect.offset(0, -spacingToAnchor)
                if (laidOutInWindow) {
                    if (target.targetRect.bottom > anchor.windowRect.bottom) {
                        target.targetRect.offset(
                            0,
                            anchor.windowRect.bottom - target.targetRect.bottom
                        )
                    }
                    target.targetRect.intersect(anchor.windowRect)
                }
            }
        }

        data class AlignTop(
            private val laidOutInWindow: Boolean = true
        ) : Vertical() {
            override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
                target.targetRect.offset(0, -anchor.height)
                if (laidOutInWindow) {
                    if (target.targetRect.top < anchor.windowRect.top) {
                        target.targetRect.offset(0, anchor.windowRect.top - target.targetRect.top)
                    }
                    target.targetRect.intersect(anchor.windowRect)
                }
            }
        }

        data class Center(
            private val laidOutInWindow: Boolean = true
        ) : Vertical() {
            override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
                target.targetRect.offset(0, -(target.targetRect.height() / 2 + anchor.height / 2))
                if (laidOutInWindow) {
                    target.targetRect.intersect(anchor.windowRect)
                }
            }
        }

        data class AlignBottom(
            private val laidOutInWindow: Boolean = true
        ) : Vertical() {
            override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
                target.targetRect.offset(0, -target.targetRect.height())
                if (laidOutInWindow) {
                    if (target.targetRect.bottom > anchor.windowRect.bottom) {
                        target.targetRect.offset(
                            0,
                            anchor.windowRect.bottom - target.targetRect.bottom
                        )
                    }
                    target.targetRect.intersect(anchor.windowRect)
                }
            }
        }

        data class Bellow(
            private val spacingToAnchor: Int = 0,
            private val laidOutInWindow: Boolean = true
        ) : Vertical() {
            override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
                target.targetRect.offset(0, spacingToAnchor)
                if (laidOutInWindow) {
                    if (target.targetRect.top < anchor.windowRect.top) {
                        target.targetRect.offset(0, anchor.windowRect.top - target.targetRect.top)
                    }
                    target.targetRect.intersect(anchor.windowRect)
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

            override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
                val remaining = anchor.remaining
                if (remaining.bottom >= target.targetRect.height()) {
                    bellow.compute(anchor, target)
                } else {
                    if (remaining.top >= target.targetRect.height()) {
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

    sealed class Horizontal : Popover.Alignment {
        data class ToLeft(
            private val spacingToAnchor: Int = 0,
            private val laidOutInWindow: Boolean = true
        ) : Horizontal() {
            override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
                target.targetRect.offset(-target.targetRect.width(), 0)
                target.targetRect.offset(-spacingToAnchor, 0)
                if (laidOutInWindow) {
                    if (target.targetRect.right > anchor.windowRect.right) {
                        target.targetRect.offset(
                            anchor.windowRect.right - target.targetRect.right,
                            0
                        )
                    }
                    target.targetRect.intersect(anchor.windowRect)
                }
            }
        }

        data class AlignLeft(
            private val laidOutInWindow: Boolean = true
        ) : Horizontal() {
            override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
                if (laidOutInWindow) {
                    if (target.targetRect.left < anchor.windowRect.left) {
                        target.targetRect.offset(anchor.windowRect.left - target.targetRect.left, 0)
                    }
                    target.targetRect.intersect(anchor.windowRect)
                }
            }
        }

        data class Center(
            private val laidOutInWindow: Boolean = true
        ) : Horizontal() {
            override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
                target.targetRect.offset(-(target.targetRect.width() / 2 - anchor.width / 2), 0)
                if (laidOutInWindow) {
                    target.targetRect.intersect(anchor.windowRect)
                }
            }
        }

        data class AlignRight(
            private val laidOutInWindow: Boolean = true
        ) : Horizontal() {
            override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
                target.targetRect.offset(-(target.targetRect.width() - anchor.width), 0)
                if (laidOutInWindow) {
                    if (target.targetRect.right > anchor.windowRect.right) {
                        target.targetRect.offset(
                            anchor.windowRect.right - target.targetRect.right,
                            0
                        )
                    }
                    target.targetRect.intersect(anchor.windowRect)
                }
            }
        }

        data class ToRight(
            private val spacingToAnchor: Int = 0,
            private val laidOutInWindow: Boolean = true
        ) : Horizontal() {
            override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
                target.targetRect.offset(anchor.width, 0)
                target.targetRect.offset(spacingToAnchor, 0)
                if (laidOutInWindow) {
                    if (target.targetRect.left < anchor.windowRect.left) {
                        target.targetRect.offset(anchor.windowRect.left - target.targetRect.left, 0)
                    }
                    target.targetRect.intersect(anchor.windowRect)
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

            override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
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

            override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
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

            override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
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

            override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
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

            override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
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
                    if (startEdge >= target.targetRect.width()) {
                        alignEnd.compute(anchor, target)
                    } else if (endEdge >= target.targetRect.width()) {
                        alignStart.compute(anchor, target)
                    } else {
                        if (startEdge >= endEdge) {
                            alignEnd.compute(anchor, target)
                        } else {
                            alignStart.compute(anchor, target)
                        }
                    }
                } else {
                    if (endEdge >= target.targetRect.width()) {
                        alignStart.compute(anchor, target)
                    } else if (startEdge >= target.targetRect.width()) {
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

    override fun compute(anchor: Popover.Anchor, target: Popover.Target) {
        val l = target.targetRect.left
        val r = target.targetRect.right
        val t = target.targetRect.top
        val b = target.targetRect.bottom

        horizontal.compute(anchor, target)
        val nl = target.targetRect.left
        val nr = target.targetRect.right

        target.targetRect.set(l, t, r, b)

        vertical.compute(anchor, target)
        val nt = target.targetRect.top
        val nb = target.targetRect.bottom

        target.targetRect.set(nl, nt, nr, nb)
        target.targetRect.intersect(anchor.windowRect)
    }
}