package ru.snapix.snapicooperation.api.events.friend

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import ru.snapix.snapicooperation.api.User

class FriendRemoveEvent(val user: User, val friend: User) : Event() {
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