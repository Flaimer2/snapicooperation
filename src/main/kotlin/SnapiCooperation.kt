package ru.snapix.snapicooperation

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import ru.snapix.library.bukkit.SnapiLibraryBukkit
import ru.snapix.library.network.ServerType
import ru.snapix.library.network.player.NetworkPlayer
import ru.snapix.snapicooperation.api.Party
import ru.snapix.snapicooperation.caches.Friends
import ru.snapix.snapicooperation.commands.Commands
import ru.snapix.snapicooperation.database.FriendDatabase
import ru.snapix.snapicooperation.listeners.ChatListener
import ru.snapix.snapicooperation.listeners.ConnectionListener
import ru.snapix.snapicooperation.listeners.FriendListener
import ru.snapix.snapicooperation.listeners.PartyListener

class SnapiCooperation : JavaPlugin() {
    override fun onLoad() {
        instance = this
    }

    override fun onEnable() {
        if (SnapiLibraryBukkit.instance.serverType == ServerType.LOBBY) {
            FriendDatabase.load()
            FriendDatabase.users().forEach { Friends.update(it) }
        }
        server.pluginManager.registerEvents(ConnectionListener(), this)
        server.pluginManager.registerEvents(PartyListener(), this)
        server.pluginManager.registerEvents(FriendListener(), this)
        server.pluginManager.registerEvents(ChatListener(), this)
        Commands.enable()
    }

    companion object {
        lateinit var instance: SnapiCooperation
    }
}

val plugin = SnapiCooperation.instance