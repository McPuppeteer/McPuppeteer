/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.psychedelicpalimpsest.mixin;


import com.google.gson.Gson;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.CallbackManager;
import me.psychedelicpalimpsest.PuppeteerServer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.psychedelicpalimpsest.McPuppeteer.createTextJsonSerializer;
import static me.psychedelicpalimpsest.McPuppeteer.textToString;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {


    @Shadow
    public abstract float getHealth();

    @Inject(method = "damage", at = @At("RETURN"))
    void onDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        boolean value = cir.getReturnValue();
        if (!value) return;

        Entity asEntity = (Entity) (Object) this;
        if (!asEntity.isPlayer()) return;


        Gson ser = createTextJsonSerializer();
        Text msg = source.getDeathMessage((LivingEntity) (Object) this);

        PuppeteerServer.broadcastJsonPacket(CallbackManager.CallbackType.PLAYER_DAMAGE, BaseCommand.jsonOf(
                "amount", amount,
                "health", this.getHealth(),
                "would be death message", textToString(msg),
                "would be death message json", ser.toJsonTree(msg)
        ));
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    void onOnDeath(DamageSource damageSource, CallbackInfo ci) {
        Entity asEntity = (Entity) (Object) this;
        if (!asEntity.isPlayer()) return;

        Gson ser = createTextJsonSerializer();
        Text msg = damageSource.getDeathMessage((LivingEntity) (Object) this);

        PuppeteerServer.broadcastJsonPacket(CallbackManager.CallbackType.PLAYER_DEATH, BaseCommand.jsonOf(
                "death message", textToString(msg),
                "death message json", ser.toJsonTree(msg)
        ));
    }
}
