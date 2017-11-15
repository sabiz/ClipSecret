package jp.sabiz.android.clipsecret.util

import android.os.Handler
import android.os.HandlerThread

/**
 * @auther sabi
 * Created on 2017/11/14.
 */
class Async{
    private val handlerThread: HandlerThread = HandlerThread("ASYNC")
    private val asyncHandler: Handler by lazy {
        handlerThread.start()
        Handler(handlerThread.looper)
    }

    fun post(runnable: ()->Unit) {
        asyncHandler.post(runnable)
    }

    fun quit(){
        asyncHandler.post {
            handlerThread.quitSafely()
        }
    }
}