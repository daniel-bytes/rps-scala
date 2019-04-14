export class ApiError extends Error {
  readonly status: number
  readonly headers: Map<string, string>

  constructor(status: number, msg: string, headers: Map<string, string>) {
    super(msg)
    this.status = status
    this.headers = headers
  }
}