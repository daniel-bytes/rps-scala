import * as React from 'react'
import { observer, inject } from 'mobx-react'
import { IApplicationStore } from '../services/ApplicationStore'
import ErrorBox from './ErrorBox'
import { CombatSummary, Game } from '../models/GameModels'

interface Props {
  applicationStore?: IApplicationStore
}

interface CombatNotificationProps {
  combat: CombatSummary
  theirName: string
  yourMove: boolean
}

interface CombatNotificationsProps {
  game: Game | null
}

interface SubtitleProps {
  applicationStore: IApplicationStore
}

const CombatNotification: React.FunctionComponent<CombatNotificationProps> = (props) => {
  const yourToken = props.yourMove ? props.combat.attackerTokenType : props.combat.defenderTokenType
  const youAreAttacker = props.combat.attackerTokenType === yourToken

  const result = props.combat.winnerTokenType 
    ? (<span>{youAreAttacker ? 'Your' : 'Their'} <em className={youAreAttacker ? 'player-token-name' : 'other-player-token-name'}>{props.combat.winnerTokenType}</em> wins!</span>)
    : (<span> Everybody loses</span>)
  
  return (
    <div>
      { props.yourMove ? 'Your ' : `${props.theirName}'s `}
      <em className={props.yourMove ? 'player-token-name' : 'other-player-token-name'}>{props.combat.attackerTokenType}</em> attacked
      { props.yourMove ? ` ${props.theirName}'s ` : ' your '}
      <em className={props.yourMove ? 'other-player-token-name' : 'player-token-name'}>{props.combat.defenderTokenType}</em> - { result }
    </div>
  )
}

const CombatNotifications: React.FunctionComponent<CombatNotificationsProps> = (props) => {
  const game = props.game

  if (game) {
    const combats = game.recentMoves.filter(x => x.combatSummary)

    if (combats.length > 0) {
      const combatNotifications = combats.map(x => (
        <CombatNotification
          key={`combat:${x.from.x}:${x.from.y}:${x.to.x}:${x.to.y}`}
          combat={x.combatSummary!} 
          theirName={game.otherPlayerName}
          yourMove={game.playerId === x.playerId} />
      ))

      return <div>{combatNotifications}</div>
    }
  }

  return <span></span>
}

const Subtitle: React.FunctionComponent<SubtitleProps> = (props) => {
  if (props.applicationStore.loggedIn) {
    if (props.applicationStore.game) {
      if (props.applicationStore.game.isGameOver) {
        return (
        <div>
          Game Over: {props.applicationStore.game.winnerName} wins!
          <div>
            <button
                id="new-game-button"
                onClick={ props.applicationStore!.endGameButtonPressedAsync }>
              Start A New Game
            </button>
          </div>
        </div>
        )
      } else if (props.applicationStore.game.isPlayerTurn) {
        return <div>Your turn</div>
      } 
    }
  }

  return <div></div>
}

const Notifications: React.FunctionComponent<Props> = inject("applicationStore")(observer((props) => {
  const applicationStore = props.applicationStore!

  return (<div className="notifications">
    <CombatNotifications game={applicationStore.game} />
    <Subtitle applicationStore={applicationStore} />
    <ErrorBox />
  </div>)
}))

export default Notifications