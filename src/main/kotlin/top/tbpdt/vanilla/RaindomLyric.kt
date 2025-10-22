package top.tbpdt.vanilla

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import top.tbpdt.configer.GlobalConfig
import top.tbpdt.utils.MessageUtils.getPlainText
import top.tbpdt.vanilla.PluginMain.dataFolder
import java.io.File

/**
 * 歌词数据结构
 */
@Serializable
data class Lyrics(
    val author: List<String>,
    val title: String,
    val year: Int,
    val lines: List<String>
)

object RaindomLyric : SimpleListenerHost() {
    private val LyricsJSONPath = "${dataFolder}${File.separator}${GlobalConfig.LyricsJSON}"
    private var cachedLyricsList: List<Lyrics>? = null

    private fun loadLyricsData(): List<Lyrics> {
        if (cachedLyricsList != null) {
            return cachedLyricsList!!
        }
        try {
            val jsonFile = File(LyricsJSONPath)
            if (!jsonFile.exists()) {
                throw IllegalStateException("歌词 JSON 文件未找到: $LyricsJSONPath")
            }
            val jsonString = jsonFile.readText(Charsets.UTF_8)
            cachedLyricsList = Json.decodeFromString<List<Lyrics>>(jsonString)
            if (cachedLyricsList.isNullOrEmpty()) {
                throw IllegalStateException("加载了一个空的 JSON 列表……")
            }
            return cachedLyricsList!!
        } catch (e: Exception) {
            cachedLyricsList = null
            throw e
        }
    }

    fun getLyrics(): Lyrics {
        return try {
            val lyricsList = loadLyricsData()
            lyricsList.random()
        } catch (e: Exception) {
            Lyrics(emptyList(), "", 0, listOf("[未获取到歌词]"))
        }
    }

    fun Lyrics.toFixedString(): String {
        if (this.year == 0 || this.lines.isEmpty()) {
            return "[未获取到歌词]"
        }
        val result = StringBuilder()
        for (line in this.lines) {
            result.append(line)
            result.append('\n')
        }
        result.append("——")
        for (author in this.author) {
            result.append(author)
            result.append(' ')
        }
        result.append("《${this.title}》(${this.year})")
        return result.toString()
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun GroupMessageEvent.onLyrics() {
        if (message.getPlainText() != "歌词") {
            return
        }
        val lyrics = getLyrics()
        if (lyrics.year == 0) {
            group.sendMessage("唔姆，读取歌词文件失败，请检查文件路径和格式...")
        } else {
            group.sendMessage(lyrics.toFixedString())
        }
    }
}