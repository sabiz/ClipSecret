package jp.sabiz.android.clipsecret

import android.app.AlertDialog
import android.app.KeyguardManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.wdullaer.swipeactionadapter.SwipeActionAdapter
import com.wdullaer.swipeactionadapter.SwipeDirection
import jp.sabiz.android.clipsecret.data.SecretStore
import jp.sabiz.android.clipsecret.util.Async
import jp.sabiz.android.clipsecret.util.getUrlFromIntent
import jp.sabiz.android.clipsecret.util.toastLong
import jp.sabiz.android.clipsecret.util.toastShort


class ClipSecret : AppCompatActivity() {

    private val REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 0xBEEF
    private lateinit var keyguardManager:KeyguardManager
    private lateinit var store: SecretStore
    private val async: Async = Async()
    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keyguardManager = getSystemService(KeyguardManager::class.java)
        url = getUrlFromIntent(this.intent).toString()
        if (url.isEmpty()) {
            finishAndRemoveTask()
            return
        }

        if (!keyguardManager.isKeyguardSecure) {
            toastLong(this, "You need setup security lock")
            finishAndRemoveTask()
            return
        }

        setContentView(R.layout.activity_clip_secret)
        showAuthenticationScreen()
    }

    override fun finishAndRemoveTask() {
        super.finishAndRemoveTask()
        async.quit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            return
        }
        if (resultCode == RESULT_OK) {
           load()
        } else {
            toastShort(this,"Authentication failed...")
            finishAndRemoveTask()
        }
    }

    private fun showAuthenticationScreen() {
        val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                    getString(R.string.app_name), "Please input your device secure lock")
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS)
        }
    }

    private fun load() {
        store = SecretStore(this)
        val secretsList = store.findNamesByUrl(url)
        if(secretsList.isEmpty()){
            toastShort(this,"Secrets empty...")
            finishAndRemoveTask()
            return
        }

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, secretsList)
        val adapter = SwipeActionAdapter(arrayAdapter)
        val listView = findViewById<ListView>(R.id.listView)
        adapter.setListView(listView)
        adapter.setSwipeActionListener(object : SwipeActionAdapter.SwipeActionListener {
            override fun hasActions(position: Int, direction: SwipeDirection): Boolean {
                return direction.isLeft
            }

            override fun shouldDismiss(position: Int, direction: SwipeDirection): Boolean {
                return true
            }

            override fun onSwipe(positionList: IntArray, directionList: Array<SwipeDirection>) {
                AlertDialog.Builder(this@ClipSecret,R.style.AlertDialogStyle)
                        .setTitle("Delete " + secretsList[positionList[0]] + " ?")
                        .setNegativeButton("Cancel",null)
                        .setPositiveButton("OK", { _, _ ->
                            async.post {
                                val secret = store.findSecretByUrlAndName(url,secretsList[positionList[0]])
                                store.delete(secret)
                                Handler(Looper.getMainLooper()).post {
                                    secretsList.removeAt(positionList[0])
                                    adapter.notifyDataSetChanged()
                                    if(secretsList.isEmpty()) {
                                        toastShort(this@ClipSecret,"Secrets empty...")
                                        finishAndRemoveTask()
                                    }else {
                                        toastShort(this@ClipSecret,"Deleted...")
                                    }

                                }
                            }

                        })
                        .show()
            }
        })
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, index, _ ->

            async.post {
                val secret = store.findSecretByUrlAndName(url,secretsList[index])
                val clipboard = getSystemService(ClipboardManager::class.java)
                val clipData = ClipData.newPlainText("Clip", secret.password)
                clipboard.primaryClip = clipData
                toastLong(this,"Copy to clipboard!\nClear clipboard after 10 seconds.")
                async.postDelayed({
                    val clear = ClipData.newPlainText("", "")
                    clipboard.primaryClip = clear
                    toastLong(this,"Clear clipboard")
                    finishAndRemoveTask()
                },10)
            }
            finish()
        }

        listView.adapter = adapter
    }
}
