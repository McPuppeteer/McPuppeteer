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
