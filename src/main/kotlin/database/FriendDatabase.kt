package ru.snapix.snapicooperation.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import ru.snapix.snapicooperation.api.User
import ru.snapix.snapicooperation.settings.Settings

object FriendTable : Table("cooperation_friend") {
    val name: Column<String> = varchar("name", 32).uniqueIndex()
    val friends: Column<String?> = text("friend_name").nullable()
}

object FriendDatabase {
    private var database: Database

    init {
        val config = Settings.database
        database = Database.connect(
            url = "jdbc:mariadb://${config.host()}/${config.database()}",
            driver = "org.mariadb.jdbc.Driver",
            user = config.username(),
            password = config.password()
        )
    }

    fun load() {
        transaction(database) {
            SchemaUtils.create(FriendTable)
        }
    }

    fun update(user: User) {
        val value = user.friends.joinToString(":")
        transaction(database) {
            FriendTable.upsert {
                it[name] = user.name
                it[friends] = if (value == "") null else value
            }
        }
    }

    operator fun get(name: String) = transaction(database) {
        FriendTable.selectAll().where { FriendTable.name eq name }.map(::toUser).firstOrNull()
    }

    fun users(): List<User> {
        return transaction(database) {
            FriendTable.selectAll().map(::toUser)
        }
    }

    fun toUser(row: ResultRow): User {
        val friends = row[FriendTable.friends]?.split(":")?.toMutableList() ?: mutableListOf()
        friends.remove("")
        return User(
            row[FriendTable.name],
            friends,
            mutableSetOf()
        )
    }
}