package top.tbpdt.vanilla.utils

import top.tbpdt.vanilla.PluginMain.dataFolder
import top.tbpdt.vanilla.PluginMain.logger
import java.io.File
import java.io.IOException
import java.net.URL

/**
 * @author Takeoff0518
 */
object CensorUtils {
    val fileName = "sensitiveWord.txt"
    val file = File("${dataFolder}${File.separator}${fileName}")
    var censorDict = emptySet<String>()

    fun initCensorDict() {
        censorDict = if (file.exists()) {
            logger.info("试图装载敏感词词库……")
            file.readLines().toSet()
        } else {
            logger.info("未找到敏感词词库，正在尝试下载……")
            val url = "https://raw.githubusercontent.com/YZLbot/censor-dict/master/sensitiveWord.txt"
            try {
                val text = URL(url).readText()
                file.writeText(text)
                logger.info("成功下载并保存敏感词词库~")
                logger.info("试图装载敏感词词库……")
                text.lineSequence().toSet()
            } catch (e: IOException) {
                logger.warning(
                    "敏感词词库下载失败，敏感词检测功能暂时关闭！请手动下载词库，并将其放置在\n" +
                            "${dataFolder}\n下，下载地址：\n${url}"
                )
                emptySet()
            }
        }
    }

    fun checkCensor(str: String): String? {
        return censorDict.find { str.contains(it) }
    }
}