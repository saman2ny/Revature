import { Component, OnInit } from '@angular/core';
import { AlertService } from '../../core/services/alert.service';
import { FleetNotification } from '../../core/models/notification.model';

@Component({
  selector: 'app-alerts',
  templateUrl: './alerts.component.html',
  styleUrl: './alerts.component.scss'
})
export class AlertsComponent implements OnInit {
  pending: FleetNotification[] = [];
  loading = false;

  constructor(private alertService: AlertService) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading = true;
    this.alertService.pendingApprovals().subscribe({
      next: items => { this.pending = items; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  approve(item: FleetNotification): void {
    this.alertService.approve(item.id).subscribe(() => this.refresh());
  }

  reject(item: FleetNotification): void {
    this.alertService.reject(item.id).subscribe(() => this.refresh());
  }
}
