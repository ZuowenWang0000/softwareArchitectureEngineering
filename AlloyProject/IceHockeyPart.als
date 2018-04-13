open GeneralPart

///////////////////////////////////////// PHASES //////////////////////////////////////////////////

abstract sig EliminationRound extends Phase {
}{
	// Make sure that only Ice Hockey performances are part of this phase (no performances from other disciplines)
	all p: getPerformances[this] | p in OneVOneGame

}
  
one sig QuarterFinals extends EliminationRound {}{
	// 4 matches in the quarter-finals
	#getPerformances[this] = 4
	// Next comes the semi-finals
	one successor
	successor in SemiFinals
	// 8 teams play in the quarter-finals
	#getParticipants[getPerformances[this]] = 8
}

one sig SemiFinals extends EliminationRound {}{
	// 2 matches in the semi-finals
	#getPerformances[this] = 2
	// Next come the rank-3 game and the finals
	#successor = 2
	some s: successor | s in Finals
	some s: successor | s in Rank3Game
	// 4 teams play in the semi-finals
	#getParticipants[getPerformances[this]] = 4
}

one sig Rank3Game extends EliminationRound {}{
	// 1 match to determine who gets the bronze
	#getPerformances[this] = 1
	#getParticipants[getPerformances[this]] = 2
	no successor
}

one sig Finals extends EliminationRound {}{
	// 1 match to determine the gold and the silver
	#getPerformances[this] = 1
	#getParticipants[getPerformances[this]] = 2
	no successor
}

/////////////////////////////////////// DISCIPLINE ////////////////////////////////////////////////

sig IceHockey extends Discipline {}

////////////////////////////////////// PERFORMANCE ////////////////////////////////////////////////

sig OneVOneGame extends Performance {
	// Every 1v1 game results in a winner
	winner: one IceHockeyTeam
}{
	// All Ice Hockey performances are 1v1 matches between 2 Teams
	#getParticipants[this] = 2
	// Winner is one of the players
	winner in getParticipants[this]
}

////////////////////////////////////////// TEAM ///////////////////////////////////////////////////

sig IceHockeyTeam extends Team {
	// There are some athletes on the ice
	athletesOnIce: set Athlete,
}
{
	// Each ice hockey team contains 2 members (simplified for alloy)
	#getMembers[this] = 2
	// There is one member on the ice (simplified for alloy)
	athletesOnIce in getMembers[this]
	#athletesOnIce = 1
}

///////////////////////////////////////// EVENT ///////////////////////////////////////////////////

abstract sig IceHockeyTournament extends Event {}
{
	// Only ice hockey teams play in the tournament
	all t: getEventParticipants[this] | t in IceHockeyTeam
	// All participants playing in one of the performances are participating in the event (no "external" players)
	all t: getParticipants[getPerformances[getPhases[this]]] | t in getEventParticipants[this]
	// Ranking
	#getGoldMedals[this] = 1
	#getSilverMedals[this] = 1
	#getBronzeMedals[this] = 1
}

one sig WomensIceHockeyTournament extends IceHockeyTournament {}
{
	// All athletes are female
	all t: getEventParticipants[this] | all a: getMembers[t] | a in FemaleAthlete
}

////////////////////////////////////////// FACTS /////////////////////////////////////////////////

// No performances shared among different phases
//fact {all disj e1, e2: EliminationRound | (getPerformances[e1] & getPerformances[e2]) = none}
// --> Should follow from block under Performance in the general part

// Make sure all performances of a later phase are after the performances of the previous phase
//fact {all disj e1, e2: EliminationRound | e1 in e2.successor => isBefore[getHighestEndingTime[e2], getLowestStartingTime[e1]]}
// --> Must already hold in general model

// the teams in sucessor is subset of teams in predecessor
fact {all disj e1, e2: EliminationRound | e1 in e2.successor => (getTeamFromPhase[e1] in getTeamFromPhase[e2])}

// any two teams in a performance in a phase, can not come from a same performance in the previous phase
//fact {
//	all disj e1, e2: EliminationRound | e1 in e2.successor => 
//	((all disj t1, t2: getTeamFromPhase[e1] | all p: getPerformances[e2]| (t1 in getParticipants[p] => t2 not in getParticipants[p]))
//	and (all p1: Finals | all p2: Rank3Game | all t: IceHockeyTeam| t in getTeamFromPhase[p1] => t not in getTeamFromPhase[p2]))
//}
// Except for the rank-3 game, all the winners proceed to the next stage
fact{all e1, e2: EliminationRound| e2 in e1.successor => e2 in Rank3Game || getParticipants[getPerformances[e2]] = getPerformances[e1].winner}
//fact{all q: QuarterFinals, s: SemiFinals | getParticipants[getPerformances[s]] = getPerformances[q].winner }

// The finals and the rank-3-game together contain all participants from the semifinals
fact{all p: SemiFinals | getTeamFromPhase[p.successor] = getTeamFromPhase[p]}


// Make sure the medals match up with the winners
fact {GoldMedal in getMedalsForTeam[getPerformances[Finals].winner]}
fact {SilverMedal in getMedalsForTeam[(getPhaseParticipants[Finals])]} /*1 Medal per team and event ensures that it is not the winner*/
fact {BronzeMedal in getMedalsForTeam[getPerformances[Rank3Game].winner]}

// In Ice Hockey, at most 1 Team per Country per Event (1 for Women's Tournament)
fact {all c: Country, e: IceHockeyTournament | #getTeamsForCountryAndEvent[c, e] <= 1}

///////////////////////////////// FUNCTIONS & PREDICATES /////////////////////////////////////////

/*template*/ pred isAmongBest[t: Team, p: Phase] { 
	t in getPerformances[p].winner
}

//////////////////////////// PREDICATES FOR SHOWING INSTANCES ////////////////////////////////////

// In Ice Hockey, there are always one gold medal, one silver medal, and one bronze medal.
pred static_instance_6 {}
run static_instance_6 for 10 but exactly 8 IceHockeyTeam, exactly 16 Athlete
