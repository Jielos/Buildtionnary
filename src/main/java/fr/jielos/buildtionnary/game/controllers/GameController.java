package fr.jielos.buildtionnary.game.controllers;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.game.Game;
import fr.jielos.buildtionnary.game.GameComponent;
import fr.jielos.buildtionnary.game.data.players.GamePlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

public class GameController extends GameComponent {

    public GameController(Buildtionnary instance, Game game) {
        super(instance, game);
    }

    public void loadOnlinePlayers() {
        for(Player player : instance.getServer().getOnlinePlayers()) {
            addPlayer(player);
        }
    }

    public void addPlayer(Player player) {
        game.getGameData().getPlayers().add(player);

        player.teleport(game.getConfigController().getLocation(ConfigController.Value.WAITING_ROOM));
        player.setGameMode(GameMode.ADVENTURE);
        clearContents(player);

        if(game.isWaiting()) {
            final int playersSize = game.getGameData().getPlayers().size();
            final int maxPlayers = game.getConfigController().getInt(ConfigController.Value.MAX_PLAYERS);
            final String joinMessage = String.format("§7%s §ea rejoint la partie ! §a(%d/%d)", player.getName(), playersSize, maxPlayers);

            instance.getServer().broadcastMessage(joinMessage);
            instance.getInitializer().sendActionBar(instance.getServer().getOnlinePlayers(), joinMessage);

            game.checkLaunch();
        }

        game.getBoardController().updatePlayerBoard(player);
    }

    public void removePlayer(Player player, boolean spectate) {
        game.getGameData().getPlayers().remove(player);

        if(game.isWaiting()) {
            final int playersSize = game.getGameData().getPlayers().size();
            final int maxPlayers = game.getConfigController().getInt(ConfigController.Value.MAX_PLAYERS);
            final String quitMessage = String.format("§7%s §ea quitté la partie ! §c(%d/%d)", player.getName(), playersSize, maxPlayers);

            instance.getServer().broadcastMessage(quitMessage);
            instance.getInitializer().sendActionBar(instance.getServer().getOnlinePlayers(), quitMessage);

            final PluginDescriptionFile pluginDescriptionFile = instance.getDescription();
            player.sendMessage(String.format("§8§l%s\n§7%s", pluginDescriptionFile.getName(), pluginDescriptionFile.getDescription()));
        } else if(game.isPlaying()) {
            final GamePlayer gamePlayer = game.getGameData().getGamePlayer(player);
            if(game.getGameBuilders().getGameRemainingBuilders().contains(gamePlayer)) {
                game.getGameBuilders().removeGamePlayer(gamePlayer, true);
            }

            if(spectate) addSpectator(player);
        }

        game.getBoardController().updatePlayerBoard(player);
    }

    public void addSpectator(Player player) {
        player.teleport(game.getConfigController().getLocation(ConfigController.Value.WAITING_ROOM));
        player.setGameMode(GameMode.SPECTATOR);

        player.sendMessage("§7§oVous êtes désormais §8§oSpectateur§7§o, vous rejoindrez la prochaine partie automatiquement.");
        instance.getInitializer().sendTitle(player, "§8§lMODE SPECTATEUR", "", 10, 70, 20);

        game.getGameData().getSpectators().add(player);
        game.getBoardController().updatePlayerBoard(player);
    }

    public boolean isPlayer(Player player) {
        return game.getGameData().getPlayers().contains(player) && !game.getGameData().getSpectators().contains(player);
    }
    public boolean isSpectator(Player player) {
        return !isPlayer(player);
    }

    public void clearContents(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setHealth(20); player.setFoodLevel(20);
        player.setLevel(0); player.setExp(0);
    }
}