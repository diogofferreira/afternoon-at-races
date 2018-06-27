# An afternoon at the races

This problem was introduced in the context of the Distributed Systems course 
(Computer and Telematics Engeneering at University of Aveiro) and consists in 
designing a concurrent and two distributed solutions of the given problem.

## Preamble

Events portray the activities that go by during a typical afternoon at a hippic
center, somewhere at the outskirts of Aveiro. There are four main locations: 
the track where the races take place, the stable wherethe horses rest waiting 
their turn to enter the competition, the paddock where the horses are paraded 
before the spectators, and the betting center where the spectators place their 
bets on the winning horse.

There are three kinds of intervening entities: the pairs horse / jockey 
participating in the races, the spectators watching the races and placing bets
on the horse they hope to win and the broker who accepts the bets, pays the 
dividends in case of victory and manages in a general manner all the operations
that take place.

`K` races are run during the afternoon, each with `N` competitors. 
`M` spectators are present. The activities are organized as described below

* the broker announces the next race;
* the participating horses are paraded at the paddock by the jockeys;
* the spectators, after observing the horses and thinking about their winning 
chances, go to the betting center to place their bet;
* the race takes place and one or more horses are declared winners;
* when somebody wins, he or she goes to the betting center to collect the gains.

At the end of the afternoon, the spectators meet at the bar to have a drink 
and talk about the events that took place.

Each race is composed of a sequence of position increments of the intervening 
horse / jockey pairs according to the following rules
* the track distance for the race `k`, with `k = 0, 1, ... , K-1`, is `D_k` 
units of length;
* each horse / jockey `C_nk`, with `n = 0, 1, ... , N-1` and `k = 0, 1, ... , K-1` 
carries out a single positionincrement per iteration by moving randomly 1 to `P_nk`
length units along its path
* the maximum value `P_nk` is specific of a given horse, because they are not 
all equal, some being more agile and faster than others;
* the horse / jockey pairs move in parallel paths and may be side by side or 
overtake one another;
* the winner is the pair horse / jockey `C_nk`, with `n = 0, 1, ... , N-1` and 
`k = 0, 1, ... , K-1`, which, after the completion of an iteration and having 
overtaken the finishing line, has a position with the highest value;
* in case of a draw, all the horse / jockey pairs with the highest position 
value are declared winners; the dividends to be received are inversely 
proportional to their number and rounded to unity.

Assuming there are five races, each having four competitors and that the number
of spectators is also four.

## Active entities life cycles

### Broker

![Broker life cycle](https://i.imgur.com/SqnSJtb.jpg)

### Horse / Jockey pair

![Horse Jockey pair life cycle](https://i.imgur.com/O3vR633.jpg)

### Spectator

![Spectator life cycle](https://i.imgur.com/cFy5Tlh.jpg)

## Shared Regions

The following shared memory regions will be accessed by the active entities in
mutual exclusion manner:

* General Repository
* Stable
* Control Centre
* Paddock
* Racing Track
* Betting Centre

In the concurrent solution, all of them will be shared memory regions inside 
the same process addressing space, however, in the distributed solutions, they'll
all be running in a different machine (they can also be run on the same, if
configured with that purpose).

## Solutions

To run all the solutions, Java 8 is needed on all the used machines.

### Concurrent Solution

This solution is intended to be run locally and it serves as the base for the
distributed versions, since both of them use a *Server Replication* approach.

To run this solution:

```
cd afternoon-at-races-concurrent
javac -d out -sourcepath src src/main/AnAfternoonAtTheRaces.java
java -cp out main.AnAfternoonAtTheRaces
```

### Distributed Solution (message passing)

This solution is based on the previous one, since it reutilizes the concurrent
code to create multiple proxy agents at the servers to serve multiple clients'
requests.

Active entities and clients communicate via message passing through TCP sockets.

It is already provided a script to automatically deploy the application in 
different machines. Make sure to change the addresses and ports of the remote 
machines you use to deploy it. Then, just run:

```
cd afternoon-at-races-distributed
./deploy.sh
```

### Distributed Solution (Remote Method Invocation)

Java already provides a framework to call methods on remote objects through TCP.
Therefore, the previous solution was adapted to implement RMI. However, in order
to successfully deploy this solution, an HTTP server (Apache, NGINX, etc.) must
be configured to serve static files over HTTP (`*.class` files).

Just like the previous solution, do not forget to modify the deploy script to 
your remote machines.

`
cd afternoon-at-races-rmi
./deploy.sh
`

Diogo Ferreira & Pedro Martins - 2018
