import {User} from '../models/UserModels'

declare global {
  interface Window { gapi: any; }
}

export interface GoogleApiCallback {
  (service: GoogleAuthService): void
}

export interface GoogleApiInitializeRequest {
  apiScriptUrl: string,
  clientId: string,
  scope: string,
  document: Document,
  window: Window
}

export class GoogleAuthService {
  private _gapi: any = null
  private readonly _args: GoogleApiInitializeRequest

  constructor(args: GoogleApiInitializeRequest) {
    this._args = args
  }

  /**
   * Initializes the Google auth API.  Only should be called once
   * @param args The init request parameters
   */
  initialize(): Promise<GoogleAuthService> {
    return new Promise<GoogleAuthService>((resolve) => {
      if (this._gapi) {
        return resolve(this)
      }

      const script = this._args.document.createElement("script")
      script.src = this._args.apiScriptUrl

      script.onload = () => {
        this._args.window.gapi.load('auth2', () => {
          this._gapi = this._args.window.gapi.auth2
          return resolve(this)
        })
      }

      this._args.document.body.appendChild(script)
    })
  }

  requestToken(): Promise<User> {
    return new Promise((resolve, reject) => {
      this._gapi.init({
        clientId: this._args.clientId,
        scope: this._args.scope,
      })
      .grantOfflineAccess()
      .then((response: any) => { 
        const authInstance = window.gapi.auth2.getAuthInstance()
        const currentUser = authInstance.currentUser.get()
        
        if (currentUser && currentUser.getBasicProfile() && currentUser.getAuthResponse()) {
          resolve({
            id: currentUser.getId(),
            name: currentUser.getBasicProfile().getName(),
            email: currentUser.getBasicProfile().getEmail(),
            authCode: response.code,
            source: 'google'
          })
        } else {
          reject("No user available")
        }
      })
    })
  }
}
