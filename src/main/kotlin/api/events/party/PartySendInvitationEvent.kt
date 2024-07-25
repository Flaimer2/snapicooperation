package ru.snapix.snapicooperation.api.events.party

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import ru.snapix.library.network.player.NetworkPlayer
import ru.snapix.snapicooperation.api.Party

class PartySendInvitationEvent(val receiver: NetworkPlayer, val party: Party) : Event() {
    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}