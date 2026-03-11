package dev.signcommand;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class SignCommandMod implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("sign-command");

    // Cooldown
    private static long lastExecuteTime = 0;
    private static final long COOLDOWN_MS = 500;

    // Für Partikel-Timing
    private static int tickCounter = 0;
    private static final Random random = new Random();

    @Override
    public void onInitializeClient() {
        LOGGER.info("SignCommand mod loaded. Use Shift+Right-click on signs to execute commands.");

        // Hover-Preview und Partikel-Effekt
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            HitResult hit = client.crosshairTarget;
            if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos pos = blockHit.getBlockPos();

            // Prüfe ob es ein Sign ist
            String command = null;
            if (client.world.getBlockEntity(pos) instanceof SignBlockEntity sign) {
                command = extractCommand(sign.getFrontText(), false);
            } else if (client.world.getBlockEntity(pos) instanceof HangingSignBlockEntity hangingSign) {
                command = extractCommand(hangingSign.getFrontText(), true);
            }

            if (command != null) {
                // Command-Preview in Action Bar
                client.player.sendMessage(Text.literal("§7[Shift+Click] §f" + command), true);

                // Partikel alle 5 Ticks spawnen
                tickCounter++;
                if (tickCounter >= 5) {
                    tickCounter = 0;
                    spawnParticles(client, pos);
                }
            }
        });

        // Command-Ausführung bei Shift+Rechtsklick
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient()) return ActionResult.PASS;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return ActionResult.PASS;

            // Nur bei Shift
            if (!client.player.isSneaking()) return ActionResult.PASS;

            // Cooldown prüfen
            long now = System.currentTimeMillis();
            if (now - lastExecuteTime < COOLDOWN_MS) {
                return ActionResult.PASS;
            }

            // Command extrahieren (normal oder hanging sign)
            String command = null;
            BlockPos pos = hitResult.getBlockPos();

            if (world.getBlockEntity(pos) instanceof SignBlockEntity sign) {
                command = extractCommand(sign.getFrontText(), false);
            } else if (world.getBlockEntity(pos) instanceof HangingSignBlockEntity hangingSign) {
                command = extractCommand(hangingSign.getFrontText(), true);
            }

            // Kein Command gefunden - ignorieren (kein Feedback)
            if (command == null) {
                return ActionResult.PASS;
            }

            // Cooldown setzen
            lastExecuteTime = now;

            // Sound abspielen
            client.world.playSound(
                client.player,
                pos,
                SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
                SoundCategory.BLOCKS,
                0.5f,
                1.2f
            );

            // Command ausführen
            client.player.sendMessage(Text.literal("§a[SignCmd] §f" + command), true);
            client.player.networkHandler.sendChatCommand(command.substring(1));
            LOGGER.info("SignCommand executed: '{}'", command);

            return ActionResult.SUCCESS;
        });
    }

    /**
     * Extrahiert den Command aus dem Sign-Text.
     * - Normale Signs: Zeile 3+4 (Index 2, 3)
     * - Hanging Signs: Zeile 2+3 (Index 1, 2) weil nur 3 Zeilen
     * - Color Codes (&x) werden entfernt
     * - Gibt null zurück wenn kein gültiger Command (muss mit / beginnen)
     */
    private static String extractCommand(SignText text, boolean isHangingSign) {
        int startLine = isHangingSign ? 1 : 2;
        int endLine = isHangingSign ? 3 : 4;

        StringBuilder sb = new StringBuilder();
        for (int i = startLine; i < endLine; i++) {
            String line = text.getMessage(i, false).getString().trim();
            line = stripColorCodes(line);
            if (!line.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(line);
            }
        }

        String result = sb.toString().trim();

        // Muss mit / beginnen um als Command erkannt zu werden
        if (result.isEmpty() || !result.startsWith("/")) {
            return null;
        }

        return result;
    }

    /**
     * Entfernt Minecraft Color Codes (&0-9, &a-f, &k-o, &r)
     */
    private static String stripColorCodes(String text) {
        return text.replaceAll("&[0-9a-fk-orA-FK-OR]", "");
    }

    /**
     * Spawnt Enchant-Partikel um das Sign herum via ParticleManager
     */
    private static void spawnParticles(MinecraftClient client, BlockPos pos) {
        if (client.world == null || client.particleManager == null) return;

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        // 2 Partikel pro Spawn
        for (int i = 0; i < 2; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.8;
            double offsetY = (random.nextDouble() - 0.5) * 0.8;
            double offsetZ = (random.nextDouble() - 0.5) * 0.8;

            client.particleManager.addParticle(
                ParticleTypes.ENCHANT,
                x + offsetX,
                y + offsetY,
                z + offsetZ,
                0.0, 0.1, 0.0
            );
        }
    }
}
