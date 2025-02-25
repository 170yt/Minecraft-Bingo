package x170.bingo.game;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class BingoInventory extends SimpleInventory {
    private final String name;
    private final boolean large;

    public BingoInventory(String name, boolean large) {
        super(large ? 54 : 27);
        this.name = name;
        this.large = large;
    }

    public void open(ServerPlayerEntity player) {
        player.openHandledScreen(
                new SimpleNamedScreenHandlerFactory(
                        (i, playerInventory, playerEntity) ->
                                this.large
                                    ? GenericContainerScreenHandler.createGeneric9x6(i, playerInventory, this)
                                    : GenericContainerScreenHandler.createGeneric9x3(i, playerInventory, this),
                        Text.literal(name)
                )
        );
    }
}
