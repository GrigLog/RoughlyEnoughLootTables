package griglog.relt

import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

fun wrapHoverName(stack: ItemStack){
    stack.hoverName = Component.literal("*").append(stack.hoverName).append("*")
}