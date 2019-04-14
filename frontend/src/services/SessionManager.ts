import * as session from '../models/SessionModels'
import * as user from '../models/UserModels'
import ApiClient from './ApiClient'
import { GoogleAuthService } from './GoogleAuthService'



/**
 * Service for managing a user's session token
 */
export default class SessionManager {
  private readonly _url = `/session`
  private readonly _sessionStateKey = `session-state`
  private readonly _google: GoogleAuthService
  private readonly _api: ApiClient
  private _apiWithToken: ApiClient

  constructor(google: GoogleAuthService) {
    this._google = google
    this._api = new ApiClient()
    this._apiWithToken = new ApiClient(this.getToken())
  }

  async initialize(): Promise<SessionManager> {
    await this._google.initialize()
    return this
  }

  getApiClient(): ApiClient {
    return this._apiWithToken
  }

  clearSessionState() {
    localStorage.removeItem(this._sessionStateKey)
  }

  getSessionState(): session.SessionState | null {
    const state = localStorage.getItem(this._sessionStateKey)

    if (state) {
      return JSON.parse(state) as session.SessionState
    } else {
      return null
    }
  }

  getGameId(): string | null {
    const session = this.getSessionState()

    if (session) {
      return session.gameId
    } else {
      return null
    }
  }

  setGameId(id: string | null) {
    const session = this.getSessionState()

    if (session) {
      session.gameId = id
      this.setSessionState(session)
    }
  }

  async requestGoogleTokenAsync(): Promise<session.SessionState> {
    const user = await this._google.requestToken()
    const result = await this.loadTokenAsync(user)
    
    return result
  }

  /**
   * Returns a session token from local storage, or null if not found
   */
  private getToken(): string | null {
    const session = this.getSessionState()

    if (session) {
      return session.token
    } else {
      return null
    }
  }

  private setSessionState(state: session.SessionState) {
    localStorage.setItem(this._sessionStateKey, JSON.stringify(state))
    return state
  }

  /**
   * Creates a new token by POST-ing to the API server
   */
  async loadTokenAsync(user: user.User): Promise<session.SessionState> {
    console.log(`Requesting new session token from server for user [${user.name}]`)

    const request = {
      id: user.id,
      name: user.name,
      authCode: user.authCode
    }

    const response = await this._api.postAsync<object>(this._url + '/google', request)
    
    if (response.status === 200) {
      const token = response.headers.get(`set-authorization`)

      if (token) {
        console.log(`Session token loaded for user [${user.name}]`)
        return this.setSessionState({ user, token, gameId: null })
      } else {
        throw new Error(`Auth token was null`)
      }
    } else {
      console.error(response)
      throw new Error(`Token request failed with ${response.status}`)
    }
  }

  /**
   * Validates the current token stored in local storage.
   * If no token is stored, or the token is invalid, null is returned.
   * If token is valid, the token is returned.
   */
  async validateSessionAsync(): Promise<session.SessionState | null> {
    console.log(`Checking for valid session token in local storage`)
    const sessionState = this.getSessionState()

    if (sessionState) {
      const response = await this._apiWithToken.getAsync<session.SessionResponse>(this._url)

      if (response.body.sessionId) {
        return sessionState
      } else {
        return null
      }
    } else {
      return null
    }
  }
}