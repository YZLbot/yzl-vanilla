package top.tbpdt.vanilla

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import top.tbpdt.utils.MessageUtils.getPlainText


/**
 * @author Takeoff0518
 */

/**
 * {
 *     "id": 5455,
 *     "uuid": "643355d9-03fc-4bc2-b1e5-604fcb8e832f",
 *     "hitokoto": "春宵一刻值千金，花有清香月有阴。",
 *     "type": "i",
 *     "from": "春宵·春宵一刻值千金",
 *     "from_who": "苏轼",
 *     "creator": "a632079",
 *     "creator_uid": 1044,
 *     "reviewer": 4756,
 *     "commit_from": "web",
 *     "created_at": "1586198952",
 *     "length": 16
 * }
 */
@Serializable
data class MyHitokoto(
    val id: Int,
    val uuid: String,
    val hitokoto: String,
    val type: String,
    val from: String,
    @SerialName("from_who")
    val fromWho: String?,
    val creator: String,
    @SerialName("creator_uid")
    val creatorUid: Int,
    val reviewer: Int,
    @SerialName("commit_from")
    val commitFrom: String,
    @SerialName("created_at")
    val createdAt: String,
    val length: Int
)

val client = HttpClient()

object Hitokoto: SimpleListenerHost() {

    suspend fun getHitokoto(type: String): MyHitokoto {

        val response: HttpResponse = client.get("https://v1.hitokoto.cn/") {
            parameter("c", type)
            parameter("encode", "json")
            parameter("charset", "utf-8")
        }
        val responseBody = response.bodyAsText()
        return Json.decodeFromString(MyHitokoto.serializer(), responseBody)
    }

    fun MyHitokoto.toFixedString(): String{
        if(this.id == 0){
            return "[未获取到一言]"
        }
        return  "${this.hitokoto}\n" +
                "——${this.fromWho ?: ""}${if (this.from == this.fromWho) "" else "《" + this.from + "》"}"
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun GroupMessageEvent.onHitokoto(){
        if(message.getPlainText() != "一言"){
            return
        }
        val hitokoto: MyHitokoto = try {
            getHitokoto("d")
        } catch (e: Exception) {
            MyHitokoto(0, "", "[未获取到一言]", "", "", null, "", 0, 0, "", "", 0)
        }
        group.sendMessage("${hitokoto.hitokoto}\n" +
                "——${hitokoto.fromWho ?: ""}${if (hitokoto.from == hitokoto.fromWho) "" else "《" + hitokoto.from + "》"}")
    }
}