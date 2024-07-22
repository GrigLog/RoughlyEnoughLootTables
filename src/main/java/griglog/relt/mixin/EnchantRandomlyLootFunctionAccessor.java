package griglog.relt.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import net.minecraft.registry.entry.RegistryEntryList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(EnchantRandomlyLootFunction.class)
public interface EnchantRandomlyLootFunctionAccessor {

    @Accessor("options")
    Optional<RegistryEntryList<Enchantment>> getOptions();
}
