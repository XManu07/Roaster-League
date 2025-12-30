package org.rosterleague.common;

import org.rosterleague.entities.Player;
import org.rosterleague.entities.Team;

import java.util.Collection;
import java.util.List;

public class StandingTeam {
    private final Team team;
    private int points;

    public StandingTeam(Team team, int points) {
        this.team = team;
        this.points = points;
    }

    public Team getTeam() {
        return team;
    }

    public int getPoints() {
        return points;
    }

    public String getPlayersList() {
        List <Player> players = (List<Player>) team.getPlayers();
        String string = "\n Players: ";
        for (Player player : players) {
            string += player.getName() + ", ";
        }
        return string;
    }
    @Override
    public String toString() {
        return team.getName() + " - " + points + " pts" + getPlayersList();
    }
}
