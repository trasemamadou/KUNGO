import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface DashboardStats {
  totalClients: number;
  totalProfessionals: number;
  totalBookings: number;
  pendingBookings: number;
  completedBookings: number;
  pendingVerifications: number;
  pendingReports: number;
  totalCategories: number;
  totalServices: number;
  totalGrossVolume: number;
  totalCommissionRevenue: number;
}

export interface CommissionRule {
  id: string;
  categoryId?: string;
  categoryName?: string;
  percentage: number;
  effectiveFrom: string;
  active: boolean;
}

export interface CommissionRuleRequest {
  categoryId?: string;
  percentage: number;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly api = inject(ApiService);

  getDashboardStats(): Observable<DashboardStats> {
    return this.api.get<DashboardStats>('/admin/dashboard/stats');
  }

  approveVerification(professionalId: string): Observable<void> {
    return this.api.post<void>(`/professionals/${professionalId}/verification/approve`);
  }

  rejectVerification(professionalId: string): Observable<void> {
    return this.api.post<void>(`/professionals/${professionalId}/verification/reject`);
  }

  listCommissionRules(): Observable<CommissionRule[]> {
    return this.api.get<CommissionRule[]>('/admin/commission-rules');
  }

  createCommissionRule(request: CommissionRuleRequest): Observable<CommissionRule> {
    return this.api.post<CommissionRule>('/admin/commission-rules', request);
  }

  deactivateCommissionRule(id: string): Observable<void> {
    return this.api.deleteVoid(`/admin/commission-rules/${id}`);
  }
}
