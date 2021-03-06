/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.Auxiliary.RecipeManagers.CastingRecipes.Blocks;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import Reika.ChromatiCraft.Auxiliary.RecipeManagers.CastingRecipe.PylonRecipe;
import Reika.ChromatiCraft.Registry.CrystalElement;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;

public class RecipeEnderTNT extends PylonRecipe {

	public RecipeEnderTNT(ItemStack out, ItemStack main) {
		super(out, main);

		this.addAuxItem(Blocks.tnt, 2, 0);
		this.addAuxItem(Blocks.tnt, -2, 0);
		this.addAuxItem(Blocks.tnt, 0, 2);
		this.addAuxItem(Blocks.tnt, 0, -2);

		this.addAuxItem(ReikaItemHelper.stoneBricks, 2, 2);
		this.addAuxItem(ReikaItemHelper.stoneBricks, -2, 2);
		this.addAuxItem(ReikaItemHelper.stoneBricks, 2, -2);
		this.addAuxItem(ReikaItemHelper.stoneBricks, -2, -2);

		this.addAuraRequirement(CrystalElement.PINK, 500);
		this.addAuraRequirement(CrystalElement.BLACK, 500);
		this.addAuraRequirement(CrystalElement.YELLOW, 1000);
		this.addAuraRequirement(CrystalElement.LIME, 500);
	}

}
