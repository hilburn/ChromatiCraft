/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.TileEntity.Networking;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.api.wands.IWandable;
import Reika.ChromatiCraft.ChromatiCraft;
import Reika.ChromatiCraft.Auxiliary.ChromaOverlays;
import Reika.ChromatiCraft.Auxiliary.ChromaStructures;
import Reika.ChromatiCraft.Auxiliary.ProgressionManager.ProgressStage;
import Reika.ChromatiCraft.Base.TileEntity.CrystalTransmitterBase;
import Reika.ChromatiCraft.Magic.CrystalPotionController;
import Reika.ChromatiCraft.Magic.ElementTagCompound;
import Reika.ChromatiCraft.Magic.Interfaces.CrystalReceiver;
import Reika.ChromatiCraft.Magic.Interfaces.CrystalRepeater;
import Reika.ChromatiCraft.Magic.Interfaces.CrystalSource;
import Reika.ChromatiCraft.Magic.Interfaces.CrystalTransmitter;
import Reika.ChromatiCraft.ModInterface.ChromaAspectManager;
import Reika.ChromatiCraft.Registry.ChromaBlocks;
import Reika.ChromatiCraft.Registry.ChromaOptions;
import Reika.ChromatiCraft.Registry.ChromaPackets;
import Reika.ChromatiCraft.Registry.ChromaSounds;
import Reika.ChromatiCraft.Registry.ChromaTiles;
import Reika.ChromatiCraft.Registry.Chromabilities;
import Reika.ChromatiCraft.Registry.CrystalElement;
import Reika.ChromatiCraft.Render.Particle.EntityBallLightning;
import Reika.ChromatiCraft.Render.Particle.EntityBlurFX;
import Reika.ChromatiCraft.Render.Particle.EntityFlareFX;
import Reika.ChromatiCraft.Render.Particle.EntityRuneFX;
import Reika.ChromatiCraft.World.PylonGenerator;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.APIStripper.Strippable;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.BlockArray;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.FilledBlockArray;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.IO.ReikaColorAPI;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.ModInteract.DeepInteract.ReikaThaumHelper;
import Reika.DragonAPI.ModInteract.ItemHandlers.BloodMagicHandler;
import WayofTime.alchemicalWizardry.api.soulNetwork.SoulNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
//Make player able to manufacture in the very late game, otherwise rare worldgen
@Strippable(value = {"thaumcraft.api.nodes.INode", "thaumcraft.api.wands.IWandable"})
public class TileEntityCrystalPylon extends CrystalTransmitterBase implements CrystalSource, INode, IWandable {

	private boolean hasMultiblock = false;
	private boolean enhanced = false;
	private CrystalElement color = CrystalElement.WHITE;
	public int randomOffset = rand.nextInt(360);
	public static final int MAX_ENERGY = 180000;
	public static final int MAX_ENERGY_ENHANCED = 900000;
	private int energy = MAX_ENERGY;
	private int energyStep = 1;

	public static final int RANGE = 48;

	private static Class node;
	private static HashMap<String, ArrayList<Integer>> nodeCache;

