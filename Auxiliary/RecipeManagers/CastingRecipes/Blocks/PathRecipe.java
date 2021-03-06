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

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import Reika.ChromatiCraft.Auxiliary.RecipeManagers.CastingRecipe.PylonRecipe;
import Reika.ChromatiCraft.Registry.ChromaBlocks;
import Reika.ChromatiCraft.Registry.CrystalElement;

public class PathRecipe extends PylonRecipe {

	public PathRecipe(ItemStack main, int meta, ItemStack outer) {
		super(ChromaBlocks.PATH.getStackOfMetadata(meta), main);

		this.addAuxItem(Blocks.stonebrick, -2, 0);
		this.addAuxItem(Blocks.stonebrick, 2, 0);
		this.addAuxItem(Blocks.stonebrick, 0, 2);
		this.addAuxItem(Blocks.stonebrick, 0, -2);

		this.addAuxItem(outer, -2, -2);
		this.addAuxItem(outer, 2, -2);
		this.addAuxItem(outer, 2, 2);
		this.addAuxItem(outer, -2, 2);

		this.addAuraRequirement(CrystalElement.LIME, 5000);
		this.addAuraRequirement(CrystalElement.BROWN, 500);
	}

	public PathRecipe(ItemStack main, int meta, Item outer) {
		this(main, meta, new ItemStack(outer));
	}

	public PathRecipe(ItemStack main, int meta, Block outer) {
		this(main, meta, new ItemStack(outer));
	}

	@Override
	public int getNumberProduced() {
		return 64;
	}

}
