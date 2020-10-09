export type ApiErrorCode = 
  'not-a-movable-token' |
  'version-conflict' |
  'game-not-found' |
  'unknown'

export class ApiError extends Error {
  readonly status: number
  readonly code: string
  readonly headers: Map<string, string>
  readonly message: string

  constructor(status: number, code: ApiErrorCode, message: string, headers: Map<string, string>) {
    super(`An API error occurred: ${status} - ${code}: ${message}`)

    this.status = status
    this.code = code
    this.headers = headers
    this.message = message
  }
}