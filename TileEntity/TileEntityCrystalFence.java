/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.TileEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import Reika.ChromatiCraft.Base.TileEntity.TileEntityChromaticBase;
import Reika.ChromatiCraft.Registry.ChromaTiles;
import Reika.DragonAPI.Instantiable.Data.Perimeter;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;

public class TileEntityCrystalFence extends TileEntityChromaticBase {

	private Perimeter fence = new Perimeter().disallowVertical();
	private HashMap<Integer, Integer> active = new HashMap();

	@Override
	public ChromaTiles getTile() {
		return ChromaTiles.FENCE;
	}

	public void removeCoordinate(World world, int x, int y, int z) {
		if (!world.isRemote) {
			if (fence.hasCoordinate(x, y, z))
				fence.clear();
			this.syncAllData(true);
		}
	}

	public void addCoordinate(World world, int x, int y, int z) {
		if (!world.isRemote) {
			fence.addPointBeforeLast(x, y, z);
			this.syncAllData(true);
		}
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		if (fence.isEmpty()) {
			fence.prependPoint(x, y, z);
			fence.appendPoint(x, y, z);
		}

		List<EntityLivingBase> li = this.getEntities();
		for (int i = 0; i < li.size(); i++) {
			EntityLivingBase e = li.get(i);
			boolean att = true;
			if (e instanceof EntityPlayer) {
				EntityPlayer ep = (EntityPlayer)e;
				if (ep == this.getPlacer())
					att = false;
				else if ("Reika_Kalseki".equals(ep.getCommandSenderName()))
					att = false;
			}
			if (att) {
				e.attackEntityFrom(DamageSource.cactus, 4);
				//e.knockBack(null, 0, 0, 0);
				double dx = e.posX-x-0.5;
				double dz = e.posZ-z-0.5;
				double dd = ReikaMathLibrary.py3d(dx, 0, dz);
				e.motionX = dx/dd;
				e.motionY = 0.5;
				e.motionZ = dz/dd;
				e.velocityChanged = true;
			}
		}

		for (Integer key : active.keySet()) {
			if (key != null) {
				int val = active.get(key);
				if (val > 0)
					val -= 8;
				active.put(key, val);
			}
		}
	}

	public int getSegmentAlpha(int i) {
		return active.containsKey(i) ? active.get(i) : 0;
	}

	public Perimeter getFence() {
		return fence.copy();
	}

	private List<EntityLivingBase> getEntities() {
		ArrayList<AxisAlignedBB> li = fence.getAABBs();
		ArrayList<EntityLivingBase> le = new ArrayList();
		for (int i = 0; i < li.size(); i++) {
			AxisAlignedBB aabb = li.get(i).expand(0, 255, 0);
			List<EntityLivingBase> ents = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
			le.addAll(ents);
			if (!ents.isEmpty())
				active.put(i, 512);
		}
		return le;
	}

	@Override
	protected void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	protected void writeSyncTag(NBTTagCompound NBT)
	{
		super.writeSyncTag(NBT);

		fence.writeToNBT("per", NBT);
		//ReikaJavaLibrary.pConsole("write: "+fence, Side.SERVER);
	}

	@Override
	protected void readSyncTag(NBTTagCompound NBT)
	{
		super.readSyncTag(NBT);

		fence.readFromNBT("per", NBT);
		fence.prependPoint(xCoord, yCoord, zCoord);
		fence.appendPoint(xCoord, yCoord, zCoord);
		//ReikaJavaLibrary.pConsole("read: "+fence, Side.SERVER);
	}

}