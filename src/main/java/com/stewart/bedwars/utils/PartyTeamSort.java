package com.stewart.bedwars.utils;

import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayerManager;
import de.simonsator.partyandfriends.spigot.api.party.PartyManager;
import de.simonsator.partyandfriends.spigot.api.party.PlayerParty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class PartyTeamSort {

    public List<PAFParty> lstTeams;
    public int numPlayers ;
    public int teamIterator = 1;

    private List<PAFParty> lstPAFParties;
    private int gameTeamSize;
    private List<UUID> lstSinglePlayers;
    private int numTeamsRequired;
    private int maxTeamSize;
    private int minTeamSize;
    private List<UUID> lstAllPlayers;

    public  PartyTeamSort(List<UUID> lstAllPlayers,  int gameTeamSize) {
        this.lstAllPlayers = lstAllPlayers;
        this.numPlayers = lstAllPlayers.size();
        this.gameTeamSize = gameTeamSize;
        this.lstSinglePlayers = new ArrayList<>();
        this.lstPAFParties = new ArrayList<>();
        FillPartyList();
    }

    private void FillPartyList() {

        // this will be all the parties represented by players in the all player list
        List<PlayerParty> lstPlayerParty = new ArrayList<>();

        for (UUID uuid : lstAllPlayers) {
            System.out.println("Checking if player is in a party");
            PAFPlayer pafPlayer = PAFPlayerManager.getInstance().getPlayer(uuid);
            PlayerParty party = PartyManager.getInstance().getParty(pafPlayer);
            if (party != null) {
                System.out.println("Player is in a party");
                boolean partyAlreadyFound = false;

                for (PlayerParty p: lstPlayerParty){
                    if (p.isInParty(pafPlayer)) {
                        partyAlreadyFound = true;
                    }
                }
                if (!partyAlreadyFound) {
                    System.out.println("Adding party to list");
                    lstPlayerParty.add(party);
                } else {
                    System.out.println("Party is already in list");
                }
            } else {
                System.out.println("Player is not in a party, adding to single player list");
                lstSinglePlayers.add(uuid);
            }
        }
        // loop through each party and add the players from each (that are on the server) to a PAFParty object.
        int i = 0;
        for (PlayerParty party : lstPlayerParty) {
            PAFParty pafParty = new PAFParty();
            pafParty.partyPlayers = new ArrayList<>();
            pafParty.partyName = "party" + i;

            for (PAFPlayer p : party.getPlayers()) {
                if(lstAllPlayers.contains(p.getUniqueId())) {
                    System.out.println("Adding party player " + p.getName() + " to " + pafParty.partyName );
                    pafParty.partyPlayers.add(p.getUniqueId());
                } else {
                    System.out.println("Party player " + p.getName() + " is not in the game.");
                }
                System.out.println("Player in party is " + p.getName());
            }
            if(lstAllPlayers.contains(party.getLeader().getUniqueId())) {
                System.out.println("Adding party leader " + party.getLeader().getName() + " to " + pafParty.partyName );
                pafParty.partyPlayers.add(party.getLeader().getUniqueId());
            } else {
                System.out.println("Party leader " + party.getLeader().getName() + " is not in the game.");
            }
            lstPAFParties.add(pafParty);
            i+=1;
        }
    }


    public void FillTeams() {

        // this will hold the final teams
        lstTeams = new ArrayList<>();
        // find number of required teams for the game
        setNumTeamsRequired();
        // get the min and max number of players in a team, will only ever be 1 0 difference between them.
        setMinMaxPlayersPerTeam();
        // order parties list big > small
        OrderParties();
        // create the teams we will need to be filled
        CreateTeams();
        // loop through parties and see if any are same size as max team size
        // remember parties are ordered big - small so if max team size reduces it should work fien
        TeamPartyMatchingSize();

        System.out.println("All parties that match team size now dealt with, moving on to parties with larger or small player numbers");
        // may now have a few full teams, there will be no part-filled teams
        // all remaining parties have more or less players than the teams max or min values
        // team max and min values may be the same or 1 apart
        // get the first empty team, it will be null if all teams are already filled
        PAFParty teamLoop = null;
        if (getTeamsNotFull(lstTeams).size() > 0) {
            teamLoop = getTeamsNotFull(lstTeams).get(0);
        }
        if (teamLoop != null) {
            // loop Parties, always getting first & removing it from the list when the players have been allocated
            if (lstPAFParties.size() > 0) {
                do {
                    // if the current team is full, get the next empty one
                    // will be null if no more teams
                    if (teamLoop.isFull) {
                        System.out.println("team loop is full");
                        if (getTeamsNotFull(lstTeams).size() > 0) {
                            System.out.println("there are empty team(s)");
                            teamLoop = getTeamsNotFull(lstTeams).get(0);
                        } else {
                            System.out.println("there are no empty team(s)");
                            teamLoop = null;
                        }
                    }
                    // add all the players from this party to the team, if the party is larger tan a team, it will fill
                    // more than one
                    if (lstPAFParties.size() > 0) {
                        teamLoop = AddPartyPlayersToTeams(teamLoop);
                    }

                    if (teamLoop.partyPlayers.size() < maxTeamSize) {
                        System.out.println("Need to add more players to the team");
                        teamLoop = FillRemainingTeamSpaces(teamLoop);
                    } else {
                        System.out.println("Don't need to add anymore players to the team, setting it to full");
                        teamLoop = setTeamLoopFull(teamLoop);
                    }

                } while (lstPAFParties.size() > 0 && teamLoop != null);
            }

            System.out.println("all parties have been put into teams, adding non-party players");
            teamLoop = FillRemainingWithNonPartyPlayers(teamLoop);

        } // all teams were filled by parties the same size

        System.out.println("ordered");

    }

    private PAFParty FillRemainingWithNonPartyPlayers(PAFParty teamLoop) {
        if (lstSinglePlayers.size() > 0) {
            do {
                if (teamLoop.isFull) {
                    if (getTeamsNotFull(lstTeams).size() > 0) {
                        teamLoop = getTeamsNotFull(lstTeams).get(0);
                    } else {
                        // no more teams
                        break;
                    }
                }
                if (teamLoop.partyPlayers.size() < (minTeamSize +1)) {
                    addNonPartyPlayer(teamLoop);
                    if (teamLoop.partyPlayers.size() == minTeamSize) {
                        setTeamLoopFull(teamLoop);
                    }
                }
            } while (lstSinglePlayers.size() > 0);
        }
        return teamLoop;
    }

    private PAFParty FillRemainingTeamSpaces(PAFParty teamLoop) {
        // First try to fill the remaining spaces with non-party members (if there are enough to fill it)
        int playersNeeded = minTeamSize - teamLoop.partyPlayers.size();
        if (lstSinglePlayers.size() > playersNeeded) {
            do {
                addNonPartyPlayer(teamLoop);
            } while (teamLoop.partyPlayers.size() < (minTeamSize) && lstSinglePlayers.size() > 0);
        }
        // check if the team is now full of does it need more players
        if (teamLoop.partyPlayers.size() == maxTeamSize || teamLoop.partyPlayers.size() == minTeamSize) {
            // adding non-party players filled the team
            teamLoop = setTeamLoopFull(teamLoop);
        } else {
            // adding non-part players did not fill the team.
            // check if any other partys will fill the space.
            List<PAFParty> lstPartysWithPlayersThatFit = getPartysWithPlayersOrLess(maxTeamSize - teamLoop.partyPlayers.size());
            if (lstPartysWithPlayersThatFit.size() > 0) {
                // add this parties players to the team
                for (UUID player2 : lstPartysWithPlayersThatFit.get(0).partyPlayers) {
                    System.out.println("Adding party player to team");
                    teamLoop.partyPlayers.add(player2);
                    // decrease the number of players as I'm only concerned with those not already sorted
                    numPlayers -= 1;
                }
                // remove this party from the list
                lstPAFParties.remove(lstPartysWithPlayersThatFit.get(0));
            }

            // If the team is still not full I need to take some players from non-party list or if thats empty,
            // the next party even if it's larger than the remaining space in this team.
            if (teamLoop.partyPlayers.size() == minTeamSize || teamLoop.partyPlayers.size() == maxTeamSize) {
                teamLoop = setTeamLoopFull(teamLoop);
            } else {
                // first try adding non-party players, could still be some left at this point
                if (lstSinglePlayers.size() > 0) {
                    do {
                        addNonPartyPlayer(teamLoop);
                    } while (teamLoop.partyPlayers.size() < (minTeamSize + 1) && lstSinglePlayers.size() > 0);
                }
                // if still not full add players from other partie
                if (teamLoop.partyPlayers.size() == minTeamSize || teamLoop.partyPlayers.size() == maxTeamSize) {
                    teamLoop = setTeamLoopFull(teamLoop);
                } else {
                    PAFParty party = lstPAFParties.get(0);
                    do {
                        // add first player form party to team
                        teamLoop.partyPlayers.add(party.partyPlayers.get(0));
                        // remove player from party
                        party.partyPlayers.remove(0);
                        numPlayers -= 1;

                    } while (teamLoop.partyPlayers.size() < maxTeamSize);
                }
            }
        }
        return  teamLoop;
    }

    private void TeamPartyMatchingSize() {
        Iterator<PAFParty> party = lstPAFParties.iterator();
        while (party.hasNext()) {
            PAFParty n = party.next();
            // check if the party player count is the same as the max or min team size count.
            if (n.partyPlayers.size() == maxTeamSize || n.partyPlayers.size() == minTeamSize) {
                // put this party in a team
                System.out.println("Party " + n.partyName + " hax maxTeamTize or minTeamSize players " + n.partyPlayers.size());
                addPartyToTeamFull(n);
                // remove it from the party list
                System.out.println("removing " + n.partyName + " from party list");
                party.remove();
                teamIterator += 1;
                // need to recheck max min players per team, it may have gone down 1
                setMinMaxPlayersPerTeam();
            }
        }
    }

    private PAFParty AddPartyPlayersToTeams(PAFParty teamLoop) {
        for (UUID player : lstPAFParties.get(0).partyPlayers) {
            System.out.println("party " + lstPAFParties.get(0).partyName + " has " + lstPAFParties.get(0).partyPlayers.size() + " players");
            System.out.println("team " + teamLoop.partyName + " has " + teamLoop.partyPlayers.size() + " players");
            if (teamLoop.partyPlayers.size() == minTeamSize) { // do this chakc & update team before adding players
                System.out.println("Team players now full, will add remaining party players to another team");
                teamLoop = setTeamLoopFull(teamLoop);
                // get a new team, if we still have players there should be an emoty team
                teamLoop = getTeamsNotFull(lstTeams).get(0);
            }
            System.out.println("Adding party player to team");
            teamLoop.partyPlayers.add(player);
            // decrease the number of players as I'm only concerned with those not already sorted
            numPlayers -= 1;
        }
        // all the players from that party has been added to a team(s)
        // remove the party from the list
        System.out.println("All party players have been added to teams, removing this party");
        System.out.println("parties count: " +lstPAFParties.size());
        lstPAFParties.remove(0);
        System.out.println("after remove parties count: " +lstPAFParties.size());
        System.out.println("Team has " + teamLoop.partyPlayers.size() + " players");
        return teamLoop;
    }


    private PAFParty setTeamLoopFull(PAFParty teamLoop) {
        teamLoop.isFull = true;
        // decrease number of teams required (we just filled one)
        numTeamsRequired -= 1;
        // if max and min are different - recalculate them
        if (maxTeamSize != minTeamSize) {
            setMinMaxPlayersPerTeam();
        } else {
            System.out.println("max and min are same size, no need to recalculate");
        }
        return teamLoop;
    }

    private void addNonPartyPlayer(PAFParty teamLoop) {
        System.out.println("adding non party player " + lstSinglePlayers.get(0) + " to team");
        teamLoop.partyPlayers.add(lstSinglePlayers.get(0));
        // remove non party player
        lstSinglePlayers.remove(0);
        numPlayers -= 1;
    }

    private void addPartyToTeamFull(PAFParty party) {
        PAFParty team = getTeamsNotFull(lstTeams).get(0);
        System.out.println("Got empty team " + teamIterator);
        team.partyName = "team " + teamIterator;
        System.out.println("Adding players from " + party.partyName + " to " + team.partyName);
        for (UUID uuid : party.partyPlayers) {
            team.partyPlayers.add(uuid);
            // decrease the number of players as I'm only concerned with those not already sorted
            numPlayers -= 1;

        }
        // same with number of teams
        numTeamsRequired -= 1;
        team.isFull = true;
    }

    public List<PAFParty> getTeamsNotFull(List<PAFParty> lst) {

        List<PAFParty> ppp = lst.stream().filter(
                t -> t.isFull == false
        ).collect(Collectors.toList());
        return  ppp;
    }

    public List<PAFParty> getPartysWithPlayersOrLess(int intPlayers) {

        List<PAFParty> ppp = lstPAFParties.stream().filter(
                t -> t.partyPlayers.size() <= intPlayers
        ).collect(Collectors.toList());

        // sort with highest number first
        Collections.sort(ppp, Comparator.comparing(PAFParty::getSize));
        Collections.reverse(ppp);

        return  ppp;
    }

    private void CreateTeams() {
        for (int j= 1; j < (numTeamsRequired +1); j++) {
            PAFParty party = new PAFParty();
            party.partyName = "Team " + j;
            party.partyPlayers = new ArrayList<>();
            lstTeams.add(party);
        }
        System.out.println("Created " +lstTeams.size() + " teams");
    }

    private void OrderParties() {
        if (lstPAFParties.size() > 0) {
            Collections.sort(lstPAFParties, Comparator.comparing(PAFParty::getSize));
            Collections.reverse(lstPAFParties);
        }
    }



    private void setNumTeamsRequired() {
        // for testing
        if (numPlayers == 1) {
            numTeamsRequired = 1;
        } else {
            numTeamsRequired = (int) Math.ceil((double)numPlayers / gameTeamSize);
            System.out.println("Num teams required = " + numTeamsRequired);
            if (numTeamsRequired == 1) {
                System.out.println("Num teams required set to 2, 1 is no use unless testing");
                numTeamsRequired = 2;
            }
        }

    }

    private void setMinMaxPlayersPerTeam() {
        maxTeamSize = (int) Math.ceil((double)numPlayers / numTeamsRequired );
        minTeamSize = (int) Math.floor((double)numPlayers / numTeamsRequired );
        System.out.println("Max team size = " + maxTeamSize);
        System.out.println("Min team size = " + minTeamSize);
    }

    public class PAFParty {
        public String partyName;
        public List<UUID> partyPlayers;
        public boolean isFull = false;
        public int getSize() {
            return partyPlayers.size();
        }
    }




}




