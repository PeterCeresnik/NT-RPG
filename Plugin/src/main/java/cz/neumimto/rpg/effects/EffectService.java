/*    
 *     Copyright (c) 2015, NeumimTo https://github.com/NeumimTo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *     
 */

package cz.neumimto.rpg.effects;


import cz.neumimto.core.ioc.Inject;
import cz.neumimto.core.ioc.PostProcess;
import cz.neumimto.core.ioc.Singleton;
import cz.neumimto.rpg.NtRpgPlugin;
import cz.neumimto.rpg.players.IActiveCharacter;
import org.spongepowered.api.Game;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by NeumimTo on 17.1.2015.
 */
@Singleton
public class EffectService {


    public static final long TICK_PERIOD = 250L;

    private static final long unlimited_duration = -1;

    @Inject
    private Game game;

    @Inject
    private NtRpgPlugin plugin;

    private Set<IEffect> effectSet = new HashSet<>();
    private Set<IEffect> pendingAdditions = new HashSet<>();
    private Set<IEffect> pendingRemovals = new HashSet<>();
    private Map<String, IGlobalEffect> globalEffects = new HashMap<>();

    /**
     * calls effect.onApply and registers if effect requires
     *
     * @param effect
     */
    public void runEffect(IEffect effect) {
        pendingAdditions.add(effect);
    }

    /**
     * Stops the effect and calls onRemove
     *
     * @param effect
     */
    public void stopEffect(IEffect effect) {
        if (effect.requiresRegister()) {
            pendingRemovals.add(effect);
        }
    }


