package com.elytraswap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypedActionResult;

public class ElytraSwap implements ModInitializer {

	@Override
	public void onInitialize() {
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack held = player.getStackInHand(hand);

			if (player.isSpectator() || !isChestItem(held)) {
				return TypedActionResult.pass(held);
			}

			ItemStack worn = player.getEquippedStack(EquipmentSlot.CHEST);

			// Debe haber algo equipado, y uno debe ser elytra y el otro un peto
			if (worn.isEmpty() || !isChestItem(worn) || isElytra(held) == isElytra(worn)) {
				return TypedActionResult.pass(held);
			}

			// Respetar la maldicion de ligadura (salvo en creativo)
			if (!player.isCreative() && EnchantmentHelper.hasBindingCurse(worn)) {
				return TypedActionResult.pass(held);
			}

			if (!world.isClient) {
				ItemStack newWorn = held.copy();
				ItemStack newHeld = worn.copy();
				player.equipStack(EquipmentSlot.CHEST, newWorn);
				player.setStackInHand(hand, newHeld);

				// Sonido de equipar del objeto que pasa a estar puesto
				world.playSound(null, player.getX(), player.getY(), player.getZ(),
						getEquipSound(newWorn), SoundCategory.PLAYERS, 1.0F, 1.0F);
			}

			return TypedActionResult.success(player.getStackInHand(hand));
		});
	}

	private static SoundEvent getEquipSound(ItemStack stack) {
		if (isElytra(stack)) {
			return SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA;
		}
		return ((ArmorItem) stack.getItem()).getMaterial().getEquipSound();
	}

	private static boolean isElytra(ItemStack stack) {
		return stack.getItem() == Items.ELYTRA;
	}

	private static boolean isChestItem(ItemStack stack) {
		if (isElytra(stack)) return true;
		return stack.getItem() instanceof ArmorItem
				&& ((ArmorItem) stack.getItem()).getSlotType() == EquipmentSlot.CHEST;
	}
}
