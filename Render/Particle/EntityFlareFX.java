/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.Render.Particle;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import Reika.ChromatiCraft.Registry.ChromaIcons;
import Reika.ChromatiCraft.Registry.CrystalElement;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Java.ReikaGLHelper.BlendMode;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityFlareFX extends EntityFX {

	private final CrystalElement color;

	public EntityFlareFX(CrystalElement e, World world, double x, double y, double z) {
		super(world, x, y, z);
		particleMaxAge = 30+rand.nextInt(30);
		noClip = true;
		particleIcon = ChromaIcons.FLARE.getIcon();
		particleScale = 3F;
		color = e;
		particleGravity = rand.nextInt(3) == 0 ? 0.6F : -0.6F;
	}

	public EntityFlareFX(CrystalElement e, World world, double x, double y, double z, double vx, double vy, double vz) {
		this(e, world, x, y, z);
		motionX = vx;
		motionY = vy;
		motionZ = vz;
	}

	public EntityFlareFX(CrystalElement e, World world, WorldLocation start, WorldLocation target, float rx, float ry, float rz) {
		this(e, world, start.xCoord+0.5+rx, start.yCoord+0.5+ry, start.zCoord+0.5+rz);
		double dd = target.getDistanceTo(start);
		motionX = (target.xCoord-start.xCoord)/dd/2;
		motionY = (target.yCoord-start.yCoord)/dd/2;
		motionZ = (target.zCoord-start.zCoord)/dd/2;
		particleMaxAge = (int)(dd*2.8);
	}

	public EntityFlareFX setNoGravity() {
		particleGravity = 0;
		return this;
	}

	@Override
	public void renderParticle(Tessellator v5, float par2, float par3, float par4, float par5, float par6, float par7)
	{
		v5.draw();
		ReikaTextureHelper.bindTerrainTexture();
		BlendMode.ADDITIVEDARK.apply();
		GL11.glColor4f(1, 1, 1, 1);
		v5.startDrawingQuads();
		v5.setBrightness(this.getBrightnessForRender(0));
		super.renderParticle(v5, par2, par3, par4, par5, par6, par7);
		v5.draw();
		BlendMode.DEFAULT.apply();
		v5.startDrawingQuads();
	}

	@Override
	public int getBrightnessForRender(float par1)
	{
		return 240;
	}

	@Override
	public int getFXLayer()
	{
		return 2;
	}


}
