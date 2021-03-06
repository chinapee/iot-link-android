package com.tencent.iot.explorer.link.core.demo.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.holder.BaseHolder
import com.tencent.iot.explorer.link.core.demo.holder.VideoHolder
import com.tencent.iot.video.link.entity.PlaybackVideoEntity

class VideoListAdapter : BaseAdapter {
    constructor(context: Context, list: List<PlaybackVideoEntity>) : super(context, list)
    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
        return VideoHolder(mContext, parent, R.layout.item_playback_video)
    }
}