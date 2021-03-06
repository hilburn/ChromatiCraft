/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import Reika.ChromatiCraft.API.AbilityAPI.Ability;
import Reika.ChromatiCraft.Auxiliary.AbilityHelper;
import Reika.ChromatiCraft.Auxiliary.ChromaFX;
import Reika.ChromatiCraft.Auxiliary.ProgressionManager;
import Reika.ChromatiCraft.Auxiliary.ProgressionManager.ProgressStage;
import Reika.ChromatiCraft.Base.CrystalBlock;
import Reika.ChromatiCraft.Block.BlockEnderTNT.TileEntityEnderTNT;
import Reika.ChromatiCraft.Block.BlockPowerTree;
import Reika.ChromatiCraft.Block.BlockRangeLamp.TileEntityRangedLamp;
import Reika.ChromatiCraft.Container.ContainerBookPages;
import Reika.ChromatiCraft.Items.ItemCrystalShard;
import Reika.ChromatiCraft.Items.ItemInfoFragment;
import Reika.ChromatiCraft.Items.Tools.ItemChromaBook;
import Reika.ChromatiCraft.Magic.PlayerElementBuffer;
import Reika.ChromatiCraft.ModInterface.TileEntityAspectFormer;
import Reika.ChromatiCraft.Registry.ChromaPackets;
import Reika.ChromatiCraft.Registry.ChromaResearch;
import Reika.ChromatiCraft.Registry.ChromaResearchManager;
import Reika.ChromatiCraft.Registry.ChromaSounds;
import Reika.ChromatiCraft.Registry.Chromabilities;
import Reika.ChromatiCraft.Registry.CrystalElement;
import Reika.ChromatiCraft.TileEntity.AOE.TileEntityLampController;
import Reika.ChromatiCraft.TileEntity.Acquisition.TileEntityTeleportationPump;
import Reika.ChromatiCraft.TileEntity.Networking.TileEntityCrystalPylon;
import Reika.ChromatiCraft.TileEntity.Plants.TileEntityCrystalPlant;
import Reika.ChromatiCraft.TileEntity.Processing.TileEntityAutoEnchanter;
import Reika.ChromatiCraft.TileEntity.Processing.TileEntityInventoryTicker;
import Reika.ChromatiCraft.TileEntity.Processing.TileEntitySpawnerReprogrammer;
import Reika.ChromatiCraft.TileEntity.Recipe.TileEntityRitualTable;
import Reika.ChromatiCraft.World.PylonGenerator;
import Reika.DragonAPI.Auxiliary.PacketTypes;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Interfaces.IPacketHandler;
import Reika.DragonAPI.Libraries.ReikaInventoryHelper;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper.PacketObj;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;

public class ChromatiPackets implements IPacketHandler {

	protected ChromaPackets pack;

