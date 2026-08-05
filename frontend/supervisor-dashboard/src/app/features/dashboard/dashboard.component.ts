import { Component, OnInit } from '@angular/core';
import { AlertService } from '../../core/services/alert.service';
import { WebSocketService } from '../../core/services/websocket.service';
import { FleetNotification } from '../../core/models/notification.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  feed: FleetNotification[] = [];

  constructor(
    private alertService: AlertService,
    private wsService: WebSocketService
  ) {}

  ngOnInit(): void {
    // Initial load from REST, then switch to live WebSocket push for new items.
    this.alertService.supervisorFeed().subscribe(items => (this.feed = items));

    this.wsService.onNotification().subscribe(notification => {
      this.feed = [notification, ...this.feed].slice(0, 100);
    });
  }

  severityClass(severity: string): string {
    return `sev-${severity.toLowerCase()}`;
  }
}
