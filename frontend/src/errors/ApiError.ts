export class ApiError extends Error {
  readonly status: number
  readonly code: string
  readonly headers: Map<string, string>

  constructor(status: number, code: string, headers: Map<string, string>) {
    super(`An API error occurred: ${status} - ${code}`)
    Object.setPrototypeOf(this, ApiError.prototype)

    this.status = status
    this.code = code
    this.headers = headers
  }
}