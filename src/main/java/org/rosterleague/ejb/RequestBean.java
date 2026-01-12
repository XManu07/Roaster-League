/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v1.0, which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package org.rosterleague.ejb;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.rosterleague.common.*;
import org.rosterleague.entities.*;

/**
 * This is the bean class for the RequestBean enterprise bean.
 *
 * @author ian
 */
@Stateless
public class RequestBean implements Request, Serializable {

    private static final Logger logger = Logger.getLogger(RequestBean.class.getName());

    @PersistenceContext
    private EntityManager em;

    private CriteriaBuilder cb;

    @PostConstruct
    private void init() {
        cb = em.getCriteriaBuilder();
    }

    @Override
    public void createPlayer(String id, String name, String position, double salary) {
        logger.info("createPlayer");
        try {
            Player player = new Player(id, name, position, salary);
            em.persist(player);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public void addPlayer(String playerId, String teamId) {
        logger.info("addPlayer");
        try {
            Player player = em.find(Player.class, playerId);
            Team team = em.find(Team.class, teamId);

            team.addPlayer(player);
            player.addTeam(team);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public void removePlayer(String playerId) {
        logger.info("removePlayer");
        try {
            Player player = em.find(Player.class, playerId);

            Collection<Team> teams = player.getTeams();
            for (Team team : teams) {
                team.dropPlayer(player);
            }

            em.remove(player);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public void dropPlayer(String playerId, String teamId) {
        logger.info("dropPlayer");
        try {
            Player player = em.find(Player.class, playerId);
            Team team = em.find(Team.class, teamId);

            team.dropPlayer(player);
            player.dropTeam(team);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public PlayerDetails getPlayer(String playerId) {
        logger.info("getPlayerDetails");
        try {
            Player player = em.find(Player.class, playerId);
            PlayerDetails playerDetails = new PlayerDetails(player.getId(), player.getName(), player.getPosition(), player.getSalary());
            return playerDetails;
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public List<PlayerDetails> getPlayersOfTeam(String teamId) {
        logger.info("getPlayersOfTeam");
        List<PlayerDetails> playerList;
        try {
            Team team = em.find(Team.class, teamId);
            playerList = this.copyPlayersToDetails((List<Player>) team.getPlayers());
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
        return playerList;
    }

    @Override
    public List<TeamDetails> getTeamsOfLeague(String leagueId) {
        logger.info("getTeamsOfLeague");
        List<TeamDetails> detailsList = new ArrayList<>();
        Collection<Team> teams;

        try {
            League league = em.find(League.class, leagueId);
            teams = league.getTeams();
        } catch (Exception ex) {
            throw new EJBException(ex);
        }

        for (Team team : teams) {
            TeamDetails teamDetails = new TeamDetails(team.getId(), team.getName(), team.getCity(), team.getColor());
            detailsList.add(teamDetails);
        }
        return detailsList;
    }

    @Override
    public List<PlayerDetails> getPlayersByPosition(String position) {
        logger.info("getPlayersByPosition");
        List<Player> players = null;

        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            if (cq != null) {
                Root<Player> player = cq.from(Player.class);

                // set the where clause
                cq.where(cb.equal(player.get(Player_.position), position));
                cq.select(player);
                TypedQuery<Player> q = em.createQuery(cq);
                players = q.getResultList();
            }
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public List<PlayerDetails> getPlayersByHigherSalary(String name) {
        logger.info("getPlayersByHigherSalary");
        List<Player> players = null;

        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            if (cq != null) {
                Root<Player> player1 = cq.from(Player.class);
                Root<Player> player2 = cq.from(Player.class);

                // create a Predicate object that finds players with a salary
                // greater than player1
                Predicate gtPredicate = cb.greaterThan(player1.get(Player_.salary), player2.get(Player_.salary));
                // create a Predicate object that finds the player based on
                // the name parameter
                Predicate equalPredicate = cb.equal(player2.get(Player_.name), name);
                // set the where clause with the predicates
                cq.where(gtPredicate, equalPredicate);
                // set the select clause, and return only unique entries
                cq.select(player1).distinct(true);
                TypedQuery<Player> q = em.createQuery(cq);
                players = q.getResultList();
            }
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public List<PlayerDetails> getPlayersBySalaryRange(double low, double high) {
        logger.info("getPlayersBySalaryRange");
        List<Player> players = null;

        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            if (cq != null) {
                Root<Player> player = cq.from(Player.class);

                // set the where clause
                cq.where(cb.between(player.get(Player_.salary), low, high));
                // set the select clause
                cq.select(player).distinct(true);
                TypedQuery<Player> q = em.createQuery(cq);
                players = q.getResultList();
            }
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public List<PlayerDetails> getPlayersByLeagueId(String leagueId) {
        logger.info("getPlayersByLeagueId");
        List<Player> players = null;

        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            if (cq != null) {
                Root<Player> player = cq.from(Player.class);
                Join<Player, Team> team = player.join(Player_.teams);
                Join<Team, League> league = team.join(Team_.league);

                // set the where clause
                cq.where(cb.equal(league.get(League_.id), leagueId));
                cq.select(player).distinct(true);
                TypedQuery<Player> q = em.createQuery(cq);
                players = q.getResultList();
            }
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public List<PlayerDetails> getPlayersBySport(String sport) {
        logger.info("getPlayersByLeagueId");
        List<Player> players = null;

        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            if (cq != null) {
                Root<Player> player = cq.from(Player.class);
                Join<Player, Team> team = player.join(Player_.teams);
                Join<Team, League> league = team.join(Team_.league);

                // set the where clause
                cq.where(cb.equal(league.get(League_.sport), sport));
                cq.select(player).distinct(true);
                TypedQuery<Player> q = em.createQuery(cq);
                players = q.getResultList();
            }
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public List<PlayerDetails> getPlayersByCity(String city) {
        logger.info("getPlayersByCity");
        List<Player> players = null;

        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            if (cq != null) {
                Root<Player> player = cq.from(Player.class);
                Join<Player, Team> team = player.join(Player_.teams);

                // set the where clause
                cq.where(cb.equal(team.get(Team_.city), city));
                cq.select(player).distinct(true);
                TypedQuery<Player> q = em.createQuery(cq);
                players = q.getResultList();
            }
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public List<PlayerDetails> getAllPlayers() {
        logger.info("getAllPlayers");
        List<Player> players = null;

        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            if (cq != null) {
                Root<Player> player = cq.from(Player.class);

                cq.select(player);
                TypedQuery<Player> q = em.createQuery(cq);
                players = q.getResultList();
            }
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public List<PlayerDetails> getPlayersNotOnTeam() {
        logger.info("getPlayersNotOnTeam");
        List<Player> players = null;

        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            if (cq != null) {
                Root<Player> player = cq.from(Player.class);

                // set the where clause
                cq.where(cb.isEmpty(player.get(Player_.teams)));
                cq.select(player).distinct(true);
                TypedQuery<Player> q = em.createQuery(cq);
                players = q.getResultList();
            }
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public List<PlayerDetails> getPlayersByPositionAndName(String position, String name) {
        logger.info("getPlayersByPositionAndName");
        List<Player> players = null;

        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            if (cq != null) {
                Root<Player> player = cq.from(Player.class);

                // set the where clause
                cq.where(cb.equal(player.get(Player_.position), position), cb.equal(player.get(Player_.name), name));
                cq.select(player).distinct(true);
                TypedQuery<Player> q = em.createQuery(cq);
                players = q.getResultList();
            }
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public List<LeagueDetails> getLeaguesOfPlayer(String playerId) {
        logger.info("getLeaguesOfPlayer");
        List<LeagueDetails> detailsList = new ArrayList<>();
        List<League> leagues = null;

        try {
            CriteriaQuery<League> cq = cb.createQuery(League.class);
            if (cq != null) {
                Root<League> league = cq.from(League.class);
                Join<League, Team> team = league.join(League_.teams);
                Join<Team, Player> player = team.join(Team_.players);

                cq.where(cb.equal(player.get(Player_.id), playerId));
                cq.select(league).distinct(true);
                TypedQuery<League> q = em.createQuery(cq);
                leagues = q.getResultList();
            }
        } catch (Exception ex) {
            throw new EJBException(ex);
        }

        if (leagues == null) {
            logger.log(Level.WARNING, "No leagues found for player with ID {0}.", playerId);
            return null;
        } else {
            for (League league : leagues) {
                LeagueDetails leagueDetails = new LeagueDetails(league.getId(), league.getName(), league.getSport());
                detailsList.add(leagueDetails);
            }

        }
        return detailsList;
    }

    @Override
    public List<String> getSportsOfPlayer(String playerId) {
        logger.info("getSportsOfPlayer");
        List<String> sports = new ArrayList<>();

        try {
            CriteriaQuery<String> cq = cb.createQuery(String.class);
            if (cq != null) {
                Root<Player> player = cq.from(Player.class);
                Join<Player, Team> team = player.join(Player_.teams);
                Join<Team, League> league = team.join(Team_.league);

                // set the where clause
                cq.where(cb.equal(player.get(Player_.id), playerId));
                cq.select(league.get(League_.sport)).distinct(true);
                TypedQuery<String> q = em.createQuery(cq);
                sports = q.getResultList();
            }
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
        return sports;
    }

    @Override
    public void createTeamInLeague(TeamDetails teamDetails, String leagueId) {
        logger.info("createTeamInLeague");
        try {
            League league = em.find(League.class, leagueId);
            Team team = new Team(teamDetails.getId(), teamDetails.getName(), teamDetails.getCity(), teamDetails.getColor());
            em.persist(team);
            team.setLeague(league);
            league.addTeam(team);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public void removeTeam(String teamId) {
        logger.info("removeTeam");
        try {
            Team team = em.find(Team.class, teamId);

            Collection<Player> players = team.getPlayers();
            for (Player player : players) {
                player.dropTeam(team);
            }

            em.remove(team);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public TeamDetails getTeam(String teamId) {
        logger.info("getTeam");
        TeamDetails teamDetails;

        try {
            Team team = em.find(Team.class, teamId);
            teamDetails = new TeamDetails(team.getId(), team.getName(), team.getCity(), team.getColor());
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
        return teamDetails;
    }

    @Override
    public void createLeague(LeagueDetails leagueDetails) {
        logger.info("createLeague");
        try {
            if (leagueDetails.getSport().equalsIgnoreCase("soccer") || leagueDetails.getSport().equalsIgnoreCase("swimming") || leagueDetails.getSport().equalsIgnoreCase("basketball") || leagueDetails.getSport().equalsIgnoreCase("baseball")) {
                SummerLeague league = new SummerLeague(leagueDetails.getId(), leagueDetails.getName(), leagueDetails.getSport());
                em.persist(league);
            } else if (leagueDetails.getSport().equalsIgnoreCase("hockey") || leagueDetails.getSport().equalsIgnoreCase("skiing") || leagueDetails.getSport().equalsIgnoreCase("snowboarding")) {
                WinterLeague league = new WinterLeague(leagueDetails.getId(), leagueDetails.getName(), leagueDetails.getSport());
                em.persist(league);
            } else {
                throw new IncorrectSportException("The specified sport is not valid.");
            }
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public void removeLeague(String leagueId) {
        logger.info("removeLeague");
        try {
            League league = em.find(League.class, leagueId);
            em.remove(league);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    public LeagueDetails getLeague(String leagueId) {
        logger.info("getLeague");
        LeagueDetails leagueDetails;

        try {
            League league = em.find(League.class, leagueId);
            leagueDetails = new LeagueDetails(league.getId(), league.getName(), league.getSport());
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
        return leagueDetails;
    }

    @Override
    public void clearAllEntities() {
        em.createQuery("DELETE FROM Game").executeUpdate();
        em.createQuery("DELETE FROM Player").executeUpdate();
        em.createQuery("DELETE FROM Team").executeUpdate();
        em.createQuery("DELETE FROM League").executeUpdate();
    }

    private List<PlayerDetails> copyPlayersToDetails(List<Player> players) {
        List<PlayerDetails> detailsList = new ArrayList<>();
        for (Player player : players) {
            PlayerDetails playerDetails = new PlayerDetails(player.getId(), player.getName(), player.getPosition(), player.getSalary());
            detailsList.add(playerDetails);
        }
        return detailsList;
    }

    @Override
    public List<GameDetails> getAllLeagueGames(String leagueId) {
        logger.info("getGamesOfLeague");
        List<GameDetails> detailsList = new ArrayList<>();
        Collection<Game> games;

        try {
            League league = em.find(League.class, leagueId);
            games = league.getGames();
        } catch (Exception ex) {
            throw new EJBException(ex);
        }

        for (Game game : games) {
            detailsList.add(new GameDetails(game.getId(), game.getTeam1().getId(), game.getTeam2().getId(), game.getGoalsTeam1(), game.getGoalsTeam2()));
        }
        return detailsList;
    }

    @Override
    public List<GameDetails> getAllLeagueGamesForTeam(String leagueId, String teamId) {
        logger.info("getAllLeagueGamesForTeam");
        List<GameDetails> detailsList = new ArrayList<>();
        Collection<Game> games;

        try {
            League league = em.find(League.class, leagueId);
            games = league.getGames();
        } catch (Exception ex) {
            throw new EJBException(ex);
        }

        for (Game game : games) {
            if (game.getTeam1().getId().equals(teamId) || game.getTeam2().getId().equals(teamId)) {
                detailsList.add(new GameDetails(game.getId(), game.getTeam1().getId(), game.getTeam2().getId(), game.getGoalsTeam1(), game.getGoalsTeam2()));
            }
        }
        return detailsList;
    }

    public void createGameInLeague(GameDetails gameDetails, String leagueId) {
        logger.info("createGameInLeague");
        try {
            League league = em.find(League.class, leagueId);
            Team team1 = em.find(Team.class, gameDetails.getTeam1());
            Team team2 = em.find(Team.class, gameDetails.getTeam2());
            Game game = new Game();
            game.setId(gameDetails.getId());
            game.setTeam1(team1);
            game.setTeam2(team2);
            game.setGoalsTeam1(gameDetails.getGoalsTeam1());
            game.setGoalsTeam2(gameDetails.getGoalsTeam2());
            em.persist(game);
            game.setLeague(league);
            league.getGames().add(game);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    public List<StandingTeam> getLeagueStanding(String leagueId) {
        // returneaza numele echipei, numarul de puncte si lista de jucatori a echipei

        logger.info("getLeagueStanding");
        List<StandingTeam> standing = new ArrayList();
        try {
            League league = em.find(League.class, leagueId);
            Collection games = league.getGames();
            Collection teams = league.getTeams();

            Map <Team, Integer> teamPoints = new HashMap<>();

            // initialize points to 0
            for (Object t: teams) {
                Team team = (Team) t;
                teamPoints.put(team, 0);
            }

            // calculate points for each game
            for (Object g: games) {
                Game game = (Game) g;

                if (game.getGoalsTeam1() == game.getGoalsTeam2()) {
                    Team team1 = game.getTeam1();
                    Team team2 = game.getTeam2();
                    teamPoints.put(team1, teamPoints.get(team1)+1);
                    teamPoints.put(team2, teamPoints.get(team2)+1);
                }

                if (game.getGoalsTeam1() > game.getGoalsTeam2()) {
                    Team team1 = game.getTeam1();
                    teamPoints.put(team1, teamPoints.get(team1)+3);
                }

                if (game.getGoalsTeam1() < game.getGoalsTeam2()) {
                   Team team2 = game.getTeam2();
                   teamPoints.put(team2, teamPoints.get(team2)+3);
                }
            }

            for (Object t: teams) {
                Team team = (Team) t;

                standing.add(new StandingTeam(team, teamPoints.get(team)));
            }

            // sort the standing list by points
            standing.sort((st1, st2) -> Integer.compare(st2.getPoints(), st1.getPoints()));

        } catch (Exception ex) {
            throw new EJBException(ex);
        }
        return standing;
    }

    public List<TeamDetails> getAllTeams() {
        logger.info("getAllTeams");
        List<TeamDetails> detailsList = new ArrayList<>();
        List<Team> teams = null;

        try {
            CriteriaQuery<Team> cq = cb.createQuery(Team.class);
            if (cq != null) {
                Root<Team> team = cq.from(Team.class);

                cq.select(team);
                TypedQuery<Team> q = em.createQuery(cq);
                teams = q.getResultList();
            }

            for (Team team : teams) {
                TeamDetails teamDetails = new TeamDetails(team.getId(), team.getName(), team.getCity(), team.getColor());
                detailsList.add(teamDetails);
            }
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
        return detailsList;
    }
}
