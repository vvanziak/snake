package com.globallogic.snake.services;

import com.globallogic.snake.model.Board;
import com.globallogic.snake.model.BoardImpl;
import com.globallogic.snake.model.Snake;
import com.globallogic.snake.model.SnakeFactory;
import com.globallogic.snake.model.artifacts.ArtifactGenerator;
import com.globallogic.snake.model.artifacts.RandomArtifactGenerator;
import com.globallogic.snake.model.middle.SnakeEventListener;
import com.globallogic.snake.model.middle.SnakeEvented;
import com.globallogic.snake.services.playerdata.PlayerData;
import com.globallogic.snake.services.playerdata.Plot;
import com.globallogic.snake.services.playerdata.PlotsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * User: oleksandr.baglai
 * Date: 10/1/12
 * Time: 6:48 AM
 */
@Component("playerService")
public class PlayerService {
    private static Logger logger = LoggerFactory.getLogger(PlayerService.class);
    public static final int BOARD_SIZE = 15;

    @Autowired
    private ScreenSender screenSender;

    @Autowired
    private PlayerController playerController;

    @Autowired
    private ArtifactGenerator artifactGenerator;

    private List<Player> players = new ArrayList<>();
    private List<Board> boards = new ArrayList<>();
    private List<SnakeEventListener> scores = new ArrayList<>();

    private ReadWriteLock lock = new ReentrantReadWriteLock(true);

    public Player addNewPlayer(final String name, final String callbackUrl) {
        lock.writeLock().lock();
        try {
            int minScore = getPlayersMinScore();
            final PlayerScores playerScores = new PlayerScores(minScore);

            Board board = new BoardImpl(artifactGenerator, new SnakeFactory() {
                @Override
                public Snake create(int x, int y) {
                    return new SnakeEvented(playerScores, x, y);
                }
            }, BOARD_SIZE);

            Player player = new Player(name, callbackUrl, playerScores);
            players.add(player);
            boards.add(board);
            scores.add(playerScores);
            return player;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private int getPlayersMinScore() {
        int result = 0;
        for (Player player : players) {
            result = Math.min(player.getScore(), result);
        }
        return result;
    }

    public void nextStepForAllGames() {
        lock.writeLock().lock();
        try {
            for (Board board : boards) {
                if (board.isGameOver()) {
                    board.newGame();
                }
                board.tact();
            }

            HashMap<Player, PlayerData> map = new HashMap<>();
            HashMap<Player, List<Plot>> droppedPlotsMap = new HashMap<>();
            for (int i = 0; i < boards.size(); i++) {
                Board board = boards.get(i);
                List<Plot> plots = new ArrayList<>();
                plots.addAll(new PlotsBuilder(board).get());

                Player player = players.get(i);

                map.put(player, new PlayerData(board.getSize(), plots, player.getScore(), player.getCurrentLevel() + 1));
            }

            screenSender.sendUpdates(map);

            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);
                Board board = boards.get(i);
                try {
                    playerController.requestControl(player, board.getSnake(), board);
                } catch (IOException e) {
                    logger.error("Unable to send control request to player " + player.getName() +
                            " URL: " + player.getCallbackUrl(), e);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Player> getPlayers() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableList(players);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean alreadyRegistered(String playerName) {
        lock.readLock().lock();
        try {
            return findPlayer(playerName) != null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Player findPlayer(String playerName) {
        lock.readLock().lock();
        try {
            for (Player player : players) {
                if (player.getName().equals(playerName)) {
                    return player;
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void updatePlayer(Player player) {
        lock.writeLock().lock();
        try {
            for (Player playerToUpdate : players) {
                if (playerToUpdate.getName().equals(player.getName())) {
                    playerToUpdate.setCallbackUrl(player.getCallbackUrl());
                    return;
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            players.clear();
            boards.clear();
            scores.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    List<Board> getBoards() {
        return boards;
    }

    public Player findPlayerByIp(String ip) {
        lock.readLock().lock();
        try {
            for (Player player : players) {
                if (player.getCallbackUrl().contains(ip)) {
                    return player;
                }
            }
            return new NullPlayer();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void removePlayer(String ip) {
        lock.writeLock().lock();
        try {
            int index = players.indexOf(findPlayerByIp(ip));
            if (index < 0) return;
            players.remove(index);
            boards.remove(index);
            scores.remove(index);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int getBoardSize() {
        return BOARD_SIZE;
    }
}