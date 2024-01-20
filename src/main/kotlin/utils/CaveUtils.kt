package top.tbpdt.utils

import top.tbpdt.PluginMain.dataFolder
import top.tbpdt.PluginMain.logger
import java.io.File
import java.security.MessageDigest
import java.sql.*

/**
 * @author Takeoff0518
 */
object CaveUtils {

    private fun getSHA256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }.substring(0, 7)
    }

    private fun connectToDB(): Connection {
        val dbPath = "${dataFolder}${File.separator}cave.db"
        if (!File(dbPath).exists()) {
            createDB(dbPath)
        }
        return DriverManager.getConnection("jdbc:sqlite:$dbPath")
    }

    private fun createDB(dbPath: String) {
        try {
            val connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")
            connection.createStatement().use { statement ->
                statement.execute(
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
            connection.close()
        } catch (e: SQLException) {
            logger.error("数据库创建失败: ", e)
        }
    }

    fun saveComment(caveId: Int, text: String, senderId: Long, senderNick: String, groupId: Long, groupNick: String) {
        val query = """
        INSERT INTO cave_comments (cave_id, text, sender_id, sender_nick, group_id, group_nick, date)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """

        connectToDB().use { connection ->
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

        connectToDB().use { connection ->
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
        SELECT cave_id, text, sender_id, sender_nick, group_id, group_nick, pick_count, date FROM cave_comments WHERE cave_id LIKE ?
    """

        connectToDB().use { connection ->
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

        connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setInt(1, caveId)

                preparedStatement.executeUpdate()
            }
        }
    }

    fun getCommentCount(): Int {
        val query = "SELECT COUNT(*) as count FROM cave_comments"

        return connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.executeQuery().use { resultSet ->
                    if (resultSet.next()) resultSet.getInt("count") else 0
                }
            }
        }
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

    fun initCaveDB() {
        Class.forName("org.sqlite.JDBC")
        logger.info("建表中，地址: ${dataFolder}${File.separator}cave.db")
        connectToDB()
    }

}