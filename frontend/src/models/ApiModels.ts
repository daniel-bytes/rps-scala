export interface ApiResponse<T> {
  status: number
  body: T
  headers: Map<string, string>
}
