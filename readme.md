Rock Paper Scissor Battle
-------------------------

Classic game of Rock/Paper/Scissor, in stategy board game form.

The backend API is implemented using Scala and Akka HTTP.

The frontend is a React and Typescript.


Rules
-----
To win, capture the other team's flag.

When two tokens collide, combat begins:

- Only Rock, Paper and Scissor can be moved
- Rock beats Scissor
- Scissor beats Paper
- Paper beats Rock
- If both tokens are the same type, everyone loses
- If the defending token is a Bomb, every loses
- If the defending token is the Flag, the attacker wins the game
- If a player runs out of movable tokens, the other player wins


Running Locally
---------------
Requires an active Redis instance.  For local development, you can use Docker:

```
docker run --name redis-rps -d -p 6379:6379 redis
```

