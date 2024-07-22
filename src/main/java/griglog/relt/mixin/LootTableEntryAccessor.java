package griglog.relt.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootTableEntry.class)
public interface LootTableEntryAccessor {

    @Accessor("value")
    Either<RegistryKey<LootTable>, LootTable> getValue();
}
