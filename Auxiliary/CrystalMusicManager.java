/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.Auxiliary;

import java.util.EnumMap;
import java.util.Random;

import Reika.ChromatiCraft.Magic.CrystalPotionController;
import Reika.ChromatiCraft.Registry.CrystalElement;
import Reika.DragonAPI.Libraries.MathSci.ReikaMusicHelper.MusicKey;

public class CrystalMusicManager {

	public static final CrystalMusicManager instance = new CrystalMusicManager();

	private final EnumMap<CrystalElement, MusicKey> baseKeys = new EnumMap(CrystalElement.class);

	private static final Random rand = new Random();

	private CrystalMusicManager() {
		baseKeys.put(CrystalElement.BLACK, MusicKey.C4);
		baseKeys.put(CrystalElement.BROWN, MusicKey.D4);
		baseKeys.put(CrystalElement.BLUE, MusicKey.E4);
		baseKeys.put(CrystalElement.GREEN, MusicKey.F4);
		baseKeys.put(CrystalElement.RED, MusicKey.G4);
		baseKeys.put(CrystalElement.PURPLE, MusicKey.A4);
		baseKeys.put(CrystalElement.MAGENTA, MusicKey.B4);
		baseKeys.put(CrystalElement.CYAN, MusicKey.A4/*C4*/);
		baseKeys.put(CrystalElement.LIGHTGRAY, MusicKey.D5);
		baseKeys.put(CrystalElement.GRAY, MusicKey.C5);
		baseKeys.put(CrystalElement.LIME, MusicKey.E5);
		baseKeys.put(CrystalElement.PINK, MusicKey.F5);
		baseKeys.put(CrystalElement.YELLOW, MusicKey.G5);
		baseKeys.put(CrystalElement.LIGHTBLUE, MusicKey.A5);
		baseKeys.put(CrystalElement.ORANGE, MusicKey.E4/*B5*/);
		baseKeys.put(CrystalElement.WHITE, MusicKey.C6);

		/*
		baseKeys.put(CrystalElement.BLACK, MusicKey.C4);
		baseKeys.put(CrystalElement.RED, MusicKey.G4);
		baseKeys.put(CrystalElement.GREEN, MusicKey.F4);*
		baseKeys.put(CrystalElement.BROWN, MusicKey.D4);
		baseKeys.put(CrystalElement.BLUE, MusicKey.E4);
		baseKeys.put(CrystalElement.PURPLE, MusicKey.A4);
		baseKeys.put(CrystalElement.CYAN, MusicKey.A4);
		baseKeys.put(CrystalElement.LIGHTGRAY, MusicKey.D5);*
		baseKeys.put(CrystalElement.GRAY, MusicKey.C5);*
		baseKeys.put(CrystalElement.PINK, MusicKey.F5);
		baseKeys.put(CrystalElement.LIME, MusicKey.E5);
		baseKeys.put(CrystalElement.YELLOW, MusicKey.G5);
		baseKeys.put(CrystalElement.LIGHTBLUE, MusicKey.A5);
		baseKeys.put(CrystalElement.MAGENTA, MusicKey.B4);
		baseKeys.put(CrystalElement.ORANGE, MusicKey.E4);
		baseKeys.put(CrystalElement.WHITE, MusicKey.C6);

		 */
	}

	public int getBasePitch(CrystalElement e) {
		return baseKeys.get(e).pitch;
	}

	public double getThird(CrystalElement e) {
		MusicKey key = baseKeys.get(e);
		double base = this.getDingPitchScale(e);
		return this.isMinorKey(e) ? base*key.getMinorThird().getRatio(key) : base*key.getMajorThird().getRatio(key);
	}

	public double getFifth(CrystalElement e) {
		MusicKey key = baseKeys.get(e);
		double base = this.getDingPitchScale(e);
		return base*key.getFifth().getRatio(key);
	}

	public double getOctave(CrystalElement e) {
		MusicKey key = baseKeys.get(e);
		double base = this.getDingPitchScale(e);
		return base*key.getOctave().getRatio(key);
	}

	public double getDingPitchScale(CrystalElement e) {
		return baseKeys.get(e).getRatio(MusicKey.C5);
	}

	public float getRandomScaledDing(CrystalElement e) {
		MusicKey key = baseKeys.get(e);
		double base = this.getDingPitchScale(e);
		int n = rand.nextInt(4);
		//ReikaJavaLibrary.pConsole(key+":"+key.getMajorThird()+":"+key.getFifth());
		switch(n) {
		case 0:
			;//base *= 1;
			break;
		case 1:
			base *= this.isMinorKey(e) ? key.getMinorThird().getRatio(key) : key.getMajorThird().getRatio(key);
			break;
		case 2:
			base *= key.getFifth().getRatio(key);
			break;
		case 3:
			base *= key.getOctave().getRatio(key);
			break;
		}
		return (float)base;
	}

	public boolean isMinorKey(CrystalElement e) {
		if (e == CrystalElement.CYAN || e == CrystalElement.ORANGE)
			return true;
		return CrystalPotionController.isBadPotion(e);
	}

}
