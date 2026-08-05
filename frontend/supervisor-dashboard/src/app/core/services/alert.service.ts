import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FleetNotification } from '../models/notification.model';

/** REST calls proxied through api-gateway to notification-alert-service. */
@Injectable({ providedIn: 'root' })
export class AlertService {
  private base = '/api/notifications';

  constructor(private http: HttpClient) {}

  supervisorFeed(): Observable<FleetNotification[]> {
    return this.http.get<FleetNotification[]>(`${this.base}/supervisor/feed`);
  }

  pendingApprovals(): Observable<FleetNotification[]> {
    return this.http.get<FleetNotification[]>(`${this.base}/approvals/pending`);
  }

  approve(id: string): Observable<FleetNotification> {
    return this.http.post<FleetNotification>(`${this.base}/approvals/${id}/approve`, {});
  }

  reject(id: string): Observable<FleetNotification> {
    return this.http.post<FleetNotification>(`${this.base}/approvals/${id}/reject`, {});
  }
}