    /**
     * Attempts to remove all references of given object from the scheduler
     * Wont call onRemove
     *
     * @param effect
     */
    public void purgeEffect(IEffect effect) {
        if (effect.requiresRegister()) {
            try {
                pendingRemovals.remove(effect);
                pendingAdditions.remove(effect);
                effectSet.remove(effect);
                IEffectConsumer consumer = effect.getConsumer();
                if (consumer != null) {
                    consumer.removeEffect(effect);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @PostProcess(priority = 1000)
    public void run() {
        game.getScheduler().createTaskBuilder().name("EffectTask")
                .delay(10L, TimeUnit.MILLISECONDS).interval(TICK_PERIOD, TimeUnit.MILLISECONDS)
                .execute(() -> {
                    for (IEffect pendingRemoval : pendingRemovals) {
                        if (effectSet.contains(pendingRemoval)) {
                            effectSet.remove(pendingRemoval);
                            IEffectConsumer consumer = pendingRemoval.getConsumer();
                            if (consumer != null) {
                                consumer.removeEffect(pendingRemoval);
                            }
                        }
                    }
                    pendingRemovals.clear();
                    long l = System.currentTimeMillis();
                    for (IEffect e : effectSet) {

                        if (e.getPeriod() + e.getLastTickTime() <= l) {
                            tickEffect(e, l);
                        }

                        if (e.getDuration() == unlimited_duration) {
                            continue;
                        }

                        if (e.getExpireTime() <= l) {
	                        removeEffect(e, e.getConsumer());
                        }
                    }

                    for (IEffect pendingAddition : pendingAdditions) {
                        effectSet.add(pendingAddition);
                    }
                    pendingAdditions.clear();
                }).submit(plugin);
    }

    /**
     * Calls onTick and increments tickCount
     *
     * @param effect
     */
    public void tickEffect(IEffect effect, long time) {
        effect.onTick();
        effect.tickCountIncrement();
        effect.setLastTickTime(time);
    }

    /**
     * Adds effect to the consumer,
     * Effects requiring register are registered into the scheduler
     *
     * @param iEffect
     * @param consumer
     */
    @SuppressWarnings("unchecked")
    public void addEffect(IEffect iEffect, IEffectConsumer consumer, IEffectSourceProvider effectSourceProvider) {
        IEffectContainer eff = consumer.getEffect(iEffect.getName());
        if (eff == null) {
            consumer.addEffect(iEffect.constructEffectContainer());
	        iEffect.onApply();
        } else if (eff.isStackable()) {
            iEffect.onApply();
            eff.stackEffect(iEffect, effectSourceProvider);
        }
	    if (iEffect.requiresRegister())
		    runEffect(iEffect);
    }

    /**
     * Removes and stops the effect
     *
     * @param iEffect
     * @param consumer
     */
    public void removeEffect(IEffect iEffect, IEffectConsumer consumer) {
        IEffectContainer effect = consumer.getEffect(iEffect.getName());
        if (effect != null) {
            if (effect == iEffect) {
                iEffect.onRemove();
                stopEffect(iEffect);
                consumer.removeEffect(effect);
                return;
            }
            if (effect.getEffects().contains(iEffect)) {
                effect.getEffects().remove(iEffect);
                iEffect.onRemove();
                stopEffect(iEffect);
                if (effect.getEffects().isEmpty()) {
                    consumer.removeEffect(effect);
                }
            }
        }
    }

    /**
     * Removes and stops the effect
     *
     * @param iEffect
     * @param consumer
     */
    @SuppressWarnings("unchecked")
    public void removeEffect(String iEffect, IEffectConsumer consumer, IEffectSourceProvider effectSource) {
        IEffectContainer effect = consumer.getEffect(iEffect);
        if (effect != null) {
            Iterator<IEffect> iterator = effect.getEffects().iterator();
            IEffect e;
            while (iterator.hasNext()) {
                e = iterator.next();
                if (e.getEffectSourceProvider() == effectSource) {
                    stopEffect(e);
                    iterator.remove();
                }
            }
            if (effect.getEffects().isEmpty()) {
                consumer.removeEffect(effect);
            }

        }
    }

    /**
     * Register global effect
     *
     * @param iGlobalEffect
     */
    public void registerGlobalEffect(IGlobalEffect iGlobalEffect) {
        globalEffects.put(iGlobalEffect.getName().toLowerCase(), iGlobalEffect);
    }

    /**
     * Removes cached globaleffect
     *
     * @param name
     */
    public void removeGlobalEffect(String name) {
        name = name.toLowerCase();
        if (globalEffects.containsKey(name))
            globalEffects.remove(name);
    }

    /**
     * Returns global effect by its name, if effect does not exists return null
     *
     * @param name
     * @return effect or null if key is not in the map
     */
    public IGlobalEffect getGlobalEffect(String name) {
        return globalEffects.get(name.toLowerCase());
    }

    /**
     * Applies global effect with unlimited duration
     *
     * @param effect
     * @param consumer
     * @param value
     */
    public void applyGlobalEffectAsEnchantment(IGlobalEffect effect, IEffectConsumer consumer, String value, IEffectSourceProvider effectSourceType) {
        IEffect construct = effect.construct(consumer, unlimited_duration, value);
        addEffect(construct, consumer, effectSourceType);
    }

    /**
     * Applies global effects with unlimited duration
     *
     * @param map
     * @param consumer
     */
    public void applyGlobalEffectsAsEnchantments(Map<IGlobalEffect, String> map, IEffectConsumer consumer, IEffectSourceProvider effectSourceType) {
        map.forEach((e, l) ->
            applyGlobalEffectAsEnchantment(e, consumer, l, effectSourceType)
        );
    }


    public void removeGlobalEffectsAsEnchantments(Map<IGlobalEffect, String> itemEffects, IActiveCharacter character, IEffectSourceProvider effectSourceProvider) {
        itemEffects.forEach((e, l) -> {
	        removeEffect(e.getName(), character,effectSourceProvider);
        });
    }

    public boolean isGlobalEffect(String s) {
        return globalEffects.containsKey(s.toLowerCase());
    }

	@SuppressWarnings("unchecked")
    public void removeAllEffects(IActiveCharacter character) {
	    for (IEffectContainer<Object, IEffect<Object>> IEffectContainer : character.getEffects()) {
		    for (IEffect effect : IEffectContainer.getEffects()) {
			    pendingRemovals.add(effect);
		    }
	    }
    }
}


