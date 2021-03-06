/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.TileEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import Reika.ChromatiCraft.ChromatiCraft;
import Reika.ChromatiCraft.API.BiomeBlacklist.BiomeConnection;
import Reika.ChromatiCraft.Base.ChromaDimensionBiome;
import Reika.ChromatiCraft.Base.TileEntity.TileEntityChromaticBase;
import Reika.ChromatiCraft.Registry.ChromaTiles;
import Reika.DragonAPI.Instantiable.Data.Maps.MultiMap;
import Reika.DragonAPI.Interfaces.GuiController;

public class TileEntityBiomeChanger extends TileEntityChromaticBase implements GuiController {

	private static final Collection<BiomeConnection> blacklist = new ArrayList();
	private static final MultiMap<BiomeGenBase, BiomeGenBase> availableBiomes = new MultiMap();

	@Override
	public ChromaTiles getTile() {
		return ChromaTiles.BIOMECHANGER;
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {

	}

	@Override
	protected void animateWithTick(World world, int x, int y, int z) {

	}

	private BiomeGenBase getChangedBiome(World world, int x, int z) {
		return null;
	}

	static {
		blacklist.add(new ChromaBiomeBlacklist());
	}

	public static void buildBiomeList() {
		availableBiomes.clear();
		for (int i = 0; i < BiomeGenBase.biomeList.length; i++) {
			BiomeGenBase b = BiomeGenBase.biomeList[i];
			if (b != null) {
				for (int k = 0; k < BiomeGenBase.biomeList.length; k++) {
					BiomeGenBase b2 = BiomeGenBase.biomeList[k];
					if (b2 != null) {
						if (isValid(b, b2))
							availableBiomes.addValue(b, b2);
					}
				}
			}
		}
	}

	private static boolean isValid(BiomeGenBase b, BiomeGenBase b2) {
		for (BiomeConnection bc : blacklist) {
			if (!bc.isLegalTransition(b, b2))
				return false;
		}
		return true;
	}

	public static void addBiomeConnection(BiomeConnection bc) {
		blacklist.add(bc);
	}

	public static Collection<BiomeGenBase> getValidBiomes(BiomeGenBase in) {
		return Collections.unmodifiableCollection(availableBiomes.get(in));
	}

	private static class ChromaBiomeBlacklist implements BiomeConnection {

		@Override
		public boolean isLegalTransition(BiomeGenBase in, BiomeGenBase out) {
			return this.isAccessibleBiome(in) && this.isAccessibleBiome(out);
		}

		private boolean isAccessibleBiome(BiomeGenBase in) {
			if (in == ChromatiCraft.rainbowforest)
				return false;
			if (in instanceof ChromaDimensionBiome)
				return false;
			return true;
		}



	}

}
