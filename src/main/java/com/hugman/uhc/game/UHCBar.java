package com.hugman.uhc.game;

import com.hugman.uhc.util.TickUtil;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.widget.BossBarWidget;

public class UHCBar {
    private final BossBarWidget widget;
    private final GameSpace gameSpace;
    private String symbol;
    private String name;
    private String message;
    private BossBar.Color color;
    private long endTick = 0;
    private long totalTicks = 0;
    private boolean canTick = false;

    private UHCBar(BossBarWidget widget, GameSpace gameSpace) {
        this.widget = widget;
        this.gameSpace = gameSpace;
    }

    public static UHCBar create(GlobalWidgets widgets, GameSpace gameSpace) {
        return new UHCBar(widgets.addBossBar(gameSpace.getMetadata().sourceConfig().value().name(), BossBar.Color.BLUE, BossBar.Style.PROGRESS), gameSpace);
    }

    public void set(String symbol, String name, long totalTicks, long endTick, BossBar.Color color) {
        this.symbol = symbol.equals("") ? "" : symbol + " ";
        this.name = name + ".countdown_bar";
        this.message = name + ".countdown_text";
        this.totalTicks = totalTicks;
        this.endTick = endTick;
        this.color = color;
        this.canTick = true;
    }

    public void set(String name, long totalTicks, long endTick, BossBar.Color color) {
        this.set("", name, totalTicks, endTick, color);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFull(Text title) {
        this.widget.setTitle(title);
        this.widget.setStyle(BossBar.Color.GREEN, BossBar.Style.PROGRESS);
        this.widget.setProgress(1.0f);
        this.canTick = false;
    }

    public void close() {
        this.widget.close();
        this.canTick = false;
    }

    public void tick(ServerWorld world) {
        long ticks = this.endTick - world.getTime();
        if (ticks % 20 == 0 && canTick) {
            long seconds = TickUtil.asSeconds(ticks);
            long totalSeconds = TickUtil.asSeconds(totalTicks);

            BossBar.Color newColor = this.color;
            if (seconds <= 5 || seconds == 10 || seconds == 15 || seconds == 30 || seconds == 60 || seconds == 150 || seconds == 300 || seconds == 600 || seconds == 900 || seconds == 1800) {
                sendMessage(seconds);
                newColor = BossBar.Color.RED;
            }
            this.widget.setTitle(Text.literal(symbol).append(Text.translatable(name, TickUtil.format(ticks))));
            this.widget.setStyle(newColor, BossBar.Style.NOTCHED_10);
            this.widget.setProgress((float) seconds / totalSeconds);
        }
    }

    private void sendMessage(long seconds) {
        float pitch = seconds == 0 ? 1.5F : 1.0F;
        gameSpace.getPlayers().forEach(entity -> {
            if (this.message != null && seconds != 0) {
                entity.sendMessage(Text.literal(symbol).append(Text.translatable(this.message, TickUtil.formatPretty(seconds * 20).formatted(Formatting.RED))).formatted(Formatting.GOLD), false);
            }
            entity.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, pitch);
        });
    }
}
