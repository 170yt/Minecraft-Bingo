package x170.bingo.team;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import x170.bingo.goal.Goal;

import java.util.ArrayList;

public abstract class TeamGoalsGUI {
    private static final ArrayList<SimpleGui> activeGuis = new ArrayList<>();

    public static void open(ServerPlayerEntity player) {
        BingoTeam team = TeamManager.getBingoTeam(player);
        if (team == null) return;

        TeamGoalManager goalManager = team.getGoalManager();
        if (goalManager == null) return;

        SimpleGui gui = new SimpleGui(
                getScreenSize(goalManager.getScoreMax()),
                player,
                false
        );
        gui.setTitle(Text.of("Bingo - " + team.getName()));
        fillGUI(gui, goalManager.getRemainingGoals());
        gui.open();
        activeGuis.add(gui);
    }

    public static void update(BingoTeam team) {
        for (int i = 0; i < activeGuis.size(); i++) {
            SimpleGui gui = activeGuis.get(i);
            if (!gui.isOpen()) {
                activeGuis.remove(gui);
                i--;
                continue;
            }
            if (team.hasPlayer(gui.getPlayer())) {
                TeamGoalManager goalManager = team.getGoalManager();
                if (goalManager == null) continue;
                fillGUI(gui, goalManager.getRemainingGoals());
            }
        }
    }

    private static ScreenHandlerType<?> getScreenSize(int slots) {
        if (slots <= 9) return ScreenHandlerType.GENERIC_9X1;
        else if (slots <= 18) return ScreenHandlerType.GENERIC_9X2;
        else if (slots <= 27) return ScreenHandlerType.GENERIC_9X3;
        else if (slots <= 36) return ScreenHandlerType.GENERIC_9X4;
        else if (slots <= 45) return ScreenHandlerType.GENERIC_9X5;
        else return ScreenHandlerType.GENERIC_9X6;
    }

    private static void fillGUI(SimpleGui gui, ArrayList<Goal> goals) {
        for (int i = 0; i < goals.size(); i++)
            gui.setSlot(i, goals.get(i).getDisplayItem());
        for (int i = goals.size(); i < gui.getSize(); i++)
            gui.clearSlot(i);
    }
}
