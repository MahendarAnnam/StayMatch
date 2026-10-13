import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateTenantPostRequest, TenantPost, TenantSearchFilters } from '../models/tenant.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class TenantService {
  private readonly baseUrl = `${environment.apiBaseUrl}/tenant-posts`;

  constructor(private readonly http: HttpClient) {}

  /** POST /api/v1/tenant-posts — create a new tenant requirement post. */
  createTenantPost(request: CreateTenantPostRequest): Observable<TenantPost> {
    return this.http.post<TenantPost>(this.baseUrl, request);
  }

  /** GET /api/v1/tenant-posts/search — search with optional filters + sort. */
  searchTenantPosts(filters: TenantSearchFilters): Observable<TenantPost[]> {
    let params = new HttpParams();
    const entries = Object.entries(filters) as [string, string | number | undefined][];
    for (const [key, value] of entries) {
      if (value !== null && value !== undefined && value !== '') {
        params = params.set(key, String(value));
      }
    }
    return this.http.get<TenantPost[]>(`${this.baseUrl}/search`, { params });
  }

  /** GET /api/v1/tenant-posts/{id}/matches — find matching tenants. */
  matchTenantPosts(tenantPostId: number, radiusKm?: number): Observable<TenantPost[]> {
    let params = new HttpParams();
    if (radiusKm !== undefined && radiusKm !== null) {
      params = params.set('radiusKm', radiusKm);
    }
    return this.http.get<TenantPost[]>(`${this.baseUrl}/${tenantPostId}/matches`, { params });
  }

  /** GET /api/v1/tenant-posts/{id} — get single post by ID. */
  getTenantPostById(id: number): Observable<TenantPost> {
    return this.http.get<TenantPost>(`${this.baseUrl}/${id}`);
  }

  /** PUT /api/v1/tenant-posts/{id} — update an existing post. */
  updateTenantPost(id: number, request: CreateTenantPostRequest): Observable<TenantPost> {
    return this.http.put<TenantPost>(`${this.baseUrl}/${id}`, request);
  }

  /** DELETE /api/v1/tenant-posts/{id} — delete a post. */
  deleteTenantPost(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

