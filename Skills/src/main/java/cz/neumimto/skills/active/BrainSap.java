package cz.neumimto.skills.active;

import cz.neumimto.SkillLocalization;
import cz.neumimto.core.ioc.Inject;
import cz.neumimto.rpg.IEntity;
import cz.neumimto.rpg.ResourceLoader;
import cz.neumimto.rpg.damage.SkillDamageSource;
import cz.neumimto.rpg.damage.SkillDamageSourceBuilder;
import cz.neumimto.rpg.entities.EntityService;
import cz.neumimto.rpg.events.SkillDamageEventLate;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.skills.*;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.item.ItemTypes;

/**
 * Created by NeumimTo on 5.2.2016.
 */
@ResourceLoader.Skill
@ResourceLoader.ListenerClass
public class BrainSap extends Targetted {

	@Inject
	private EntityService entityService;

	public BrainSap() {
		SkillSettings settings = new SkillSettings();
		settings.addNode(SkillNodes.COOLDOWN, 1000f, 10f);
		settings.addNode(SkillNodes.RANGE, 10f, 1f);
		settings.addNode(SkillNodes.DAMAGE, 10f, 10f);
		setLore(SkillLocalization.SKILL_BRAINSAP_LORE);
		super.settings = settings;
		setName("BrainSap");
		setDescription(SkillLocalization.SKILL_BRAINSAP_DESC);
		setIcon(ItemTypes.ENDER_EYE);
		setDamageType(DamageTypes.MAGIC);
		addSkillType(SkillType.HEALTH_DRAIN);
	}

	@Override
	public SkillResult castOn(Living targettedEntity, IActiveCharacter iActiveCharacter, ExtendedSkillInfo info) {
		SkillDamageSourceBuilder builder = new SkillDamageSourceBuilder();
		builder.fromSkill(this);
		IEntity e = entityService.get(targettedEntity);
		builder.setTarget(e);
		builder.setCaster(iActiveCharacter);
		SkillDamageSource s = builder.build();
		float damage = getFloatNodeValue(info, SkillNodes.DAMAGE);
		e.getEntity().damage(damage, s);
		return SkillResult.OK;
	}

    @Listener(order = Order.LAST)
    public void onDamage(SkillDamageEventLate event) {
        if (event.isCancelled())
            return;
        if (event.getSkill() != null && event.getSkill().getClass() == this.getClass()) {
            IEntity caster = event.getCaster();
            entityService.healEntity(caster, (float) event.getDamage(), this);
        }
    }
}
