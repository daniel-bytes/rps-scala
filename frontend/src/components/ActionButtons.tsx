import React, { Component } from 'react'
import { observer, inject } from 'mobx-react'
import { NavigationState } from '../models/NavigationModels'
import { IApplicationStore } from '../services/ApplicationStore'


interface Props {
  applicationStore?: IApplicationStore
}

export const ActionButtons: React.SFC<Props> = inject("applicationStore")(observer((props) => {
  if (!props.applicationStore) throw new Error("Missing applicationStore")

  const loginStyle = props.applicationStore.navState === NavigationState.LoginPage ? 'inline' : 'none'
  const logoutStyle = props.applicationStore.navState !== NavigationState.LoginPage ? 'inline' : 'none'
  const endGameStyle = props.applicationStore.navState === NavigationState.PlayGamePage ? 'inline' : 'none'

  return (
    <div id="buttons">
      <i className="icon-refresh button" 
          id="end_game_button"
          onClick={ props.applicationStore.endGameButtonPressedAsync }
          style={{ display: endGameStyle }}
          title="New Game">
      </i>
      
      <i className="icon-sign-in button" 
          id="authorize_button"
          onClick={ props.applicationStore.signInButtonPressedAsync }
          style={{ display: loginStyle }}
          title="Sign In With Google To Play">
      </i>

      <i className="icon-sign-out button" 
          id="signout_button"
          onClick={ props.applicationStore.signOutButtonPressedAsync }
          style={{ display: logoutStyle }}
          title="Sign Out">
      </i>
    </div> 
  )
}))