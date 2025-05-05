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


    @Shadow public abstract float getHealth();

    @Inject(method="damage", at=@At("RETURN"))
    void onDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
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
    @Inject(method="onDeath", at=@At("HEAD"))
    void onOnDeath(DamageSource damageSource, CallbackInfo ci){
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
