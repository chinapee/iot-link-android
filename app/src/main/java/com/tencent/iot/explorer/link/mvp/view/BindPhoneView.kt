package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView

interface BindPhoneView : ParentView {
    fun showCountryCode(code: String, name: String)
}