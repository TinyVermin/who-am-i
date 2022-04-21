package com.eleks.academy.whoami;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.eleks.academy.whoami.core.Game;
import com.eleks.academy.whoami.networking.client.ClientPlayer;
import com.eleks.academy.whoami.networking.server.ServerImpl;

public class App {

	public static void main(String[] args) throws IOException {
		int players = readPlayersArg(args);

		ServerImpl server = new ServerImpl(888);

		Game game = server.startGame();

		List<Socket> playerList = new ArrayList<>(players);
		for (int i = 0; i < players; i++) {
			var socket = server.waitForPlayer(game);
			playerList.add(socket);
		}

		Socket socket = playerList.get(0);
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		boolean gameStatus = true;

		var playerName = reader.readLine();

		server.addPlayer(new ClientPlayer(playerName, socket));

		game.assignCharacters();

		game.initGame();

		while (gameStatus) {
			boolean turnResult = game.makeTurn();

			while (turnResult) {
				turnResult = game.makeTurn();
			}
			game.changeTurn();
			gameStatus = !game.isFinished();
		}

		server.stopServer(socket, reader);
	}

	private static int readPlayersArg(String[] args) {
		if (args.length < 1) {
			return 2;
		} else {
			try {
				int players = Integer.parseInt(args[0]);
				if (players < 2) {
					return 2;
				} else if (players > 5) {
					return 5;
				} else {
					return players;
				}
			} catch (NumberFormatException e) {
				System.err.printf("Cannot parse number of players. Assuming 2. (%s)%n", e.getMessage());
				return 2;
			}
		}
	}

}
