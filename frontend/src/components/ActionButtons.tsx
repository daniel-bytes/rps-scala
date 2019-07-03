import React, { Component } from 'react'
import { observer, inject } from 'mobx-react'
import { NavigationState } from '../models/NavigationModels'
import { IApplicationStore } from '../services/ApplicationStore'


interface Props {
  applicationStore?: IApplicationStore
}

export const ActionButtons: React.SFC<Props> = inject("applicationStore")(observer((props) => {
  if (!props.applicationStore) throw new Error("Missing applicationStore")

  const loginStyle = props.applicationStore.navState === NavigationState.LoginPage ? 'block' : 'none'
  const logoutStyle = props.applicationStore.navState !== NavigationState.LoginPage ? 'block' : 'none'
  const newGameStyle = props.applicationStore.navState === NavigationState.ListGamesPage ? 'block' : 'none'
  const listGamesStyle = props.applicationStore.navState === NavigationState.PlayGamePage ? 'block' : 'none'
  const endGameStyle = props.applicationStore.navState === NavigationState.PlayGamePage ? 'block' : 'none'

  return (
    <div>
      <button 
        className="button"
        id="list_games_button" 
        style={{ display: listGamesStyle }}
        onClick={ props.applicationStore.homeButtonPressedAsync }>Home</button>

      <button 
        className="button"
        id="create_game_button" 
        style={{ display: newGameStyle }}
        onClick={ props.applicationStore.startGameButtonPressedAsync }>Start a Game</button>

      <button 
        className="button"
        id="end_game_button" 
        style={{ display: endGameStyle }}
        onClick={ props.applicationStore.endGameButtonPressedAsync }>End Game</button>

      <button 
        className="button"
        id="authorize_button" 
        style={{ display: loginStyle }}
        onClick={ props.applicationStore.signInButtonPressedAsync }>Sign In With Your Google Account</button>

      <button 
        className="button"
        id="signout_button" 
        style={{ display: logoutStyle }}
        onClick={ props.applicationStore.signOutButtonPressedAsync }>Sign Out</button>
    </div> 
  )
}))