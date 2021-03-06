package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.ViewGroup
import androidx.core.text.isDigitsOnly
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.tencent.iot.explorer.link.DataHolder
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.customview.progress.SeekProgress
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.control_simple_big_int.view.*

/**
 * 控制面板数字类型：大按钮
 */
class ControlSimpleNumberBigHolder : CRecyclerView.CViewHolder<DevicePropertyEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_simple_big_int_name.text = name
            itemView.tv_simple_big_int_value.text = getValueText()
            var tmpValue = getValue()
            if (tmpValue.toDoubleOrNull() == null) {
                tmpValue = "0"
            }
            val progress = tmpValue.toDouble().toInt()
            L.e("progress=$progress,min=${numberEntity?.min},max=${numberEntity?.max}")
            itemView.sp_simple_big_int.setProgress(progress)
            itemView.sp_simple_big_int.setRange(
                numberEntity?.min?.toDouble()?.toInt() ?: 0,
                numberEntity?.max?.toDouble()?.toInt() ?: 100
            )
            val step = try {
                numberEntity?.step?.toInt() ?: 1
            } catch (e: NumberFormatException) {
                numberEntity?.step?.toDouble()?.toInt() ?: 1
            }
            itemView.sp_simple_big_int.setStepValue(step)

            if (DataHolder.instance.get<DeviceEntity>("device")?.online ?: 0 == 1) {
                itemView.sp_simple_big_int.onProgressListener =
                    object : SeekProgress.OnProgressListener {
                        override fun onProgress(progress: Int, step: Int, keyUp: Boolean) {
                            itemView.tv_simple_big_int_value.text =
                                "$progress${numberEntity?.getNumUnit() ?: ""}"
                            if (keyUp) {
                                recyclerItemView?.doAction(
                                    this@ControlSimpleNumberBigHolder,
                                    itemView.sp_simple_big_int, position
                                )
                            }
                        }
                    }
            }
        }
    }

    /*private fun showNum(value: Int, unit: String) {
        itemView.tv_simple_big_int_value.text = if (unit == "oC") {
            "$value℃"
        } else {
            "$value$unit"
        }
    }
*/
}