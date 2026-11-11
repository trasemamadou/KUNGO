/** Miroir TypeScript de `com.kungo.common.response.ApiResponse` (backend). */
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}

/** Miroir TypeScript de `com.kungo.common.response.PageResponse` (backend). */
export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ErrorResponse {
  success: false;
  status: number;
  error: string;
  message: string;
  path?: string;
  timestamp: string;
  fieldErrors?: { field: string; message: string; rejectedValue: unknown }[];
}
