package dk.sdu.sem.dashability;

import dk.sdu.sem.commonsystem.IComponent;

public class DashAbilityComponent implements IComponent {
	private double dashCooldown = 0.800;
	private double dashTimer = 0.0;

	public float velocityScale = 1000;

	public void update(double deltaTime) {
		dashTimer = Math.max(dashTimer - deltaTime, 0);
	}

	public boolean isOnCooldown() {
		return dashTimer > 0;
	}

	public void use() {
		dashTimer = dashCooldown;
	}

	public double progress() {
		return 1 - dashTimer / dashCooldown;
	}
}
