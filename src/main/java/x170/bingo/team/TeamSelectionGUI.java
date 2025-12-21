package x170.bingo.team;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import x170.bingo.Bingo;
import x170.bingo.game.GameManager;
import x170.bingo.game.GameStatus;

import java.util.List;

public class TeamSelectionGUI extends SimpleGui {
    public TeamSelectionGUI(ServerPlayerEntity player) {
        // TODO: Set the size of the GUI depending on the number of teams
        super(ScreenHandlerType.GENERIC_9X1, player, false);
        this.setTitle(Text.literal("Bingo Teams"));
        this.open();
    }

    @Override
    public void onTick() {
        Scoreboard scoreboard = Bingo.SERVER.getScoreboard();

        for (BingoTeam team : TeamManager.getBingoTeams()) {
            List<ServerPlayerEntity> players = team.getPlayersInTeam();
            boolean isPlayerInTeam = players.contains(player);

            GuiElementBuilder builder = new GuiElementBuilder(team.getGuiItem())
                    .setName(Text.literal("Team " + team.getName()).formatted(team.getColor()))
                    .hideDefaultTooltip()
                    .glow(isPlayerInTeam)
                    .setMaxCount(99)
                    .setCount(players.isEmpty() ? 1 : players.size())
                    .setCallback((element, clickType, slotActionType) -> {
                        if (clickType != ClickType.MOUSE_LEFT && clickType != ClickType.MOUSE_RIGHT) return;
                        if (GameManager.status != GameStatus.IDLE) return;

                        // Join the clicked team if not already in it
                        if (isPlayerInTeam)
                            scoreboard.clearTeam(player.getNameForScoreboard());
                        else
                            scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), team.getTeam());

                        GameManager.playSoundToPlayer(player, SoundEvents.UI_BUTTON_CLICK.value(), 0.5F);
                    });

            for (ServerPlayerEntity p : players) {
                builder.addLoreLine(Text.literal(p.getName().getString()).formatted(Formatting.GRAY));
            }

            this.setSlot(team.getSlot(), builder.build());
        }
        super.onTick();
    }
}
