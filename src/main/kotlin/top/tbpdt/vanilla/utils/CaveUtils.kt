package top.tbpdt.utils

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.emptyMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import top.tbpdt.vanilla.PluginMain.dataFolder
import top.tbpdt.vanilla.PluginMain.logger
import top.tbpdt.vanilla.configer.CaveConfig.CDTime
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

        DBUtils.connectToDB().use { connection ->
            DBUtils.createTable(
                connection,
                """
                    CREATE TABLE IF NOT EXISTS cave_pics (
                        name TEXT,
                        url TEXT,
                        PRIMARY KEY (name)
                    )
                    """
            )
        }
        logger.info("已尝试创建数据表 cave_pics")
    }

    fun saveComment(caveId: Int, text: String, senderId: Long, senderNick: String, groupId: Long, groupNick: String) {
        val query = """
        INSERT INTO cave_comments (cave_id, text, sender_id, sender_nick, group_id, group_nick, date, pick_count)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
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

                preparedStatement.setLong(8, 0L)

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

    /**
     * 查询一个人的所有回声洞
     */
    fun loadCaveIds(senderId: Long): List<Int> {
        val query = """
        SELECT cave_id FROM cave_comments WHERE sender_id=?
    """

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setLong(1, senderId)

                val resultSet = preparedStatement.executeQuery()

                val caveIds = mutableListOf<Int>()
                while (resultSet.next()) {
                    caveIds.add(resultSet.getInt("cave_id"))
                }

                return caveIds
            }
        }
    }

    fun updatePickCount(caveId: Int) {
        val query = "UPDATE cave_comments SET pick_count = COALESCE(pick_count, 0) + 1 WHERE cave_id = ?"

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
    data class UserCommentInfo(
        val senderId: Long,
        val senderNick: String,
        val senderCount: Int
    )
    /**
     * 回声洞排名
     */
    fun getMostFrequentSenderId(): UserCommentInfo? {
        val query = """
        SELECT sender_id, sender_nick, COUNT(*) as count
        FROM cave_comments
        GROUP BY sender_id, sender_nick
        ORDER BY count DESC
        LIMIT 1
    """.trimIndent()

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return UserCommentInfo(
                            senderId = resultSet.getLong("sender_id"),
                            senderNick = resultSet.getString("sender_nick"),
                            senderCount = resultSet.getInt("count")
                        )
                    }
                }
            }
        }
        return null
    }
    fun getTopFiveSenders(): List<UserCommentInfo> {
        val query = """
        SELECT first_sender.sender_id, first_sender.sender_nick, COUNT(*) as count
        FROM cave_comments
        INNER JOIN (
            SELECT sender_id, MIN(sender_nick) as sender_nick
            FROM cave_comments
            GROUP BY sender_id
        ) first_sender ON cave_comments.sender_id = first_sender.sender_id
        GROUP BY first_sender.sender_id, first_sender.sender_nick
        ORDER BY count DESC
        LIMIT 5
    """.trimIndent()

        val commentInfos = mutableListOf<UserCommentInfo>()
        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        commentInfos.add(
                            UserCommentInfo(
                                senderId = resultSet.getLong("sender_id"),
                                senderNick = resultSet.getString("sender_nick"),
                                senderCount = resultSet.getInt("count")
                            )
                        )
                    }
                }
            }
        }
        return commentInfos
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
    @Deprecated("该方法已经弃用，请使用CaveUtils.downloadImage")
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
            connection.connectTimeout = 4120
            connection.readTimeout = 4120
            val responseCode = connection.responseCode
            connection.disconnect()
            responseCode == HttpURLConnection.HTTP_OK
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
    @Deprecated("该方法已经弃用，请使用CaveUtils.addImage")
    suspend fun Comment.replaceExpiredImage(contact: Contact): MessageChain {
        val message = text.deserializeMiraiCode()
        var result = emptyMessageChain()
        logger.info("检查图片中，语境：${contact}")
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
                val url = i.queryUrl()
                val name = getFileName(i.queryUrl())
                if (isHttpFileExists(i.queryUrl())) {
                    result += i
                    if (!isPicExists(url)) {
                        logger.info("发现未下载的图片，正在下载：$url")
                        downloadAndSaveImage(url)
                        addPicture(name, url)
                        logger.info("下载成功，文件名：$name")
                    } else {
                        logger.info("图片 $name 存在于本地，地址：$url")
                    }
                } else {
                    logger.warning("图片已过期：$url")
                    val updatedImage: Image
                    val imageFile = if (isPicExists(url))
                        File(picPath + File.separator + queryName(url))
                    else null

                    if (imageFile != null) {
                        logger.info("本地已存在该图片（${imageFile.name}），正在尝试上传")
                        imageFile.toExternalResource().use { resource ->
                            updatedImage = contact.uploadImage(resource)
                        }
                        logger.info("图片上传成功！地址：${updatedImage.queryUrl()}")
                        updateUrl(imageFile.name, updatedImage.queryUrl())
                        result += updatedImage
                    } else {
                        logger.warning("本地未找到该图片，标记为已过期")
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

    /**
     * 通过 URL 检查图片映射信息是否存在于数据库中
     */
    fun isPicExists(url: String): Boolean {
        val query = "SELECT 1 FROM cave_pics WHERE url = ? LIMIT 1"

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setString(1, url)
                val resultSet = preparedStatement.executeQuery()
                return resultSet.next()
            }
        }
    }

    /**
     * 通过文件名查找数据库中其对应的 URL
     */
    fun queryUrl(name: String): String {
        val query = "SELECT url FROM cave_pics WHERE name = ?"
        var url = ""

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setString(1, name)
                val resultSet = preparedStatement.executeQuery()

                if (resultSet.next()) {
                    url = resultSet.getString("user_nick")
                }
            }
        }
        return url
    }

    /**
     * 通过 URL 查找数据库中其对应的文件名
     */
    fun queryName(url: String): String {
        val query = "SELECT name FROM cave_pics WHERE url = ?"
        var name = ""

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setString(1, url)
                val resultSet = preparedStatement.executeQuery()

                if (resultSet.next()) {
                    name = resultSet.getString("name")
                }
            }
        }
        return name
    }

    /**
     * 通过文件名更新数据库中其对应的 URL
     */
    fun updateUrl(name: String, newUrl: String) {
        val query =
            "UPDATE cave_pics SET url = ? WHERE name = ?"
        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setString(1, newUrl)
                preparedStatement.setString(2, name)

                preparedStatement.executeUpdate()
            }
        }
    }

    /**
     * 向数据库中添加图片映射信息
     */
    fun addPicture(name: String, url: String) {
        val query = """
        INSERT INTO cave_pics (name, url)
        VALUES (?, ?)
    """

        DBUtils.connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setString(1, name)
                preparedStatement.setString(2, url)

                preparedStatement.executeUpdate()
            }
        }
    }

    /**
     * 上传回声洞图片
     */
    suspend fun Comment.addImage(contact: Contact): MessageChain {
        val message = text.deserializeMiraiCode()
        var result = emptyMessageChain()
        for (i in message) {
            /*
            The images' URLs are like
            https://multimedia.nt.qq.com.cn/download
            ?appid=1407
            &fileid=CgozNTQ3MzU5MjI5EhSFWqK1BiXEuHQshJb-W_F2bOx7JRi5ugUg_wooyMqcl4XriAMyBHByb2RQgL2jAQ
            &spec=0
            &rkey=CAQSKAB6JWENi5LMsAKnLnJn7TePeitGTSpWvGxAgVSMRrE_9Fauzoqzmus
            on 2024/10/01
            and
            https://multimedia.nt.qq.com.cn/download
            ?appid=1407
            &fileid=CgoxOTEwNDY0MDY5EhQN3snd9D1NjwkEbs8HacjSe0aaFRjq-hgg_woosO73loGaiQMyBHByb2RQgL2jAQ
            &spec=0
            &rkey=CAESKBkcro_MGujoykuBhn6vH1RAqS-YScuzV5dtspDyjm2LQFznq0Cs-XU
            on 2024/10/19
            */
            if (i is Image) {
                val imageUrl = i.queryUrl()
                logger.info("检查图片中，url = $imageUrl")
                if (isPicExists(imageUrl)) {
                    val fileName = queryName(imageUrl)
                    result += "[mirai:image:file:///$picPath${fileName}]".deserializeMiraiCode()
                    logger.info("图片 $fileName 存在于本地，已插入到消息中")
                } else {
                    logger.info("发现未下载的图片，正在下载：$imageUrl")
                    val imageFileName = downloadImage(imageUrl)
                    addPicture(imageFileName, imageUrl)
                    logger.info("下载成功，文件名：$imageFileName")
                    result += i
                }
            } else {
                result += i
            }
        }
        return result
    }

    /**
     * 下载图片，基于 okhttp
     * @return 下载图片的文件名
     */
    suspend fun downloadImage(url: String): String {
        val client = OkHttpClient()
        logger.info("尝试下载 $url")

        val request = Request.Builder().url(url).header(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 Edg/130.0.0.0"
        ).build()
        var fileName: String
        client.newCall(request).execute().use { response: Response ->
            if (!response.isSuccessful) {
                logger.error("意外的 HTTP 状态码：$response")
                throw IOException("Unexpected code $response")
            }
            val contentType = response.header("Content-Type")
            val extension = when {
                contentType?.contains("image/jpeg") == true -> "jpg"
                contentType?.contains("image/png") == true -> "png"
                contentType?.contains("image/gif") == true -> "gif"
                contentType?.contains("image/webp") == true -> "webp"
                else -> "jpg"
            }

            fileName = "${getSHA256(url)}.$extension"
            FileOutputStream(File(picPath + fileName)).use { outputStream ->
                response.body?.byteStream()?.use { inputStream ->
                    inputStream.copyTo(outputStream)
                } ?: {
                    logger.error("Response Body 为空！")
                    throw IOException("Response body is null")
                }
            }
            logger.info("图片已保存为 $picPath$fileName")
        }
        return fileName
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

    /**
     * 检查冷却时间
     *
     * >宜群！宣群！不择手段的宣群！要强杀群星！
     * >——时间之外的往事(188056008)
     *
     * @return 第一次调用时，返回 `-1`。
     *
     * 在后续的调用中，如果超过冷却时间，返回 `-1`，否则返回剩余时间的秒数。
     */
    @Deprecated("改用 top.tbpdt.utils.CDTimer")
    fun checkInterval(): Long {
        val currentTime = System.currentTimeMillis()
        val isFirst = lastCalledTime == 0L
        return if (isFirst) {
            lastCalledTime = currentTime
            -1
        } else {
            val elapsedTime = currentTime - lastCalledTime
            if (elapsedTime < CDTime) {
                (CDTime / 1000 - elapsedTime / 1000)
            } else {
                lastCalledTime = currentTime
                -1
            }
        }
    }
}