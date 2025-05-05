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

package me.psychedelicpalimpsest;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;

import java.util.List;

import static me.psychedelicpalimpsest.McPuppeteer.MOD_ID;

public class PuppeteerEffects {
    public static PuppeteerEffect noRotationEffect = new PuppeteerEffect(Identifier.of(MOD_ID, "textures/no_rotation.png"));


    /* Just some simple potion effect like icons */
    public static final List<PuppeteerEffect> effects = ImmutableList.of(
            noRotationEffect
    );


    public static final class PuppeteerEffect {
        public boolean isActive = false;
        public final Identifier texture;

        public PuppeteerEffect(Identifier texture) {
            this.texture = texture;
        }
    }
}
