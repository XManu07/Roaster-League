package org.rosterleague.common;

import java.io.Serializable;

public class GameDetails implements Serializable {
    private final String id;
    private final String team1;
    private final String team2;
    private final int goalsTeam1;
    private final int goalsTeam2;

    public String getId() {
        return id;
    }

    public String getTeam1() {
        return team1;
    }

    public String getTeam2() {
        return team2;
    }

    public int getGoalsTeam1() {
        return goalsTeam1;
    }

    public int getGoalsTeam2() {
        return goalsTeam2;
    }

    public GameDetails(String id, String team1, String team2, int goalsTeam1, int goalsTeam2) {
        this.id = id;
        this.team1 = team1;
        this.team2 = team2;
        this.goalsTeam1 = goalsTeam1;
        this.goalsTeam2 = goalsTeam2;
    }

    @Override
    public String toString() {
        return id + " " + team1 + " vs " + team2 + " " + goalsTeam1 + "-" + goalsTeam2;
    }
}
