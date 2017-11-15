package jp.sabiz.android.clipsecret

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import jp.sabiz.android.clipsecret.data.SecretStore
import jp.sabiz.android.clipsecret.util.getTitleFromIntent
import jp.sabiz.android.clipsecret.util.getUrlFromIntent
import jp.sabiz.android.clipsecret.util.Async
import jp.sabiz.android.clipsecret.util.toastShort


class MainActivity : AppCompatActivity() {

    private lateinit var editUrl:EditText
    private lateinit var editName:EditText
    private lateinit var editPassword:EditText
    private lateinit var btnCancel:Button
    private lateinit var btnSave:Button
    private lateinit var store: SecretStore
    private val async:Async = Async()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = getUrlFromIntent(this.intent)
        val title = getTitleFromIntent(this.intent)
        if(url.isEmpty()){
            finish()
        }

        setContentView(R.layout.activity_main)
        editUrl = findViewById(R.id.editUrl)
        editName = findViewById(R.id.editName)
        editPassword = findViewById(R.id.editPass)
        btnCancel = findViewById(R.id.btnCancel)
        btnSave = findViewById(R.id.btnSave)
        store = SecretStore(this)


        editUrl.setText(url)
        editName.setText(title)
        btnCancel.setOnClickListener { finish() }
        btnSave.setOnClickListener {
            if(editName.text.isEmpty()){
                toastShort(this,"Name required...")
                return@setOnClickListener
            }
            if(editPassword.text.isEmpty()){
                toastShort(this,"Password required...")
                return@setOnClickListener
            }
            async.post({
                if(store.add(editUrl.text.toString(),editName.text.toString(),editPassword.text.toString())) {
                    toastShort(this, "Add secret!")
                }
            })
            finish()
        }
    }

    override fun finish() {
        super.finish()
        async.quit()
    }
}
