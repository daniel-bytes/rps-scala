import * as React from 'react'
import { observer, inject } from 'mobx-react'
import { IApplicationStore } from '../services/ApplicationStore'

interface Props {
  applicationStore?: IApplicationStore
}

const Instructions: React.FunctionComponent<Props> = inject("applicationStore")(observer((props) => {
  return ( 
    <div className="instructions">
      <h3>
        <em>Capture the Flag</em> meets <em>Rock, Paper, Scissor</em>!
      </h3>

      <div id="authorize-container">
        <a className="icon-sign-in" 
            id="authorize_button"
            onClick={ props.applicationStore!.signInButtonPressedAsync }>
          Sign In With Google To Play
        </a>
      </div>
      
      <p>
        The goal of the game is to capture the other player's <em>Flag</em>. 
      </p>

      <p>
        There are 5 types of pieces on the board, only 3 of them can be moved:
      </p>
      <ol>
        <li>
          <em>Rock</em> beats <em>Scissor</em>
        </li>
        <li>
          <em>Paper</em> beats <em>Rock</em>
        </li>
        <li>
          <em>Scissor</em> beats <em>Paper</em>
        </li>
        <li>
          <em>Bomb</em> can't be moved.  If a player attacks a <em>Bomb</em>, both pieces lose
        </li>
        <li>
          <em>Flag</em> can't be moved.  If a player attacks the other player's <em>Flag</em>, they win the game
        </li>
      </ol>

      <p>
        Other rules:
      </p>
      <ol>
        <li>
          If a player run's out of movable pieces, the other player wins
        </li>
        <li>
          If a token attacks another piece of the same type (<em>Rock</em> attacks <em>Rock</em>, for example),
          both pieces lose
        </li>
      </ol>
      <p>
        <a href="https://github.com/daniel-bytes/rps-scala" target="_blank">Check out the source on Github</a>
      </p>
    </div>
  )
}))

export default Instructions