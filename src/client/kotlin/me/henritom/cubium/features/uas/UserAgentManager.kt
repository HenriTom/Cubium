package me.henritom.cubium.features.uas

import com.cinemamod.mcef.MCEFSettings

class UserAgentManager {

    var userAgent: String = ""
    val userAgents = mutableListOf<UserAgent>()

    private fun getRandomUserAgent(): UserAgent {
        return userAgents.random()
    }

    fun updateUserAgent(userAgent: String) {
        this.userAgent = userAgent

        val mcefSettings = MCEFSettings()

        if (userAgent.isEmpty() || userAgent == "")
            mcefSettings.userAgent = null

        else if (userAgent == "random")
            mcefSettings.userAgent = getRandomUserAgent().userAgent

        else
            mcefSettings.userAgent = userAgent
    }
}