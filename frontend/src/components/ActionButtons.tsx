import React from 'react'
import { observer, inject } from 'mobx-react'
import { NavigationState, AuthenticationType } from '../models/NavigationModels'
import { IApplicationStore } from '../services/ApplicationStore'

interface Props {
  applicationStore?: IApplicationStore
}

export const ActionButtons: React.SFC<Props> = inject("applicationStore")(observer((props) => {
  if (!props.applicationStore) throw new Error("Missing applicationStore")

  const authenticationType = props.applicationStore.authenticationType

  const loginGoogleStyle = authenticationType === AuthenticationType.Google &&  
    props.applicationStore.navState === NavigationState.LoginPage ? 'inline' : 'none'

  const loginAnonymousStyle = authenticationType === AuthenticationType.Anonymous &&  
    props.applicationStore.navState === NavigationState.LoginPage ? 'inline' : 'none'

  const logoutStyle = props.applicationStore.navState !== NavigationState.LoginPage ? 'inline' : 'none'

  const endGameStyle = props.applicationStore.navState === NavigationState.PlayGamePage ? 'inline' : 'none'

  return (
    <div id="buttons">
      <i className="icon-refresh button" 
          id="end-game-button"
          onClick={ props.applicationStore.endGameButtonPressedAsync }
          style={{ display: endGameStyle }}
          title="New Game">
      </i>
      
      <i className="icon-sign-in button" 
          id="authorize-button"
          onClick={ props.applicationStore.signInGoogleButtonPressedAsync }
          style={{ display: loginGoogleStyle }}
          title="Sign In With Google To Play">
      </i>

      <i className="icon-sign-in button" 
          id="start-button"
          onClick={ props.applicationStore.signInAnonymousButtonPressedAsync }
          style={{ display: loginAnonymousStyle }}
          title="Start Playing">
      </i>

      <i className="icon-sign-out button" 
          id="signout-button"
          onClick={ props.applicationStore.signOutButtonPressedAsync }
          style={{ display: logoutStyle }}
          title="Sign Out">
      </i>
    </div> 
  )
}))