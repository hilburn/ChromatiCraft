/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.Magic;

import java.util.HashMap;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import Reika.ChromatiCraft.ChromatiCraft;
import Reika.ChromatiCraft.Registry.ChromaPackets;
import Reika.ChromatiCraft.Registry.ChromaSounds;
import Reika.ChromatiCraft.Registry.CrystalElement;
import Reika.DragonAPI.Command.DragonCommandBase;
import Reika.DragonAPI.Libraries.ReikaPlayerAPI;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;


public class PlayerElementBuffer {

	public static final PlayerElementBuffer instance = new PlayerElementBuffer();

	private final HashMap<EntityPlayer, Integer> recentUpgrades = new HashMap();

	private static final String NBT_TAG = "CrystalBuffer";

	private PlayerElementBuffer() {

	}

	public float getAndDecrUpgradeTick(EntityPlayer ep) {
		Integer val = recentUpgrades.get(ep);
		if (val == null)
			return 0;
		else {
			int tick = val.intValue();
			tick--;
			if (tick == 0) {
				recentUpgrades.remove(ep);
			}
			else {
				recentUpgrades.put(ep, tick);
			}
			return tick/2000F;
		}
	}

	private NBTTagCompound getTag(EntityPlayer ep) {
		NBTTagCompound tag = ep.getEntityData().getCompoundTag(NBT_TAG);
		ep.getEntityData().setTag(NBT_TAG, tag);
		return tag;
	}

	public ElementTagCompound getPlayerBuffer(EntityPlayer ep) {
		NBTTagCompound tag = this.getTag(ep);
		return ElementTagCompound.createFromNBT(tag);
	}

	public int getPlayerContent(EntityPlayer ep, CrystalElement e) {
		NBTTagCompound tag = this.getTag(ep);
		return tag.getInteger(e.name());
	}

	public boolean playerHas(EntityPlayer ep, CrystalElement e, int amt) {
		return this.getPlayerContent(ep, e) >= amt;
	}

	public boolean playerHas(EntityPlayer player, ElementTagCompound tag) {
		for (CrystalElement e : tag.elementSet()) {
			int amt = tag.getValue(e);
			if (!this.playerHas(player, e, amt))
				return false;
		}
		return true;
	}

	public boolean addToPlayer(EntityPlayer ep, CrystalElement e, int amt) {
		NBTTagCompound tag = this.getTag(ep);
		int has = tag.getInteger(e.name());
		int val = Math.min(has+amt, this.getElementCap(ep));
		tag.setInteger(e.name(), val);
		return val > has;
	}

	private void setToPlayer(EntityPlayer ep, CrystalElement e, int amt) {
		NBTTagCompound tag = this.getTag(ep);
		tag.setInteger(e.name(), amt);
	}

	public boolean addToPlayer(EntityPlayer ep, ElementTagCompound tag) {
		boolean flag = false;
		for (CrystalElement e : tag.elementSet()) {
			flag |= this.addToPlayer(ep, e, tag.getValue(e));
		}
		return flag;
	}

	public void removeFromPlayer(EntityPlayer ep, CrystalElement e, int amt) {
		if (ep.capabilities.isCreativeMode)
			return;
		NBTTagCompound tag = this.getTag(ep);
		int has = tag.getInteger(e.name());
		has -= amt;
		tag.setInteger(e.name(), Math.max(0, has));
	}

	public void removeFromPlayer(EntityPlayer player, ElementTagCompound tag) {
		for (CrystalElement e : tag.elementSet()) {
			int amt = tag.getValue(e);
			this.removeFromPlayer(player, e, amt);
		}
	}

	public int getElementCap(EntityPlayer ep) {
		NBTTagCompound tag = this.getTag(ep);
		return Math.max(24, tag.getInteger("cap"));
	}

	public int getChargeSpeed(EntityPlayer ep) {
		return (int)Math.pow(this.getElementCap(ep)/24, 0.667);
	}

	public double getPlayerFraction(EntityPlayer ep, CrystalElement e) {
		return (double)this.getPlayerContent(ep, e)/this.getElementCap(ep);
	}

	public boolean upgradeCap(EntityPlayer ep) {
		NBTTagCompound tag = this.getTag(ep);
		int prev = this.getElementCap(ep);
		int val = Math.min(prev*4, 400000);
		tag.setInteger("cap", val);
		boolean flag = val > prev;
		if (flag) {
			ChromaSounds.CRAFTDONE.playSound(ep.worldObj, ep.posX, ep.posY, ep.posZ, 1, 1);
			recentUpgrades.put(ep, 2000);
			if (ep instanceof EntityPlayerMP)
				this.sendUpgradePacket((EntityPlayerMP)ep);
		}
		return flag;
	}

	private void sendUpgradePacket(EntityPlayerMP ep) {
		ReikaPacketHelper.sendDataPacket(ChromatiCraft.packetChannel, ChromaPackets.BUFFERINC.ordinal(), ep, 0);
	}

	public void upgradePlayerOnClient(EntityPlayer ep) {
		recentUpgrades.put(ep, 2000);
	}

	public boolean canPlayerAccept(EntityPlayer ep, CrystalElement e, int amt) {
		return this.getPlayerContent(ep, e)+amt <= this.getElementCap(ep);
	}

	public boolean isMaxed(EntityPlayer player, CrystalElement e) {
		return this.getPlayerContent(player, e) == this.getElementCap(player);
	}

	public boolean isMaxedWithin(EntityPlayer player, CrystalElement e, float frac) {
		return this.getPlayerContent(player, e) >= this.getElementCap(player)*(1-frac);
	}

	public boolean checkUpgrade(EntityPlayer player, boolean doUpgrade) {
		for (int i = 0; i < CrystalElement.elements.length; i++) {
			CrystalElement e = CrystalElement.elements[i];
			if (!this.isMaxedWithin(player, e, 0.1F))
				return false;
		}
		return doUpgrade ? this.upgradeCap(player) : true;
	}

	public boolean hasElement(EntityPlayer ep, CrystalElement e) {
		return this.getPlayerContent(ep, e) > 0;
	}

	public int getPlayerTotalEnergy(EntityPlayer ep) {
		int sum = 0;
		for (int i = 0; i < CrystalElement.elements.length; i++) {
			CrystalElement e = CrystalElement.elements[i];
			sum += this.getPlayerContent(ep, e);
		}
		return sum;
	}

	public static class PlayerEnergyCommand extends DragonCommandBase {

		@Override
		public void processCommand(ICommandSender ics, String[] args) {
			EntityPlayer sender = this.getCommandSenderAsPlayer(ics);
			EntityPlayer tg = args.length == 2 ? sender : sender.worldObj.getPlayerEntityByName(args[0]);
			String es = args[args.length-2].toUpperCase();
			CrystalElement[] en = es.equals("ALL") ? CrystalElement.elements : new CrystalElement[]{CrystalElement.valueOf(es)};
			int amt = Integer.parseInt(args[args.length-1]);
			for (CrystalElement e : en)
				instance.setToPlayer(tg, e, amt);
			if (tg instanceof EntityPlayerMP)
				ReikaPlayerAPI.syncCustomData((EntityPlayerMP)tg);
		}

		@Override
		public String getCommandString() {
			return "chromabuffer";
		}

		@Override
		protected boolean isAdminOnly() {
			return true;
		}

	}

}
