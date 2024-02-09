package top.tbpdt.utils

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.emptyMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import top.tbpdt.PluginMain.dataFolder
import top.tbpdt.PluginMain.logger
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.sql.Date
import java.sql.Timestamp

/**
 * @author Takeoff0518
 */
object CaveUtils {

    private val picPath = "${dataFolder}${File.separator}images${File.separator}"

    private fun getSHA256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun createTable() {
        DBUtils.connectToDB().use { connection ->
            DBUtils.createTable(
                connection,
                """
                    CREATE TABLE IF NOT EXISTS cave_comments (
                        cave_id LONG,
                        text TEXT,
                        sender_id LONG,
                        sender_nick TEXT,
                        group_id LONG,
                        group_nick TEXT,
                        pick_count LONG,
                        date DATE,
                        PRIMARY KEY (cave_id)
                    )
                    """
            )
        }
        logger.info("已尝试创建数据表 cave_comments")
    }

    fun saveComment(caveId: Int, text: String, senderId: Long, senderNick: String, groupId: Long, groupNick: String) {
        val query = """
        INSERT INTO cave_comments (cave_id, text, sender_id, sender_nick, group_id, group_nick, date)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setInt(1, caveId)
                preparedStatement.setString(2, text)
                preparedStatement.setLong(3, senderId)
                preparedStatement.setString(4, senderNick)
                preparedStatement.setLong(5, groupId)
                preparedStatement.setString(6, groupNick)

                val currentTimestamp = Timestamp(System.currentTimeMillis())
                val currentDate = Date(currentTimestamp.time)
                preparedStatement.setDate(7, currentDate)

                preparedStatement.executeUpdate()
            }
        }
    }

    /**
     * 查找指定 ID 回声洞
     */
    fun loadComments(caveId: Int): List<Comment> {
        val query = """
        SELECT text, sender_id, sender_nick, group_id, group_nick, pick_count, date FROM cave_comments WHERE cave_id=?
    """

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setInt(1, caveId)

                val resultSet = preparedStatement.executeQuery()

                return generateSequence {
                    if (resultSet.next()) {
                        Comment(
                            caveId,
                            resultSet.getString("text"),
                            resultSet.getLong("sender_id"),
                            resultSet.getString("sender_nick"),
                            resultSet.getLong("group_id"),
                            resultSet.getString("group_nick"),
                            resultSet.getLong("pick_count"),
                            resultSet.getDate("date")
                        )
                    } else {
                        null
                    }
                }.toList()
            }
        }
    }

    /**
     * 查找含指定内容回声洞
     */
    fun loadComments(target: String): List<Comment> {
        val query = """
        SELECT cave_id, text, sender_id, sender_nick, group_id, group_nick, pick_count, date FROM cave_comments WHERE text LIKE ?
    """

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setString(1, "%$target%")

                val resultSet = preparedStatement.executeQuery()

                return generateSequence {
                    if (resultSet.next()) {
                        Comment(
                            resultSet.getInt("cave_id"),
                            resultSet.getString("text"),
                            resultSet.getLong("sender_id"),
                            resultSet.getString("sender_nick"),
                            resultSet.getLong("group_id"),
                            resultSet.getString("group_nick"),
                            resultSet.getLong("pick_count"),
                            resultSet.getDate("date")
                        )
                    } else {
                        null
                    }
                }.toList()
            }
        }
    }

    fun updatePickCount(caveId: Int) {
        val query = "UPDATE cave_comments SET pick_count = pick_count + 1 WHERE cave_id = ?"

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setInt(1, caveId)

                preparedStatement.executeUpdate()
            }
        }
    }

    fun getCommentCount(): Int {
        val query = "SELECT COUNT(*) as count FROM cave_comments"

        return DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.executeQuery().use { resultSet ->
                    if (resultSet.next()) resultSet.getInt("count") else 0
                }
            }
        }
    }

    /**
     * 获取文件扩展名。如果没有则用 `.png` 替代
     * @param url 图片链接
     */
    private fun getFileExtension(url: String): String {
        return try {
            val imageUrl = URL(url)
            val connection = imageUrl.openConnection()
            val contentType = connection.contentType

//        when (contentType) {
//            "image/jpeg" -> ".jpg"
//            "image/png" -> ".png"
//            "image/gif" -> ".gif"
//            else -> ".png"
//        }
            "." + contentType.removePrefix("image/")
        } catch (e: IOException) {
            logger.warning("获取文件扩展名时出错：$e")
            ".unknown"
        }

    }

    private fun getFileName(url: String): String {
        return getSHA256(url) + getFileExtension(url)
    }

    /**
     * 下载图片，并将其保存在 images 目录下
     * @param url 图片地址（http）
     * @exception Exception 图片下载失败
     */
    private fun downloadAndSaveImage(url: String) {
        try {
            val fileName = getFileName(url)
            val savePath = picPath + fileName
            File(picPath).mkdirs()
            val imageUrl = URL(url)
            imageUrl.openConnection().getInputStream().use { input ->
                FileOutputStream(savePath).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: IOException) {
            logger.warning("下载图片时出现意外：", e)
        }
    }

    /**
     * 检查图片是否可用
     *  @param url 图片地址（http）
     *  @return 图片是否可用
     */
    private fun isHttpFileExists(url: String): Boolean {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            val responseCode = connection.responseCode
            return responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 更新单条回声洞信息
     */
    private fun updateCaveComments(caveId: Int, text: String) {
        val query = "UPDATE cave_comments SET text = ? WHERE cave_id = ?"

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setString(1, text)
                preparedStatement.setInt(2, caveId)

                preparedStatement.executeUpdate()
            }
        }
    }

    /**
     * 替换过期的图片（重新上传），并更新回声洞里的图片信息
     */
    suspend fun Comment.replaceExpiredImage(contact: Contact): MessageChain {
        val message = text.deserializeMiraiCode()
        var result = emptyMessageChain()
        for (i in message) {
            /*
            tx 的 imageId 规则已改变：
            Caused by: java.lang.IllegalArgumentException: Illegal imageId:
            'http://gchat.qpic.cn/gchatpic_new/0/0-0-40B0CDB56899F21C897A8771EEA45DF7/0?term=2'.
            ImageId must match Regex `/[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}`,
            `/[0-9]*-[0-9]*-[0-9a-fA-F]{32}` or `\{[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}\}\..{3,5}`
             */
//                if (i.isUploaded(bot)) {
            if (i is Image) {
                if (isHttpFileExists(i.queryUrl())) {
                    result += i
                    if (!File("$picPath${getFileName(i.queryUrl())}").exists()) {
                        downloadAndSaveImage(i.queryUrl())
                    }
                } else {
                    val updatedImage: Image
                    val imageFile =
                        File("$picPath${getFileName(i.queryUrl())}")
                    if (imageFile.exists()) {
                        imageFile.toExternalResource().use { resource ->
                            updatedImage = contact.uploadImage(resource)
                        }
                        result += updatedImage
                        if (!imageFile.renameTo(File("$picPath${getFileName(updatedImage.queryUrl())}"))) {
                            logger.warning("文件重命名失败！")
                        }
                    } else {
                        result += " [过期的图片] "
                    }
                }
            } else {
                result += i
            }
        }
        if (message != result) {
            updateCaveComments(this.caveId, result.serializeToMiraiCode())
        }
        return result
    }

    suspend fun downloadAllPictures(): Pair<Int, Int> {
        var totalCount = 0
        var successCount = 0
        for (i in 1..getCommentCount()) {
            val comment = loadComments(i).first()
            for (element in comment.text.deserializeMiraiCode().filterIsInstance<Image>()) {

                logger.info("询问 ${element.queryUrl()}")
                if (File(picPath + getFileName(element.queryUrl())).exists()) {
                    logger.warning("文件已存在")
                    continue
                }
                totalCount++
                if (!isHttpFileExists(element.queryUrl())) {
                    logger.warning("图片已过期")
                    continue
                }
                logger.info("正在下载……")
                downloadAndSaveImage(element.queryUrl())
                successCount++
            }
        }
        return Pair(totalCount, successCount)
    }

    data class Comment(
        val caveId: Int,
        val text: String,
        val senderId: Long,
        val senderNick: String,
        val groupId: Long,
        val groupNick: String,
        val pickCount: Long,
        val date: Date
    )

    private var lastCalledTime = 0L
    private const val CD_TIME = 40000L

    fun checkInterval(): Long {
        val currentTime = System.currentTimeMillis()
        val isFirst = lastCalledTime == 0L
        return if (isFirst) {
            lastCalledTime = currentTime
            -1
        } else {
            val elapsedTime = currentTime - lastCalledTime
            if (elapsedTime < CD_TIME) {
                (CD_TIME / 1000 - elapsedTime / 1000)
            } else {
                lastCalledTime = currentTime
                -1
            }
        }
    }
}