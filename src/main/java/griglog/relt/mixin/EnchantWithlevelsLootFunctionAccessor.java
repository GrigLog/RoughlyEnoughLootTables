package griglog.relt.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.loot.function.EnchantWithLevelsLootFunction;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.registry.entry.RegistryEntryList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(EnchantWithLevelsLootFunction.class)
public interface EnchantWithlevelsLootFunctionAccessor {

    @Accessor("levels")
    LootNumberProvider getLevels();

    @Accessor("options")
    Optional<RegistryEntryList<Enchantment>> getOptions();
}
