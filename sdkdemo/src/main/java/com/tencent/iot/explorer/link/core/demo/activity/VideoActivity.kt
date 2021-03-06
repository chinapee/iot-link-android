package com.tencent.iot.explorer.link.core.demo.activity

import android.Manifest
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.util.LogcatHelper
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.video.link.util.audio.AudioRecordUtil
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PCallback
import kotlinx.android.synthetic.main.activity_video.*
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class VideoActivity : BaseActivity(), View.OnClickListener, SurfaceHolder.Callback, XP2PCallback {

    private lateinit var secretId: String
    private lateinit var secretKey: String
    private lateinit var productId: String
    private lateinit var deviceName: String
    private var isSpeaking: Boolean = false
    private var isPlaying: Boolean = true
    private var isP2PChannelAvailable: Boolean = false

    private lateinit var mPlayer: IjkMediaPlayer
    private lateinit var audioRecordUtil: AudioRecordUtil
    private val mHandler = Handler(Looper.getMainLooper())

    private var permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )

    override fun getContentView(): Int {
        return R.layout.activity_video
    }

    override fun initView() {
        requestPermission(permissions)
        getSwitchState(this)
        LogcatHelper.getInstance(this).start()
        val bundle = this.intent.extras
        secretId = bundle?.get(VideoConst.VIDEO_SECRET_ID) as String
        secretKey = bundle.get(VideoConst.VIDEO_SECRET_KEY) as String
        productId = bundle.get(VideoConst.VIDEO_PRODUCT_ID) as String
        deviceName = bundle.get(VideoConst.VIDEO_DEVICE_NAME) as String

        audioRecordUtil = AudioRecordUtil(this)
        video_view.holder.addCallback(this)
        mPlayer = IjkMediaPlayer()
        mPlayer.setOnPreparedListener {
            mHandler.post {
                val viewWidth = video_view.width
                val videoWidth = mPlayer.videoWidth
                val videoHeight = mPlayer.videoHeight
                val lp = video_view.layoutParams
                lp.width = viewWidth
                lp.height = (videoHeight.toFloat() * viewWidth.toFloat() / videoWidth.toFloat()).toInt()
                video_view.layoutParams = lp
            }
        }
        if (productId == " " || deviceName == " " || secretId == " " || secretKey == " ") {
            Toast.makeText(this, "设备信息有误，请确保配置文件中的设备信息填写正确", Toast.LENGTH_LONG).show()
        } else {
            val ret = openP2PChannel(productId, deviceName, secretId, secretKey)
            if (ret == 0) {
                isP2PChannelAvailable = true
                val url = XP2P.delegateHttpFlv() + "ipc.flv?action=live"
                mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0)
                mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-hevc", 1)
                mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100L)
                mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 10240L)
                mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L)
                mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0L)
                mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L)

                mPlayer.dataSource = url
                mPlayer.prepareAsync()
                mPlayer.start()
            } else {
                isP2PChannelAvailable = false
                speak.visibility = View.GONE
                watch_monitor.visibility = View.GONE
                Toast.makeText(this, "P2P通道建立失败，请检查设备是否上线", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun setListener() {
        speak.setOnClickListener(this)
        watch_monitor.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v) {
            speak -> {
                if (isSpeaking) {
                    stopSpeak()
                    isSpeaking = false
                    speak.text = "开始对讲"
                } else {
                    if (checkPermissions(permissions)) {
                        startSpeak()
                        isSpeaking = true
                        speak.text = "停止对讲"
                    } else {
                        requestPermission(permissions)
                    }
                }
            }
            watch_monitor -> {
                if (isPlaying) {
                    mPlayer.pause()
                    isPlaying = false
                    watch_monitor.text = "开始播放"
                } else {
                    mPlayer.start()
                    isPlaying = true
                    watch_monitor.text = "停止播放"
                }
            }
        }
    }

    private fun openP2PChannel(productId: String, deviceName: String, secretId: String, secretKey: String): Int {
        XP2P.setDeviceInfo(productId, deviceName)
        XP2P.setQcloudApiCred(secretId, secretKey)
        XP2P.setXp2pInfoAttributes("_sys_xp2p_info")
        XP2P.setCallback(this)
        return XP2P.startServiceWithXp2pInfo("")
    }

    private fun startSpeak() {
        XP2P.runSendService()
        Thread.sleep(500)
        audioRecordUtil.start()
    }

    private fun stopSpeak() {
        audioRecordUtil.stop()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) { }

    override fun surfaceDestroyed(holder: SurfaceHolder?) { }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        mPlayer.setDisplay(holder)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayer.release()
        XP2P.stopService()
        audioRecordUtil.release()
        LogcatHelper.getInstance(this).stop()
    }

    override fun commandRequest(msg: String?) {
    }

    override fun fail(msg: String?, errorCode: Int) {
    }

    override fun avDataRecvHandle(data: ByteArray?, len: Int) { // 音视频数据回调接口
    }

    override fun avDataCloseHandle(msg: String?, errorCode: Int) {
    }

    companion object {
        var saveData = false
        fun getSwitchState(context: Context) {
            saveData = SharePreferenceUtil.getString(context, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_SAVE_RAW_AV) == "true"
        }
        @JvmStatic fun isSaveAVData() : Boolean {
            return saveData
        }
    }
}