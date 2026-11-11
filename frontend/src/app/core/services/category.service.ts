import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Category } from '../models/category.model';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private readonly api = inject(ApiService);

  getRootCategories(): Observable<Category[]> {
    return this.api.get<Category[]>('/categories');
  }

  getBySlug(slug: string): Observable<Category> {
    return this.api.get<Category>(`/categories/slug/${slug}`);
  }

  getById(id: string): Observable<Category> {
    return this.api.get<Category>(`/categories/${id}`);
  }
}