	public void handleData(PacketObj packet, World world, EntityPlayer ep) {
		DataInputStream inputStream = packet.getDataIn();
		int control = Integer.MIN_VALUE;
		int len;
		int[] data = new int[0];
		int x = 0;
		int y = 0;
		int z = 0;
		String stringdata = null;
		UUID id = null;
		//System.out.print(packet.length);
		try {
			PacketTypes packetType = packet.getType();
			switch(packetType) {
			case SOUND:
				control = inputStream.readInt();
				ChromaSounds s = ChromaSounds.soundList[control];
				double sx = inputStream.readDouble();
				double sy = inputStream.readDouble();
				double sz = inputStream.readDouble();
				float v = inputStream.readFloat();
				float p = inputStream.readFloat();
				ReikaSoundHelper.playClientSound(s, sx, sy, sz, v, p);
				return;
			case STRING:
				stringdata = packet.readString();
				control = inputStream.readInt();
				pack = ChromaPackets.getPacket(control);
				break;
			case DATA:
				control = inputStream.readInt();
				pack = ChromaPackets.getPacket(control);
				len = pack.numInts;
				if (pack.hasData()) {
					data = new int[len];
					for (int i = 0; i < len; i++)
						data[i] = inputStream.readInt();
				}
				break;
			case UPDATE:
				control = inputStream.readInt();
				pack = ChromaPackets.getPacket(control);
				break;
			case FLOAT:
				break;
			case SYNC:
				String name = packet.readString();
				x = inputStream.readInt();
				y = inputStream.readInt();
				z = inputStream.readInt();
				int value = inputStream.readInt();
				ReikaPacketHelper.updateTileEntityData(world, x, y, z, name, value);
				return;
			case TANK:
				String tank = packet.readString();
				x = inputStream.readInt();
				y = inputStream.readInt();
				z = inputStream.readInt();
				int level = inputStream.readInt();
				ReikaPacketHelper.updateTileEntityTankData(world, x, y, z, tank, level);
				return;
			case RAW:
				control = inputStream.readInt();
				pack = ChromaPackets.getPacket(control);
				len = 1;
				data = new int[len];
				for (int i = 0; i < len; i++)
					data[i] = inputStream.readInt();
				break;
			case PREFIXED:
				control = inputStream.readInt();
				pack = ChromaPackets.getPacket(control);
				len = inputStream.readInt();
				data = new int[len];
				for (int i = 0; i < len; i++)
					data[i] = inputStream.readInt();
				break;
			case NBT:
				break;
			case STRINGINT:
				stringdata = packet.readString();
				control = inputStream.readInt();
				pack = ChromaPackets.getPacket(control);
				data = new int[pack.numInts];
				for (int i = 0; i < data.length; i++)
					data[i] = inputStream.readInt();
				break;
			case UUID:
				control = inputStream.readInt();
				pack = ChromaPackets.getPacket(control);
				long l1 = inputStream.readLong(); //most
				long l2 = inputStream.readLong(); //least
				id = new UUID(l1, l2);
				break;
			}
			if (packetType.hasCoordinates()) {
				x = inputStream.readInt();
				y = inputStream.readInt();
				z = inputStream.readInt();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
		TileEntity tile = world.getTileEntity(x, y, z);
		try {
			switch (pack) {
			case ENCHANTER: {
				Enchantment e = Enchantment.enchantmentsList[data[0]];
				boolean incr = data[1] > 0;
				TileEntityAutoEnchanter ench = (TileEntityAutoEnchanter)tile;
				if (incr) {
					ench.incrementEnchantment(e);
				}
				else {
					ench.decrementEnchantment(e);
				}
				break;
			}
			case SPAWNERPROGRAM:
				TileEntitySpawnerReprogrammer prog = (TileEntitySpawnerReprogrammer)tile;
				prog.setMobType(stringdata);
				break;
			case CRYSTALEFFECT:
				Block b = world.getBlock(x, y, z);
				if (b instanceof CrystalBlock) {
					CrystalBlock cb = (CrystalBlock)b;
					cb.updateEffects(world, x, y, z);
				}
				break;
			case PLANTUPDATE:
				TileEntityCrystalPlant te = (TileEntityCrystalPlant)tile;
				te.updateLight();
				break;
			case ABILITY: {
				Ability c = Chromabilities.getAbilityByInt(data[0]);
				if (Chromabilities.playerHasAbility(ep, c))
					Chromabilities.triggerAbility(ep, c, data[1]);
				break;
			}
			case PYLONATTACK:
				if (tile instanceof TileEntityCrystalPylon)
					((TileEntityCrystalPylon)tile).particleAttack(data[0], data[1], data[2], data[3], data[4], data[5]);
				break;
			case ABILITYCHOOSE:
				((TileEntityRitualTable)tile).setChosenAbility(Chromabilities.getAbilityByInt(data[0]));
				break;
			case BUFFERINC:
				PlayerElementBuffer.instance.upgradePlayerOnClient(ep);
				break;
			case TELEPUMP:
				((TileEntityTeleportationPump)tile).setTargetedFluid(data[0]);
				break;
				//case TRANSMIT:
				//((TileEntityFiberTransmitter)tile).transmitParticle(ForgeDirection.VALID_DIRECTIONS[data[0]], data[1], CrystalElement.elements[data[2]]);
				//break;
			case ASPECT:
				((TileEntityAspectFormer)tile).selectAspect(stringdata);
				break;
			case LAMPCHANNEL:
				((TileEntityRangedLamp)tile).setChannel(data[0]);
				break;
			case LAMPCONTROL:
				int mode = data[0];
				switch(data[0]) {
				case 0:
					((TileEntityLampController)tile).setChannel(data[1]);
					break;
				case 1:
					((TileEntityLampController)tile).incrementMode();
					break;
				case 2:
					((TileEntityLampController)tile).toggleState();
					break;
				}
			case TNT:
				((TileEntityEnderTNT)tile).setTarget(ep, data[0], data[1], data[2], data[3]);
				break;
			case BOOKINVSCROLL:
				((ContainerBookPages)ep.openContainer).scroll(data[0] > 0);
				break;
			case TICKER:
				((TileEntityInventoryTicker)tile).ticks = data[0];
				break;
			case PYLONCLEAR:
				PylonGenerator.instance.clearDimension(data[0]);
				break;
			case SHARDBOOST: {
				Entity e = world.getEntityByID(data[0]);
				if (e instanceof EntityItem) {
					ItemCrystalShard.spawnEffects((EntityItem)e);
				}
				break;
			}/*
			case FRAGPROGRAM: {
				ChromaResearch r = ChromaResearch.researchList[data[0]];
				int slot = data[1];
				ItemStack is = ep.inventory.mainInventory[slot];
				ItemInfoFragment.programShardAndGiveData(is, ep);
				break;
			}*/
			case GIVERESEARCH: {
				ChromaResearch r = ChromaResearch.researchList[data[0]];
				ChromaResearchManager.instance.givePlayerFragment(ep, r);
				break;
			}
			case LEAFBREAK:
				BlockPowerTree.breakEffectsClient(world, x, y, z, CrystalElement.elements[data[0]]);
				break;
			case GIVEPROGRESS: {
				ProgressStage p = ProgressStage.list[data[0]];
				ProgressionManager.instance.setPlayerStageClient(ep, p, data[1] > 0);
				break;
			}
			case HEALTHSYNC:
				Chromabilities.setHealthClient(ep, data[0]);
				break;
			case INVCYCLE:
				AbilityHelper.instance.cycleInventoryClient(ep, data[0] > 0);
				break;
			case RELAYCONNECT: {
				int num = (data.length-1);
				ArrayList<Coordinate> li = new ArrayList();
				for (int i = 0; i < num; i += 3) {
					Coordinate c = new Coordinate(data[i+0], data[i+1], data[i+2]);
					li.add(c);
				}
				CrystalElement e = CrystalElement.elements[data[data.length-1]];
				ChromaFX.spawnRelayParticle(e, li);
				break;
			}
			case RERESEARCH: {
				ChromaResearch r = ChromaResearch.researchList[data[0]];
				ReikaInventoryHelper.findAndDecrStack(Items.paper, -1, ep.inventory.mainInventory);
				ReikaInventoryHelper.findAndDecrStack(ReikaItemHelper.inksac, ep.inventory.mainInventory);
				ItemStack is = ItemInfoFragment.getItem(r);
				ItemChromaBook book = (ItemChromaBook)ep.getCurrentEquippedItem().getItem();
				ArrayList<ItemStack> li = book.getItemList(ep.getCurrentEquippedItem());
				li.add(is);
				book.setItems(ep.getCurrentEquippedItem(), li);
				break;
			}
			}
		}
		catch (NullPointerException e) {
			ChromatiCraft.logger.logError("TileEntity at "+x+", "+y+", "+z+" was deleted before its packet "+pack+" could be received!");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
