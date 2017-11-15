package jp.sabiz.android.clipsecret.data

import android.content.Context
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import jp.sabiz.android.clipsecret.util.*


/**
 * @auther sabi
 * Created on 2017/11/12.
 */
class SecretStore(private val context: Context) {

    companion object {
        private const val PREF_KEY = "SECRETS"
        private const val KEY_ALIAS = "CLIP_SECRET_KEY"
    }

    private val pref = context.getSharedPreferences("SECRET_STORE",Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()
    private val keyStore by lazy {
        loadKeyStore()
    }

    fun add(url:String, name:String, password:String):Boolean {
        addKeyIfNeed(keyStore,KEY_ALIAS)
        val data = load()
        val secret = Secret(url, name, encrypt(keyStore, KEY_ALIAS, password))
        if(data.secrets.contains(secret)){
            toastLong(context,"Already exist...")
            return false
        }
        data.secrets.add(secret)
        save(data)
        return true
    }

    fun delete(secret:Secret) {
        val data = load()
        data.secrets.remove(secret)
        save(data)
    }

    fun findNamesByUrl(url:String):MutableList<String> {
        val secrets = load()
        val result = mutableListOf<String>()
        secrets.secrets.forEach {
            if(url == it.url){
                result.add(it.name)
            }
        }
        return result
    }

    fun findSecretByUrlAndName(url:String, name:String):Secret {
        val secrets = load()
        secrets.secrets.forEach {
            if(url == it.url &&  name == it.name){
                return Secret(it.url, it.name, decrypt(keyStore, KEY_ALIAS, it.password))
            }
        }
        return Secret(url,name,"")
    }

    private fun save(data: Secrets) {
        val jsonString =  moshi.adapter(Secrets::class.java).toJson(data)
        pref.edit().putString(PREF_KEY, jsonString).apply()
    }

    private fun load():Secrets {
        val jsonString = pref.getString(PREF_KEY,"")
        if(jsonString.isEmpty()){
            return Secrets(mutableListOf())
        }
        return moshi.adapter(Secrets::class.java).fromJson(jsonString)!!
    }
}