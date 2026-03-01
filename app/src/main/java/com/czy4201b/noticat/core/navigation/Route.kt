package com.czy4201b.noticat.core.navigation

sealed class Route(val route: String) {
    object Main : Route("main")

    object Readme : Route("readme")

    object Login : Route("login")

    object GlobalFilters : Route("global_filters")

    object Edit :
        Route("edit/{client}?subscriptionId={subscriptionId}") {
        fun createRoute(client: String) = "edit/$client"

        fun updateRoute(client: String, subscriptionId: Int) =
            "edit/$client?subscriptionId=$subscriptionId"

    }

    object UpdateDialog : Route("update_dialog")
}