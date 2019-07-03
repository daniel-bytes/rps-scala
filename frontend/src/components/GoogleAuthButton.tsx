import * as React from 'react'

interface GoogleAuthButtonProps {
  isReady: boolean,
  isSignedIn: boolean,
  onSignInClick: (evt: React.MouseEvent<HTMLElement>) => void,
  onSignOutClick: (evt: React.MouseEvent<HTMLElement>) => void
}

export const GoogleAuthButton: React.SFC<GoogleAuthButtonProps> = (props) => {
  const loginStyle = ( props.isReady && !props.isSignedIn ) ? 'block' : 'none'
  const logoutStyle = ( props.isReady && props.isSignedIn ) ? 'block' : 'none'

  return (
    <div>
      <button 
        className="button"
        id="authorize_button" 
        style={{ display: loginStyle }}
        onClick={ props.onSignInClick }>Sign In With Your Google Account</button>

      <button 
        className="button"
        id="signout_button" 
        style={{ display: logoutStyle }}
        onClick={ props.onSignOutClick }>Sign Out</button>
    </div> 
  )
}
