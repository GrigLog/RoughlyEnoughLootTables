package griglog.relt.entry_points

import net.fabricmc.api.ModInitializer

class ReltCommon : ModInitializer {
    override fun onInitialize() {
        ReltServer().onInitialize()
    }
}