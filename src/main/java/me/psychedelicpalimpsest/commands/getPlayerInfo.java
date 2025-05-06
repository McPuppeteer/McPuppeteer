package me.psychedelicpalimpsest.commands;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;


@PuppeteerCommand(
        cmd = "get player info",
        description = "Gets a bunch of info about the player"
)
public class getPlayerInfo implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;

        /* If you can think of anything else, send a PR! */
        callback.resultCallback(BaseCommand.jsonOf(
                "x", p.getX(),
                "y", p.getY(),
                "z", p.getZ(),

                "food level", p.getHungerManager().getFoodLevel(),
                "saturation level", p.getHungerManager().getSaturationLevel(),

                "health", p.getHealth(),

                "pitch", p.getPitch(),
                "yaw", p.getYaw(),

                "is creative", p.isCreative(),
                "is spectator", p.isSpectator(),

                "hotbar slot", p.getInventory().selectedSlot + 1,

                "is holding onto ladder", p.isHoldingOntoLadder(),
                "is using item", p.isUsingItem(),
                "is sneaking", p.isSneaking(),
                "is sprinting", p.isSprinting(),
                "is riding", p.isRiding(),
                "is alive", p.isAlive(),
                "is dead", p.isDead(),
                "is blocking", p.isBlocking(),
                "is using spyglass", p.isUsingSpyglass(),
                "is using riptide", p.isUsingRiptide(),
                "is using item", p.isUsingItem(),
                "in powder snow", p.inPowderSnow,
                "is gliding", p.isGliding(),
                "is glowing", p.isGlowing(),
                "is frozen", p.isFrozen(),
                "is in lava", p.isInLava(),
                "is climbing", p.isClimbing(),
                "is on ground", p.isOnGround(),
                "is on rail", p.isOnRail(),
                "is on fire", p.isOnFire(),
                "is auto jump enabled", p.isAutoJumpEnabled(),
                "is swimming", p.isSwimming(),
                "is touching water or rain", p.isTouchingWaterOrRain(),
                "is wet", p.isWet(),
                "is in wall", p.isInsideWall(),

                "username", MinecraftClient.getInstance().getSession().getUsername(),
                "account type", MinecraftClient.getInstance().getSession().getAccountType().getName(),

                "uuid", MinecraftClient.getInstance().getSession().getXuid().orElse("UNKNOWN")
        ));
    }
}
