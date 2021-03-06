package cz.neumimto.rpg.commands;

import cz.neumimto.rpg.NtRpgPlugin;
import cz.neumimto.rpg.skills.ISkill;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.PatternMatchingCommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Created by NeumimTo on 16.11.2017.
 */
public class LearnedSkillCommandElement extends PatternMatchingCommandElement {

	public LearnedSkillCommandElement(@Nullable Text key) {
		super(key);
	}

	@Override
	protected Iterable<String> getChoices(CommandSource source) {
		return NtRpgPlugin.GlobalScope.characterService.getCharacter((Player) source).getSkills().keySet();
	}

	@Override
	protected Object getValue(String choice) {
		Optional<ISkill> ret = Sponge.getGame().getRegistry().getType(ISkill.class, choice);
		if (!ret.isPresent()) {
			ret = Optional.ofNullable(NtRpgPlugin.GlobalScope.skillService.getSkillByLocalizedName(choice));
		}
		return ret.get();
	}

}
