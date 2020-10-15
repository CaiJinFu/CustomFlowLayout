package com.example.customflowlayout

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import kotlin.math.max

/**
 * @name CustomFlowLayout
 * @class name：com.example.customflowlayout
 * @class describe
 * @author 猿小蔡
 * @createTime 2020/10/15 13:59
 * @change
 * @changTime
 */
class FlowLayout : ViewGroup {
    // 把每一行的行高存起来
    var listLineHeight = mutableListOf<Int>()

    // 所有的子控件的容器
    var list = mutableListOf<MutableList<View>>()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    // 防止测量多次
    private var isMeasure = false

    override fun generateLayoutParams(attributeSet: AttributeSet?): LayoutParams? {
        return MarginLayoutParams(context, attributeSet)
    }

    private val TAG = javaClass.simpleName

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 获取到父容器 给我们的参考值
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        // 获取到自己的测量模式
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        //保存当前控件的里面子控件的总宽高
        var childCountWidth = 0
        var childCountHeight = 0
        if (!isMeasure) {
            isMeasure = true
        } else {
            // 当前控件中子控件一行使用的宽度值
            var lineCountWidth: Int = paddingLeft + paddingRight
            // 保存一行中最高的子控件的高度
            var lineMaxHeight: Int = 0
            // 存储每个子控件的宽高
            var iChildWidth = 0
            var iChileHeight = 0
            // 创建一行的容器
            var viewList = mutableListOf<View>()
            for (index in 0 until childCount) {
                val childAt = getChildAt(index)
                // 先测量子控件
                measureChild(childAt, widthMeasureSpec, heightMeasureSpec)
                val layoutParams = childAt.layoutParams as MarginLayoutParams
                // 计算当前子控件的实际宽高
                iChildWidth =
                    childAt.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin
                iChileHeight =
                    childAt.measuredHeight + layoutParams.bottomMargin + layoutParams.topMargin

                // 当这个字控件的宽度累加之后是否大于
                if (iChildWidth + lineCountWidth > widthSize) { // 需要换行
                    // 每次进入到这里的时候  只是 保存了上一行的信息  并没有保存当前行的信息

                    // 如果需要换行 我们就要保存一行的信息
                    // 每次换行的时候都要比较当前行和前面行谁的宽度最大
                    childCountWidth = max(lineCountWidth, childCountWidth)
                    // 如果需要换行  要累加行高
                    childCountHeight += lineMaxHeight
                    // 把行高记录到集合中
                    listLineHeight.add(lineMaxHeight)
                    // 把这一行的数据放进总容器
                    list.add(viewList)
                    // 把一行的容器 重新创建一个
                    viewList = mutableListOf()
                    // 将每一行的总长度  重新初始化
                    lineCountWidth = iChildWidth + paddingLeft + paddingRight
                    // 将高度也重新初始化
                    lineMaxHeight = iChileHeight
                    viewList.add(childAt)
                } else { // 不需要换行
                    lineCountWidth += iChildWidth
                    // 对比每个子控件到底谁的高度最高
                    lineMaxHeight = max(lineMaxHeight, iChileHeight)
                    // 如果当前不需要换行  就将当前控件保存在一行中
                    viewList.add(childAt)
                }
                // 这样做的原因是  之前的ifelse中 不会把最后一行的高度加进listLineHeight
                // 最后一行要特殊对待 不管最后一个item是不是最后一行的第一个item

                // 这样做的原因是  之前的ifelse中 不会把最后一行的高度加进listLineHeight
                // 最后一行要特殊对待 不管最后一个item是不是最后一行的第一个item
                if (index == childCount - 1) {
                    // 保存当前行信息
                    childCountWidth = max(lineCountWidth, childCountWidth)
                    val i = lineMaxHeight + paddingTop + paddingBottom
                    childCountHeight += i
                    listLineHeight.add(lineMaxHeight)
                    list.add(viewList)
                }
            }
        }
        // 设置控件最终的大小
        val measureWidth =
            if (widthMode == MeasureSpec.EXACTLY) widthSize else childCountWidth
        val measureHeight =
            if (heightMode == MeasureSpec.EXACTLY) heightSize else childCountHeight
        setMeasuredDimension(measureWidth, measureHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // 摆放子控件的位置
        var left: Int
        var top: Int
        var bottom: Int
        var right: Int
        // 保存上一个控件的边距
        var countLeft = 0
        // 保存上一行的高度的边距
        var countTop = paddingTop
        // 遍历每所有行
        for (views in list) {
            // 遍历每一行的控件
            for ((index, view) in views.withIndex()) {
                // 获取到控件的属性对象
                val layoutParams = view.layoutParams as MarginLayoutParams
                if (views.size == 1) {
                    left = countLeft + layoutParams.leftMargin + paddingLeft
                } else {
                    if (index == 0) {
                        left = countLeft + layoutParams.leftMargin + paddingLeft
                    } else {
                        left = countLeft + layoutParams.leftMargin
                    }
                }
//                left = countLeft + layoutParams.leftMargin
                top = countTop + layoutParams.topMargin
                right = left + view.measuredWidth
                bottom = top + view.measuredHeight
                view.layout(left, top, right, bottom)
                if (views.size == 1) {
                    countLeft += paddingLeft + view.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin
                } else {
                    if (index == 0) {
                        countLeft += paddingLeft + view.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin
                    } else {
                        countLeft += view.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin
                    }
                }
//                countLeft += view.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin
            }

            // 获取到当前这一行的position
            val i = list.indexOf(views)
            countLeft = 0
            countTop += listLineHeight[i]
        }
        list.clear()
        listLineHeight.clear()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}