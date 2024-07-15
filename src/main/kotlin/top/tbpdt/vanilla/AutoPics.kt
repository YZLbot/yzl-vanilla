package top.tbpdt.vanilla

import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import top.tbpdt.vanilla.PluginMain.logger
import top.tbpdt.vanilla.configer.AutoPicsConfig.regexMap
import java.io.File

/**
 * @author Takeoff0518
 */
object AutoPics : SimpleListenerHost() {
    // k = regex, v = files
    val pathMap: MutableMap<Regex, List<File>> = mutableMapOf()
    fun initPaths() {
        logger.info("正在载入关键词触发图片...")
        for (i in regexMap) {
            val listedFile: MutableList<File> = mutableListOf()
            val dirFile = File(i.value)
            if (!dirFile.exists()) {
                logger.warning("未检测到 ${dirFile.path} 存在，已忽略表达式 ${i.key}！")
                continue
            }
            logger.info("正在载入 ${dirFile.path}，表达式 ${i.key}")
            dirFile.walk().forEach { file -> // 递归遍历
                if (file.extension in listOf("bmp", "jpg", "jpeg", "png", "gif", "webp")) {
                    listedFile.add(file)
                }
            }
            pathMap[i.key.toRegex()] = listedFile
        }
        logger.info("图片载入完成~")
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun GroupMessageEvent.handle() {
        val sourceMsg = message.serializeToMiraiCode().trim()
        for (i in pathMap) {
            if (i.key.matches(sourceMsg)) {
                group.sendImage(i.value.random())
            }
        }
    }
}