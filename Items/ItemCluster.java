/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.Items;

import net.minecraft.util.IIcon;
import Reika.ChromatiCraft.Base.ItemChromaMulti;
import Reika.ChromatiCraft.Registry.ChromaItems;

public class ItemCluster extends ItemChromaMulti {

	private final IIcon[] icons = new IIcon[this.getNumberTypes()];

	public ItemCluster(int tex) {
		super(tex);
		hasSubtypes = true;
	}

	@Override
	public int getNumberTypes() {
		return ChromaItems.CLUSTER.getNumberMetadatas();
	}
}
