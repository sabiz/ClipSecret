package jp.sabiz.android.clipsecret

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
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
import jp.sabiz.android.clipsecret.util.toastShort


class ClipSecret : AppCompatActivity() {

    private lateinit var store: SecretStore
    private val async: Async = Async()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = getUrlFromIntent(this.intent).toString()
        if(url.isEmpty()){
            finish()
            return
        }

        setContentView(R.layout.activity_clip_secret)
        store = SecretStore(this)
        val secretsList = store.findNamesByUrl(url)
        if(secretsList.isEmpty()){
            toastShort(this,"Secrets empty...")
            finish()
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
                                        finish()
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
                toastShort(this,"Copy to clipboard!")
            }
            finish()
        }

        listView.adapter = adapter
    }


    override fun finish() {
        super.finish()
        async.quit()
    }
}
