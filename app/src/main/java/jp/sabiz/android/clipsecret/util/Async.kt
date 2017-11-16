package jp.sabiz.android.clipsecret.util

import android.os.Handler
import android.os.HandlerThread
import java.util.concurrent.TimeUnit

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

    fun postDelayed(runnable: ()->Unit,sec:Long) {
        asyncHandler.postDelayed(runnable,TimeUnit.SECONDS.toMillis(sec))
    }

    fun quit(){
        asyncHandler.post {
            handlerThread.quitSafely()
        }
    }
}