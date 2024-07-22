package ru.snapix.snapicooperation

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger
import ru.snapix.library.callEvent
import ru.snapix.library.velocity.VelocityPlugin
import ru.snapix.snapicooperation.api.User
import ru.snapix.snapicooperation.api.events.party.DisbandReason
import ru.snapix.snapicooperation.api.events.party.PartyDisbandEvent
import ru.snapix.snapicooperation.caches.Friends
import ru.snapix.snapicooperation.caches.Parties
import ru.snapix.snapicooperation.commands.Commands
import ru.snapix.snapicooperation.database.FriendDatabase
import ru.snapix.snapicooperation.listeners.ConnectListener
import ru.snapix.snapicooperation.listeners.PartyListener
import java.nio.file.Path

@Plugin(
    id = "snapicooperation",
    name = "SnapiCooperation",
    version = "1.0.0",
    authors = ["SnapiX"],
    dependencies = [Dependency(id = "snapilibrary")]
)
class SnapiCooperation @Inject constructor(server: ProxyServer, logger: Logger, @DataDirectory dataDirectory: Path) :
    VelocityPlugin() {
    init {
        init(server, logger, dataDirectory)
        instance = this
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        FriendDatabase.load()
        server.eventManager.register(this, PartyListener())
        server.eventManager.register(this, ConnectListener())
        Commands.enable()
        FriendDatabase.users().forEach { Friends.update(it) }
    }

    @Subscribe
    fun onDisable(event: ProxyShutdownEvent) {
        Parties.values().forEach {
            plugin.callEvent(PartyDisbandEvent(it.leader.toPlayer()!!, it, DisbandReason.DISABLE_PLUGIN))
            it.remove()
        }
    }

    companion object {
        lateinit var instance: SnapiCooperation
    }
}

val plugin = SnapiCooperation.instance

val Player.user: User
    get() {
        return User[this] ?: error("User in SnapiCooperation can't be null")
    }
val String.user: User
    get() {
        return User[this] ?: error("User in SnapiCooperation can't be null")
    }