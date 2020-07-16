package com.example.flutterrealtimevoicedemo

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*

class MainActivity : FlutterActivity() {

    companion object {
        private const val channel = "flutter_real_time_voice_demo"
        private const val TAG = "MainActivity"
        private const val audioSource = MediaRecorder.AudioSource.MIC
        private const val sampleRateInHz = 8000
        private const val channelConfig = AudioFormat.CHANNEL_IN_MONO
        private const val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    }

    private var bufferSizeInByte = 0
    private var audioRecord: AudioRecord ?= null
    private var isRecord: Boolean = false

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channel).setMethodCallHandler {
            call, result ->
            if (call.method == "release") {
                audioRecord?.release()
                result.success(true)
            } else {
                result.notImplemented()
            }
        }

        EventChannel(flutterEngine.dartExecutor.binaryMessenger, channel)
                .setStreamHandler(
                        object : EventChannel.StreamHandler {
                            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                                audioRecord ?: initAudioRecord()

                                Log.d(TAG, "onListen: 开始录音")
                                audioRecord?.startRecording()
                                isRecord = true
                                val audioData = ByteArray(bufferSizeInByte)
                                var length: Int
                                GlobalScope.launch {
                                    while (isRecord) {
                                        length = audioRecord!!.read(audioData, 0, bufferSizeInByte)
                                        if (AudioRecord.ERROR_INVALID_OPERATION != length) {
                                            runOnUiThread {
                                                events?.success(audioData)
                                            }
                                        }
                                    }
                                }
                            }

                            override fun onCancel(arguments: Any?) {
                                Log.d(TAG, "onCancel: 取消了")
                                isRecord = false
                                audioRecord?.stop()
                            }
                        }
                )
    }

    private fun initAudioRecord() {
        bufferSizeInByte = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
        audioRecord = AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInByte)
    }
}
