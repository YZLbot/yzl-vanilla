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
                    cave_id TEXT,
                    text TEXT,
                    sender_id LONG,
                    sender_nick TEXT,
                    sha_256 TEXT,
                    date DATE,
                    PRIMARY KEY (cave_id)
                )
                """
                )
            }
            connection.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun saveComment(caveId: Int, text: String, senderId: Long, senderNick: String) {
        val query = """
        INSERT INTO cave_comments (cave_id, text, sender_id, sender_nick, sha_256, date)
        VALUES (?, ?, ?, ?, ?, ?)
    """

        connectToDB().use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setInt(1, caveId)
                preparedStatement.setString(2, text)
                preparedStatement.setLong(3, senderId)
                preparedStatement.setString(4, senderNick)
                preparedStatement.setString(5, getSHA256(text))

                val currentTimestamp = Timestamp(System.currentTimeMillis())
                val currentDate = Date(currentTimestamp.time)
                preparedStatement.setDate(6, currentDate)

                preparedStatement.executeUpdate()
            }
        }
    }


    fun loadComments(caveId: Int): List<Comment> {
        val query = """
        SELECT text, sender_id, sender_nick, sha_256, date FROM cave_comments WHERE cave_id=?
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
                            resultSet.getString("sha_256"),
                            resultSet.getDate("date")
                        )
                    } else {
                        null
                    }
                }.toList()
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
        val sha256: String,
        val date: Date
    )

    fun initCaveDB() {
        logger.info("建表中，地址: ${dataFolder}${File.separator}cave.db")
        connectToDB()
    }

}