package griglog.relt

import net.minecraft.network.chat.TextComponent
import net.minecraft.world.item.ItemStack

fun wrapHoverName(stack: ItemStack){
    stack.hoverName = TextComponent("*").append(stack.hoverName).append("*")
}