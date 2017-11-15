package jp.sabiz.android.clipsecret.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

/**
 * @auther sabi
 * Created on 2017/11/14.
 */
fun toast(context: Context,text:CharSequence,length:Int){
    Handler(Looper.getMainLooper()).post { Toast.makeText(context,text,length).show() }
}

fun toastShort(context: Context,text:CharSequence){
    toast(context,text,Toast.LENGTH_SHORT)
}

fun toastLong(context: Context,text:CharSequence){
    toast(context,text,Toast.LENGTH_LONG)
}
