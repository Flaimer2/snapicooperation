package ru.snapix.snapicooperation.settings

import ru.snapix.library.libs.dazzleconf.annote.ConfDefault.DefaultString

interface DatabaseConfig {
    @DefaultString("localhost:3306")
    fun host(): String

    @DefaultString("server_global")
    fun database(): String

    @DefaultString("root")
    fun username(): String

    @DefaultString("root")
    fun password(): String
}