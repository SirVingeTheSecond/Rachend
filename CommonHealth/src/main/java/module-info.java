import dk.sdu.sem.commonhealth.HealthComponent;
import dk.sdu.sem.commonsystem.IComponent;

module CommonHealth {
	exports dk.sdu.sem.commonhealth;
	requires Common;
	provides IComponent with HealthComponent;
}