package cz.neumimto.rpg.skills;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import cz.neumimto.rpg.effects.EffectSourceType;
import cz.neumimto.rpg.effects.IEffectSource;
import cz.neumimto.rpg.inventory.ConfigRPGItemType;
import cz.neumimto.rpg.players.IActiveCharacter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;

import java.util.*;


public class ItemAccessSkill extends AbstractSkill {
    public ItemAccessSkill(String name) {
        super();
        setName(name);
    }

    @Override
    public SkillResult onPreUse(IActiveCharacter character) {
        return SkillResult.CANCELLED;
    }

    @Override
    public void skillLearn(IActiveCharacter IActiveCharacter) {
        super.skillLearn(IActiveCharacter);
        resolveItemAccess(IActiveCharacter);

    }

    @Override
    public void skillUpgrade(IActiveCharacter IActiveCharacter, int level) {
        super.skillUpgrade(IActiveCharacter, level);
        resolveItemAccess(IActiveCharacter);
    }

    @Override
    public void onCharacterInit(IActiveCharacter c, int level) {
        super.onCharacterInit(c, level);
        resolveItemAccess(c);
    }

    @Override
    public void skillRefund(IActiveCharacter IActiveCharacter) {
        super.skillRefund(IActiveCharacter);
        resolveItemAccess(IActiveCharacter);
    }

    private void resolveItemAccess(IActiveCharacter c) {
        c.updateItemRestrictions();
    }


    @Override
    public IEffectSource getType() {
        return EffectSourceType.ITEM_ACCESS_SKILL;
    }


    @Override
    public ItemAccessSkillData constructSkillData() {
        return new ItemAccessSkillData(getName());
    }

    @Override
    public <T extends SkillData> void loadSkillData(T skillData, SkillTree context, SkillLoadingErrors errors, Config c) {
        ItemAccessSkillData data = (ItemAccessSkillData) skillData;
        try {

            List<? extends Config> items = c.getConfigList("Items");
            for (Config item : items) {
                int level = item.getInt("level");
                List<String> citems = item.getStringList("items");
                for (String allowedWeapon : citems) {
                    String[] split = allowedWeapon.split(";");
                    String s = split[0];
                    double damage = 0;
                    String itemName = null;
                    ItemType type = Sponge.getGame().getRegistry().getType(ItemType.class, s).orElse(null);
                    if (type == null) {
                        errors.log(" - Unknown item type " + s);
                    } else {
                        String s1 = split[1];
                        damage = Double.parseDouble(s1);
                        if (split.length == 3) {
                            itemName = split[2];
                        }
                        ConfigRPGItemType t = new ConfigRPGItemType(type,itemName, data.getSkill(), damage);
                        data.addItemType(level, t);
                    }
                }
            }
        } catch (ConfigException e) {

        }
    }

    public class ItemAccessSkillData extends SkillData {

        private Map<Integer, Map<ItemType,Set<ConfigRPGItemType>>> items = new HashMap<>();

        public ItemAccessSkillData(String skill) {
            super(skill);
        }

        public Map<Integer, Map<ItemType, Set<ConfigRPGItemType>>> getItems() {
            return items;
        }

        public void addItemType(Integer i, ConfigRPGItemType type) {
            Map<ItemType, Set<ConfigRPGItemType>> itemTypeTreeSetMap = items.get(i);
            if (itemTypeTreeSetMap == null) {
                itemTypeTreeSetMap = new HashMap<>();
                Set<ConfigRPGItemType> set = new HashSet<>();
                set.add(type);
                itemTypeTreeSetMap.put(type.getItemType(), set);
                items.put(i, itemTypeTreeSetMap);
            } else {
                itemTypeTreeSetMap.get(type.getItemType()).add(type);
            }
        }

        public void setItems(Map<Integer, Map<ItemType, Set<ConfigRPGItemType>>> items) {
            this.items = items;
        }
    }
}