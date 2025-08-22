/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */
package me.psychedelicpalimpsest;

import static me.psychedelicpalimpsest.McPuppeteer.MOD_ID;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.util.Identifier;

public class PuppeteerEffects {
  public static PuppeteerEffect noRotationEffect =
      new PuppeteerEffect(Identifier.of(MOD_ID, "textures/no_rotation.png"));
  public static PuppeteerEffect noWalkEffect =
      new PuppeteerEffect(Identifier.of(MOD_ID, "textures/no_walk.png"));
  public static PuppeteerEffect freecamEffect =
      new PuppeteerEffect(Identifier.of(MOD_ID, "textures/freecam.png"));

  /* Just some simple potion effect like icons, drawn in InGameHudMixin.java */
  public static final List<PuppeteerEffect> effects =
      ImmutableList.of(freecamEffect, noRotationEffect, noWalkEffect);

  public static final class PuppeteerEffect {
    public boolean isActive = false;
    public final Identifier texture;

    public PuppeteerEffect(Identifier texture) {
      this.texture = texture;
    }
  }
}
