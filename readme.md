Rock Paper Scissor Battle
-------------------------

Classic game of Rock/Paper/Scissor, in stategy board game form.

The backend API is implemented using Scala and Akka HTTP.

The frontend is a React and Typescript.

Play it live at https://rock-paper-scissor-battle.com/

Handdrawn CSS from https://fxaeberhard.github.io/handdrawn.css/

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

To run the API server, you need to run with the following env vars exported:
```
cd ./api
export SESSION_KEY=SESSION_KEY_HERE
export GOOGLE_CLIENT_ID=GOOGLE_CLIENT_ID_HERE
export GOOGLE_CLIENT_SECRET=GOOGLE_CLIENT_SECRET_HERE
sbt start
```

To run the UI
```
cd ./frontend
yarn
yarn start
```

Packaging and Deployment
------------------------

run the script
```
./publish.sh
```

See https://www.freecodecamp.org/news/how-to-dockerise-a-scala-and-akka-http-application-the-easy-way-23310fc880fa/

See https://devcenter.heroku.com/articles/deploying-scala
