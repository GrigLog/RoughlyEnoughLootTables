package griglog.relt

import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.text.Text


fun wrapHoverName(stack: ItemStack) {
    stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("*").append(stack.name).append("*"))
}