	static {
		if (ModList.THAUMCRAFT.isLoaded()) {
			try {
				node = Class.forName("thaumcraft.common.tiles.TileNode");
				Field f = node.getDeclaredField("locations");
				f.setAccessible(true);
				nodeCache = (HashMap<String, ArrayList<Integer>>)f.get(null);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public ChromaTiles getTile() {
		return ChromaTiles.PYLON;
	}

	@Override
	public boolean isConductingElement(CrystalElement e) {
		return e == color;
	}

	@Override
	public boolean needsLineOfSight() {
		return true;
	}

	public CrystalElement getColor() {
		return color;
	}

	public int getEnergy(CrystalElement e) {
		return e == color ? energy : 0;
	}

	public int getRenderColor() {
		return ReikaColorAPI.mixColors(color.getColor(), 0x888888, (float)energy/this.getMaxStorage(color));
	}

	@Override
	protected void onFirstTick(World world, int x, int y, int z) {
		super.onFirstTick(world, x, y, z);
		PylonGenerator.instance.cachePylon(this);
		if (ModList.THAUMCRAFT.isLoaded() && nodeCache != null) {
			ArrayList li = new ArrayList();
			li.add(world.provider.dimensionId);
			li.add(x);
			li.add(y);
			li.add(z);
			nodeCache.put(this.getId(), li);
		}
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		super.updateEntity(world, x, y, z, meta);

		if (DragonAPICore.debugtest) {
			if (!hasMultiblock) {
				CrystalElement e = CrystalElement.randomElement();
				FilledBlockArray b = ChromaStructures.getPylonStructure(world, x, y, z, e);
				b.place();
				world.setBlock(x, y+9, z, this.getTile().getBlock(), this.getTile().getBlockMetadata(), 3);
				TileEntityCrystalPylon te = (TileEntityCrystalPylon)world.getTileEntity(x, y+9, z);
				te.color = e;
				te.hasMultiblock = true;
				te.syncAllData(true);
			}
		}

		if (hasMultiblock) {
			//ReikaJavaLibrary.pConsole(energy, Side.SERVER, color == CrystalElement.BLUE);

			int max = this.getMaxStorage(color);
			this.charge(world, x, y, z, max);
			energy = Math.min(energy, max);

			if (world.isRemote) {
				this.spawnParticle(world, x, y, z);
			}

			if (!world.isRemote && rand.nextInt(80) == 0) {
				int r = 8+rand.nextInt(8);
				AxisAlignedBB box = ReikaAABBHelper.getBlockAABB(x, y, z).expand(r, r, r);
				List<EntityLivingBase> li = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
				for (EntityLivingBase e : li) {
					boolean attack = !e.isDead && e.getHealth() > 0;
					if (e instanceof EntityPlayer) {
						EntityPlayer ep = (EntityPlayer)e;
						attack = attack && !ep.capabilities.isCreativeMode && !Chromabilities.PYLON.enabledOn(ep);
					}
					if (attack) {
						this.attackEntity(e);
						this.sendClientAttack(this, e);
					}
				}
			}

			if (this.getTicksExisted()%72 == 0) {
				ChromaSounds.POWER.playSoundAtBlock(this);
			}

			if (world.isRemote && rand.nextInt(36) == 0) {
				this.spawnLightning(world, x, y, z);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	private void spawnLightning(World world, int x, int y, int z) {
		EntityBallLightning e = new EntityBallLightning(world, x+0.5, y+0.5, z+0.5, color);
		e.setVelocity(0.125, rand.nextInt(360), 0);
		Minecraft.getMinecraft().effectRenderer.addEffect(e);
	}

	private void charge(World world, int x, int y, int z, int max) {
		if (energy < max) {
			energy += energyStep;
		}
		if (energyStep > 1)
			energyStep--;

		int a = 1;
		if (energy <= max-a) {
			BlockArray blocks = this.getBoosterCrystals(world, x, y, z);
			int c = this.isEnhanced() ? 3 : 2;
			for (int i = 0; i < blocks.getSize(); i++) {
				energy += a;
				a *= c;
				if (i == 7) { //8 crystals
					energy += a*2;
				}
				if (energy >= max) {
					return;
				}
			}
			if (world.isRemote && !blocks.isEmpty())
				this.spawnRechargeParticles(world, x, y, z, blocks);
		}
	}

	public void speedRegenShortly() {
		energyStep = 5;
	}

	@SideOnly(Side.CLIENT)
	private void spawnRechargeParticles(World world, int x, int y, int z, BlockArray blocks) {
		for (int i = 0; i < blocks.getSize(); i++) {
			int[] xyz = blocks.getNthBlock(i);//blocks.getNthBlock(this.getTicksExisted()%blocks.getSize());
			int dx = xyz[0];
			int dy = xyz[1];
			int dz = xyz[2];
			double ddx = dx-x;
			double ddy = dy-y-0.25;
			double ddz = dz-z;
			double dd = ReikaMathLibrary.py3d(ddx, ddy, ddz);
			double v = 0.125;
			double vx = -v*ddx/dd;
			double vy = -v*ddy/dd;
			double vz = -v*ddz/dd;
			double px = dx+0.5;
			double py = dy+0.125;
			double pz = dz+0.5;
			//EntityRuneFX fx = new EntityRuneFX(world, dx+0.5, dy+0.5, dz+0.5, vx, vy, vz, color);
			float sc = (float)(2F+Math.sin(4*Math.toRadians(this.getTicksExisted()+i*90/blocks.getSize())));
			EntityBlurFX fx = new EntityBlurFX(color, world, px, py, pz, vx, vy, vz).setScale(sc).setLife(38).setNoSlowdown();
			//EntityLaserFX fx = new EntityLaserFX(color, world, px, py, pz, vx, vy, vz).setScale(3);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}

	private BlockArray getRuneLocations(World world, int x, int y, int z) {
		BlockArray blocks = new BlockArray();
		blocks.addBlockCoordinate(x-3, y-4, z-1);
		blocks.addBlockCoordinate(x-1, y-4, z-3);

		blocks.addBlockCoordinate(x+3, y-4, z-1);
		blocks.addBlockCoordinate(x+1, y-4, z-3);

		blocks.addBlockCoordinate(x-3, y-4, z+1);
		blocks.addBlockCoordinate(x-1, y-4, z+3);

		blocks.addBlockCoordinate(x+3, y-4, z+1);
		blocks.addBlockCoordinate(x+1, y-4, z+3);
		return blocks;
	}

	public BlockArray getBoosterCrystals(World world, int x, int y, int z) {
		BlockArray blocks = new BlockArray();
		Block b = ChromaTiles.CRYSTAL.getBlock();
		int meta = ChromaTiles.CRYSTAL.getBlockMetadata();
		blocks.addBlockCoordinateIf(world, x-3, y-3, z-1, b, meta);
		blocks.addBlockCoordinateIf(world, x-1, y-3, z-3, b, meta);

		blocks.addBlockCoordinateIf(world, x+3, y-3, z-1, b, meta);
		blocks.addBlockCoordinateIf(world, x+1, y-3, z-3, b, meta);

		blocks.addBlockCoordinateIf(world, x-3, y-3, z+1, b, meta);
		blocks.addBlockCoordinateIf(world, x-1, y-3, z+3, b, meta);

		blocks.addBlockCoordinateIf(world, x+3, y-3, z+1, b, meta);
		blocks.addBlockCoordinateIf(world, x+1, y-3, z+3, b, meta);
		return blocks;
	}

	@SideOnly(Side.CLIENT)
	public void particleAttack(int sx, int sy, int sz, int x, int y, int z) {
		int n = 8+rand.nextInt(24);
		for (int i = 0; i < n; i++) {
			float rx = sx+rand.nextFloat();
			float ry = sy+rand.nextFloat();
			float rz = sz+rand.nextFloat();
			double dx = x-sx;
			double dy = y-sy;
			double dz = z-sz;
			double dd = ReikaMathLibrary.py3d(dx, dy, dz);
			double vx = 2*dx/dd;
			double vy = 2*dy/dd;
			double vz = 2*dz/dd;
			EntityFlareFX f = new EntityFlareFX(color, worldObj, rx, ry, rz, vx, vy, vz).setNoGravity();
			Minecraft.getMinecraft().effectRenderer.addEffect(f);
		}

		ChromaOverlays.instance.triggerPylonEffect(color);
	}

	void attackEntityByProxy(EntityPlayer player, CrystalRepeater te) {
		this.attackEntity(player);
		this.sendClientAttack(te, player);
	}

	void attackEntity(EntityLivingBase e) {
		ChromaSounds.DISCHARGE.playSoundAtBlock(this);
		ChromaSounds.DISCHARGE.playSound(worldObj, e.posX, e.posY, e.posZ, 1, 1);

		int amt = 5;

		if (e instanceof EntityPlayer) {
			EntityPlayer ep = (EntityPlayer)e;
			ProgressStage.SHOCK.stepPlayerTo(ep);
			if (ModList.BLOODMAGIC.isLoaded()) {
				int drain = 5000;
				if (BloodMagicHandler.getInstance().isPlayerWearingFullBoundArmor(ep)) {
					amt *= 10; //counter the 90% reduction
					drain = 150000;
				}
				SoulNetworkHandler.syphonFromNetwork(ep.getCommandSenderName(), drain);
			}
		}

		e.attackEntityFrom(ChromatiCraft.pylon, amt);
		PotionEffect eff = CrystalPotionController.getEffectFromColor(color, 200, 2);
		if (eff != null) {
			e.addPotionEffect(eff);
		}
	}

	private void sendClientAttack(CrystalTransmitter te, EntityLivingBase e) {
		int tx = te.getX();
		int ty = te.getY();
		int tz = te.getZ();
		int x = MathHelper.floor_double(e.posX);
		int y = MathHelper.floor_double(e.posY)+1;
		int z = MathHelper.floor_double(e.posZ);
		ReikaPacketHelper.sendDataPacket(ChromatiCraft.packetChannel, ChromaPackets.PYLONATTACK.ordinal(), this, tx, ty, tz, x, y, z);
	}

	public void invalidateMultiblock() {
		if (hasMultiblock) {
			ChromaSounds.POWERDOWN.playSoundAtBlock(this);
			ChromaSounds.POWERDOWN.playSound(worldObj, xCoord, yCoord, zCoord, 1F, 2F);
			ChromaSounds.POWERDOWN.playSound(worldObj, xCoord, yCoord, zCoord, 1F, 0.5F);

			if (worldObj.isRemote)
				this.invalidatationParticles();
		}
		hasMultiblock = false;
		this.clearTargets(false);
		energy = 0;
		this.syncAllData(true);
	}

	@SideOnly(Side.CLIENT)
	private void invalidatationParticles() {
		double d = 1.25;
		int n = 64+rand.nextInt(64);
		for (int i = 0; i < n; i++) {
			double rx = ReikaRandomHelper.getRandomPlusMinus(xCoord+0.5, d);
			double ry = ReikaRandomHelper.getRandomPlusMinus(yCoord+0.5, d);
			double rz = ReikaRandomHelper.getRandomPlusMinus(zCoord+0.5, d);
			double vx = rand.nextDouble()-0.5;
			double vy = rand.nextDouble()-0.5;
			double vz = rand.nextDouble()-0.5;
			EntityRuneFX fx = new EntityRuneFX(worldObj, rx, ry, rz, vx, vy, vz, color);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}

	public void validateMultiblock() {
		hasMultiblock = true;
		this.syncAllData(true);
	}

	@SideOnly(Side.CLIENT)
	private void spawnParticle(World world, int x, int y, int z) {
		double d = 1.25;
		double rx = ReikaRandomHelper.getRandomPlusMinus(x+0.5, d);
		double ry = ReikaRandomHelper.getRandomPlusMinus(y+0.5, d);
		double rz = ReikaRandomHelper.getRandomPlusMinus(z+0.5, d);
		EntityFlareFX fx = new EntityFlareFX(color, world, rx, ry, rz);
		Minecraft.getMinecraft().effectRenderer.addEffect(fx);
	}

	@Override
	protected void readSyncTag(NBTTagCompound NBT) {
		super.readSyncTag(NBT);

		color = CrystalElement.elements[NBT.getInteger("color")];
		hasMultiblock = NBT.getBoolean("multi");
		energy = NBT.getInteger("energy");
		enhanced = NBT.getBoolean("enhance");
	}

	@Override
	protected void writeSyncTag(NBTTagCompound NBT) {
		super.writeSyncTag(NBT);

		NBT.setInteger("color", color.ordinal());
		NBT.setBoolean("multi", hasMultiblock);
		NBT.setInteger("energy", energy);
		NBT.setBoolean("enhance", enhanced);
	}

	@Override
	public int getSendRange() {
		return RANGE;
	}

	@Override
	public boolean canConduct() {
		return hasMultiblock && energy >= 5000;
	}

	@Override
	public int maxThroughput() {
		return this.isEnhanced() ? 15000 : 5000;
	}

	@Override
	public int getTransmissionStrength() {
		return this.isEnhanced() ? 50000 : 10000;
	}

	public void generateColor(CrystalElement e) {
		color = e;
	}

	public void setColor(CrystalElement e) {
		if (worldObj.isRemote)
			return;
		color = e;
		BlockArray runes = this.getRuneLocations(worldObj, xCoord, yCoord, zCoord);
		for (int i = 0; i < runes.getSize(); i++) {
			int[] xyz = runes.getNthBlock(i);
			if (worldObj.getBlock(xyz[0], xyz[1], xyz[2]) == ChromaBlocks.RUNE.getBlockInstance())
				worldObj.setBlockMetadataWithNotify(xyz[0], xyz[1], xyz[2], color.ordinal(), 3);
		}
	}

	@Override
	public boolean drain(CrystalElement e, int amt) {
		if (e == color && energy >= amt) {
			energy -= amt;
			return true;
		}
		return false;
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public AspectList getAspects() {
		AspectList as = new AspectList();
		as.add(Aspect.AURA, 400);
		ArrayList<Aspect> li = ChromaAspectManager.instance.getAspects(this.getColor());
		for (Aspect a : li) {
			as.add(a, 400);
		}
		return as;
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public void setAspects(AspectList aspects) {}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public boolean doesContainerAccept(Aspect tag) {
		return this.getAspects().getAmount(tag) > 0;
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public int addToContainer(Aspect tag, int amount) {return 0;}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public boolean takeFromContainer(Aspect tag, int amount) {
		return this.doesContainerContainAmount(tag, amount);
	}

	@Override
	@Deprecated
	@ModDependent(ModList.THAUMCRAFT)
	public boolean takeFromContainer(AspectList ot) {
		return false;
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public boolean doesContainerContainAmount(Aspect tag, int amount) {
		return this.getAspects().getAmount(tag) > amount;
	}

	@Override
	@Deprecated
	@ModDependent(ModList.THAUMCRAFT)
	public boolean doesContainerContain(AspectList ot) {
		return false;
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public int containerContains(Aspect tag) {
		return this.getAspects().getAmount(tag);
	}

	@Override
	public String getId() { //Normally based on world coords, but uses just color to make each node color scannable once
		return "Pylon_"+color.toString();//"Pylon_"+worldObj.provider.dimensionId+":"+xCoord+":"+yCoord+":"+zCoord;
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public AspectList getAspectsBase() {
		return this.getAspects();
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public NodeType getNodeType() {
		switch(color) {
		case BLACK:
			return NodeType.DARK;
		case GRAY:
			return NodeType.UNSTABLE;
		case WHITE:
			return NodeType.PURE;
		default:
			return NodeType.NORMAL;
		}
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public void setNodeType(NodeType nodeType) {}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public void setNodeModifier(NodeModifier nodeModifier) {}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public NodeModifier getNodeModifier() {
		return NodeModifier.BRIGHT;
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public int getNodeVisBase(Aspect aspect) {
		return this.containerContains(aspect);
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public void setNodeVisBase(Aspect aspect, short nodeVisBase) {}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int mode) {
		return -1;
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public ItemStack onWandRightClick(World world, ItemStack wandstack, EntityPlayer player) {
		player.setItemInUse(wandstack, Integer.MAX_VALUE);
		ReikaThaumHelper.setWandInUse(wandstack, this);
		return wandstack;
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public void onUsingWandTick(ItemStack wandstack, EntityPlayer player, int count) {
		if (!worldObj.isRemote && this.canConduct() && player.ticksExisted%5 == 0) {
			if (!ChromaOptions.HARDTHAUM.getState() || ReikaThaumHelper.isResearchComplete(player, "NODETAPPER2")) {
				AspectList al = ReikaThaumHelper.decompose(this.getAspects());
				for (Aspect a : al.aspects.keySet()) {
					int amt = 2;
					if (ReikaThaumHelper.isResearchComplete(player, "NODETAPPER1"))
						amt *= 2;
					if (ReikaThaumHelper.isResearchComplete(player, "NODETAPPER2"))
						amt *= 2;
					amt = Math.min(amt, al.getAmount(a));
					amt = Math.min(amt, ReikaThaumHelper.getWandSpaceFor(wandstack, a));
					int ret = ReikaThaumHelper.addVisToWand(wandstack, a, amt);
					if (ret > 0) {
						energy = Math.max(0, energy-ret*8);
					}
				}
			}
		}
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public void onWandStoppedUsing(ItemStack wandstack, World world, EntityPlayer player, int count) {

	}

	public final ElementTagCompound getEnergy() {
		ElementTagCompound tag = new ElementTagCompound();
		tag.setTag(color, energy);
		return tag;
	}

	@Override
	public int getMaxStorage(CrystalElement e) {
		return this.isEnhanced() ? MAX_ENERGY_ENHANCED : MAX_ENERGY;
	}

	public boolean isEnhanced() {
		return enhanced && this.canConduct();
	}

	@Override
	public int getSourcePriority() {
		return 0;
	}

	@Override
	public boolean canTransmitTo(CrystalReceiver te) {
		return true;
	}

}
