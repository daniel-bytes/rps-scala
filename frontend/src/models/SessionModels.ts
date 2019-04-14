import { User } from "./UserModels";

export interface GoogleTokenRequest {
  id: string
  name: string
  authCode: string
}

export interface SessionResponse {
  sessionId: string
  userId: string
  name: string
}

export interface SessionState {
  user: User
  token: string
  gameId: string | null
}
