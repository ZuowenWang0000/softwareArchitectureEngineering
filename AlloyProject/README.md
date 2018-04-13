TODO
====

Item                                                                                                   | Done?     
-------------------------------------------------------------------------------------------------------|-------
1. OneVOneGame only allows two teams                                                                   | Yes
2. cardinality of performances for each phase (quater ...)                                             | Yes
3. no OneVOneGame is shared between different phases                                                   | Yes
4. "follow" relation connecting phases                                                                 | Yes
5. no team play more than once in one phase                                                            | Yes
6. a point field in IHTeam   	                                                                       | Yes
   6.1  point logic (losers of quarter get 0, winners get 1 etc)                                       | Not Necessary                                  |
7. the logic of deciding the winner of a performance(according to the score sig)                       | Yes (although nothing to do with Score)
8. pred isAmongBest                                                                                    | Yes
9. number of athelets on ice (in an abstractive way)                                                   | Yes
10. assignment medal according to the results of Rank3Match                                            | Yes
11. Make sure that every IHTeam that competes in a 1v1 match is actually participating in the event    | Yes
12. Make sure that the number of points matches up with the medals                                     | Not necessary
13. Make sure that the times match up (quarter-final 1v1 games before semi 1v1 games etc.)             | Yes
14. only the winners in a phase could enter the next phase (except for rank3 game)		       | Yes
15. two point fields in sig IHTeam & trace to show increment logic of Points 			       | Not necessary
16: More medals than events found: ensure one medal per event per team.                                | Yes
17: Only teams that participate in Events should be able to win the medals from that event             | Yes
18: There should be some phases with multiple performances (--> OK, just question limits)              | Not necessary
19: All the Performances of a phase must be terminated before the next phase can start                 | Yes
20: Why do we not allow for two performances to have the same score? (I think of "1:2", which sounds reusable to me) | Because the UML diagram specifies a 1:1 relationship
21: The times of the performances sohuld be completey disjunct: can be done one one line               | Yes
22: General Part: Teams playing in performances of events must be in participants of event             | Yes
    Part 2: Remove this fact from IceHockey, if approved                                               | Yes
23: Participants cannot be in two teams in the same event                                              | Yes
24: All teams participate in at least one performance                                                  | Yes
24: Disentangle progression logic from complete semifinals to successor logic                          | Yes
25: Loosen the silver medal restriction in Ice Hockey                                                  | Yes
26: Logic about who proceeds to the next round                                                         | Yes
