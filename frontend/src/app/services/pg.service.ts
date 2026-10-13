import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse, PgListing, PgSearchFilters } from '../models/pg.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PgService {
  private readonly baseUrl = `${environment.apiBaseUrl}/pgs`;

  constructor(private readonly http: HttpClient) {}

  /**
   * Searches PGs with filters + Spring-style pagination.
   * Returns a Page wrapper so callers know total/pages/last.
   */
  searchPgs(
    filters: PgSearchFilters,
    page: number = 0,
    size: number = 20
  ): Observable<PageResponse<PgListing>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    // Sort — Spring Pageable expects ?sort=field,direction
    if (filters.sortBy) {
      params = params.set('sort', filters.sortBy);
    }

    // Scalar filters
    const scalarKeys: (keyof PgSearchFilters)[] = [
      'latitude', 'longitude', 'radiusKm', 'city', 'area', 'sharingType', 'minPrice', 'maxPrice', 'foodType'
    ];
    for (const key of scalarKeys) {
      const val = filters[key];
      if (val !== null && val !== undefined && val !== '') {
        params = params.set(key, String(val));
      }
    }

    // Multi-value: facilityNames (repeat param)
    if (filters.facilityNames && filters.facilityNames.length > 0) {
      for (const name of filters.facilityNames) {
        params = params.append('facilityNames', name);
      }
    }

    return this.http.get<PageResponse<PgListing>>(`${this.baseUrl}/search`, { params });
  }

  getPgById(id: number | string): Observable<PgListing> {
    return this.http.get<PgListing>(`${this.baseUrl}/${id}`);
  }

  createPg(body: Record<string, unknown>): Observable<PgListing> {
    return this.http.post<PgListing>(this.baseUrl, body);
  }

  updatePg(id: number | string, body: Record<string, unknown>): Observable<PgListing> {
    return this.http.put<PgListing>(`${this.baseUrl}/${id}`, body);
  }

  deletePg(id: number | string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

