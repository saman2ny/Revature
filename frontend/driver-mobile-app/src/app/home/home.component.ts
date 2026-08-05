import { Component, OnInit } from '@angular/core';
import { TelemetrySimulatorService, TelemetrySample } from '../services/telemetry-simulator.service';
import { AlertListenerService, DriverNotification } from '../services/alert-listener.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  running = false;
  lastSample: TelemetrySample | null = null;
  alerts: DriverNotification[] = [];

  constructor(
    private telemetry: TelemetrySimulatorService,
    private alertListener: AlertListenerService
  ) {}

  ngOnInit(): void {
    this.telemetry.running$.subscribe(r => (this.running = r));
    this.telemetry.lastSample$.subscribe(s => (this.lastSample = s));
    this.alertListener.onAlert().subscribe(a => {
      this.alerts = [a, ...this.alerts].slice(0, 20);
    });
  }

  toggle(): void {
    this.running ? this.telemetry.stop() : this.telemetry.start();
  }

  severityClass(severity: string): string {
    return `sev-${severity.toLowerCase()}`;
  }
}
