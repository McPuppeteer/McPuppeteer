/**
 *     Copyright (C) 2025 - PsychedelicPalimpsest
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.psychedelicpalimpsest.modules;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.PlayerInput;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;




/*
    Acts like no keys are being pressed, unless they are forced down
 */


public class PuppeteerInput extends Input {
    public static Map<String, Boolean> isForcePressed = new HashMap<>();

    public static final String FORWARDS = "forwards";
    public static final String BACKWARDS = "backwards";
    public static final String LEFT = "left";
    public static final String RIGHT = "right";
    public static final String JUMP = "jump";
    public static final String SNEAK = "sneak";
    public static final String SPRINT = "sprint";

    public static final Set<String> validOptions = ImmutableSet.of(
            FORWARDS,
            BACKWARDS,
            LEFT,
            RIGHT,
            JUMP,
            SNEAK,
            SPRINT
    );
    public static boolean allowUserInput = true;



    private void regenInput() {
        GameOptions opts = MinecraftClient.getInstance().options;

        final boolean allowUserInputAndNotFreecam = allowUserInput && !Freecam.isFreecamActive();


        this.playerInput = new PlayerInput(
                isForcePressed.getOrDefault(FORWARDS, allowUserInputAndNotFreecam && opts.forwardKey.isPressed()),
                isForcePressed.getOrDefault(BACKWARDS, allowUserInputAndNotFreecam && opts.backKey.isPressed()),
                isForcePressed.getOrDefault(LEFT, allowUserInputAndNotFreecam && opts.leftKey.isPressed()),
                isForcePressed.getOrDefault(RIGHT, allowUserInputAndNotFreecam && opts.rightKey.isPressed()),
                isForcePressed.getOrDefault(JUMP, allowUserInputAndNotFreecam && opts.jumpKey.isPressed()),
                isForcePressed.getOrDefault(SNEAK, allowUserInputAndNotFreecam && opts.sneakKey.isPressed()),
                isForcePressed.getOrDefault(SPRINT, allowUserInputAndNotFreecam && opts.sprintKey.isPressed())
        );
    }


    @Override
    public void tick(boolean slowDown, float slowDownFactor) {
        regenInput();
        this.movementForward = KeyboardInput.getMovementMultiplier(this.playerInput.forward(), this.playerInput.backward());
        this.movementSideways = KeyboardInput.getMovementMultiplier(this.playerInput.left(), this.playerInput.right());
        if (slowDown) {
            this.movementSideways *= slowDownFactor;
            this.movementForward *= slowDownFactor;
        }
    }








}
