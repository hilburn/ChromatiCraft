/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.Auxiliary.RecipeManagers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import net.minecraft.item.ItemStack;
import Reika.ChromatiCraft.Magic.ElementTagCompound;
import Reika.ChromatiCraft.Registry.ItemMagicRegistry;
import Reika.DragonAPI.Instantiable.Data.KeyedItemStack;

public class FabricationRecipes {

	private static final FabricationRecipes instance = new FabricationRecipes();

	private ArrayList<ItemStack> products = new ArrayList();
	private final HashMap<KeyedItemStack, ElementTagCompound> data = new HashMap();
	private static final float SCALE = 0.8F;

	public static final float INITFACTOR = 0.5F;
	public static final int FACTOR = 400;
	public static final int POWER = 5;

	private int max;

	public static FabricationRecipes recipes() {
		return instance;
	}

	private FabricationRecipes() {
		Collection<KeyedItemStack> c = ItemMagicRegistry.instance.keySet();
		for (KeyedItemStack k : c) {
			ElementTagCompound tag = ItemMagicRegistry.instance.getItemValue(k);
			tag.scale(INITFACTOR);
			tag.power(POWER);
			tag.scale(FACTOR);
			data.put(k, tag);
			max = Math.max(max, tag.getMaximumValue());
		}
	}

	public Collection<ItemStack> getItemsFabricableWith(ElementTagCompound tag) {
		Collection<ItemStack> items = new ArrayList();
		for (KeyedItemStack ks : data.keySet()) {
			ElementTagCompound val = data.get(ks).copy().scale(SCALE);
			if (tag.containsAtLeast(val))
				items.add(ks.getItemStack());
		}
		return items;
	}

	public boolean isItemFabricable(ItemStack is, ElementTagCompound tag) {
		KeyedItemStack ks = new KeyedItemStack(is);
		return data.containsKey(ks) && tag.containsAtLeast(this.getItemCost(ks));
	}

	public ElementTagCompound getItemCost(ItemStack is) {
		return this.getItemCost(new KeyedItemStack(is));
	}

	private ElementTagCompound getItemCost(KeyedItemStack is) {
		ElementTagCompound tag = data.get(is);
		return tag != null ? tag.copy().scale(1/SCALE) : null;
	}

	public int getMaximumCost() {
		return max;
	}

}
