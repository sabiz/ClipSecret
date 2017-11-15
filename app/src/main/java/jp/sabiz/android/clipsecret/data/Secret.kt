package jp.sabiz.android.clipsecret.data


/**
 * @auther sabi
 * Created on 2017/11/13.
 */
data class Secret(
        val url: String,
        val name: String,
        val password: String
){
    override fun equals(other: Any?): Boolean {
        if(other !is Secret)return false

        return this.url == other.url &&
                this.name == other.name
    }

    override fun toString(): String {
        return name
    }
}

data class Secrets(
        val secrets: MutableList<Secret>
)