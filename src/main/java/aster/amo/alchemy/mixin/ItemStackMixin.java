package aster.amo.alchemy.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import aster.amo.alchemy.conversion.Transmuter;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @ModifyExpressionValue(
            method = "<clinit>",
            at =
            @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/serialization/Codec;lazyInitialized(Ljava/util/function/Supplier;)Lcom/mojang/serialization/Codec;",
                    ordinal = 0
            )
    )
    private static Codec<ItemStack> modifyBaseCodec(Codec<ItemStack> original) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<ItemStack, T>> decode(DynamicOps<T> ops, T input) {
                return original.decode(ops, Transmuter.INSTANCE.convertItem(input, ops));
            }

            @Override
            public <T> DataResult<T> encode(ItemStack input, DynamicOps<T> ops, T prefix) {
                return original.encode(input, ops, prefix);
            }
        };
    }
}
