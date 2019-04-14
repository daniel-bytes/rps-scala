import * as React from 'react'

interface GoogleAuthButtonProps {
  isReady: Boolean,
  isSignedIn: Boolean,
  onSignInClick: (evt: React.MouseEvent<HTMLElement>) => void,
  onSignOutClick: (evt: React.MouseEvent<HTMLElement>) => void
}

export const GoogleAuthButton: React.SFC<GoogleAuthButtonProps> = (props) => {
  const loginStyle = ( props.isReady && !props.isSignedIn ) ? 'block' : 'none'
  const logoutStyle = ( props.isReady && props.isSignedIn ) ? 'block' : 'none'

  return (
    <div>
      <button 
        id="authorize_button" 
        style={{ display: loginStyle }}
        onClick={ props.onSignInClick }>Authorize</button>

      <button 
        id="signout_button" 
        style={{ display: logoutStyle }}
        onClick={ props.onSignOutClick }>Sign Out</button>
    </div> 
  )
}
