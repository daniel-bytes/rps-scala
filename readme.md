Rock Paper Scissor Battle
-------------------------

Classic game of rock-paper-scissor meets capture-the-flag stategy board game.

The backend API is implemented using Scala and Akka HTTP.

The frontend is React and Typescript.

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

Backend Architecture
--------------------

The API server is a Scala application, using the Akka HTTP framework for HTTP routing.  The majority of the code is framework agnostic, and relies very little on actors.

There are a few "layers" of services and models:

### HTTP Layer

Starting at the `ApplicationServer` object, the Akka `ActorSystem` and Akka HTTP routes are configured, with the main routing logic spread across a few classes that manage routes:

- `HomeRoutes`
- `GameRoutes`
- `SessionRoutes`

This layer is just responsible for handling HTTP requests and responses, converting data between API and domain models and interacting with the service layer.

Besides routes, there is a config package which parses the Typesafe config file and loads it into config model objects.

### Domain Layer

#### Services

The service layer is where the application interaction logic lives, as well as persistance and session management.  All code here is framework agnostic; it should just as easily be runnable from a CLI or GUI application as it is from a web app.

The `GameService` handles the main interactions a player of the game would require, which includes things like fetching all active games they are playing, fetching an active game by id, and taking their next turn.  All public methods are asynchronous and return `Result` type which is a `Future[Either[ApplicationError, T]]`. No exceptions are thrown for any validation or business logic errors.

The `GameRepository` manages game storage.  Currently there are two implementations:
- `InMemoryGameRepository` - used for testing
- `RedisGameRepository` - used when running the actual application

All underlying game logic is handled by the Engine layer.

The `AuthenticationService` handles user authentication and session management. There are two implementations, one for anonymous users and another for logins with Google.  Currently the application only runs with anonymous requests; the Google OAuth service was the original version but no longer used as there is no real need for actual logins or user identity in the game as of now. It remains in the codebase in case of future need, as well as to show a reference implementation of how to handle server side Google auth in Scala.

#### Models

The domain models are the data model layer of the application, representing core entities like Games, Game Boards, Players and Tokens.

#### Rule Engines

The core game mechanics and business rules are handled by the rule engine layer. These types are pure functions that work strictly on domain models or other rule engine types and have no concept of storage concerns or asynchronous actions. This is the most tested layer of the application. It is a separate lower level from the services layer because the services layer is meant for dealing with real world concerns like storage and IO.

Frontend Architecture
---------------------

### UI - React

The frontend UI application is implemented in Typescript using the React framework.  It follows a fairly common style of a top-level `index.tsx` housing a top-level `App.tsx` component, which houses the application shell. Individual pages and page components live under the `components` directory. Components receive data from a mix of custom props or the observed `ApplicationStore` service.

### State Management - MobX

State management is handled by the MobX framework. A top-level `ApplicationStore` service type contains all of the observable state of the application, as well as most of the core business logic for interacting with the API layer.

### Models

The `models` directory houses the core API data models, expressed as Typescript interfaces.

### Services

Outside of the MobX `ApplicationStore`, a number of other services exist.

- `ApiClient` - service class that manages REST calls on top of the `fetch` 
- `GameEngine` - service class that manges client side validation rules when the player interacts with the game
- `GameService` - service class for interacting with the API, wrapping each API call in an async method and converting into typed models
- `GoogleAuthService` - service class that wraps the Google OAuth API.  Not currently in use
- `SessionService` - service class for handling anonymous session management

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
