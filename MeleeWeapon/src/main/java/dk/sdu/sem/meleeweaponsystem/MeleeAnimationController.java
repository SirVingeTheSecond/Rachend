package dk.sdu.sem.meleeweaponsystem;

import dk.sdu.sem.commonsystem.Entity;
import dk.sdu.sem.commonsystem.Vector2D;
import dk.sdu.sem.commonweapon.IMeleeWeapon;

public class MeleeAnimationController {
	IMeleeWeapon meleeWeapon;
	Entity activator;
	MeleeAnimationController(IMeleeWeapon meleeWeapon ) {
		this.meleeWeapon = meleeWeapon;
		createAnimation();
	}
	private void createAnimation() {


	}

	void runAnimation(Vector2D animationPosition,Vector2D directon){

	}
}
