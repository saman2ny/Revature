import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, interval } from 'rxjs';

export interface TelemetrySample {
  driverId: string;
  vehicleId: string;
  latitude: number;
  longitude: number;
  speedKmh: number;
  headingDegrees: number;
  fuelLevelPercent: number;
  engineTempCelsius: number;
  harshBrakingDetected: boolean;
  harshAccelerationDetected: boolean;
}

/**
 * Real device GPS/accelerometer access is out of scope for this scaffold — this
 * service simulates a plausible telemetry stream and POSTs it to the API Gateway
 * every 3 seconds, exactly like a real driver app would. Swap the `generate()`
 * body for actual navigator.geolocation + motion sensor reads to go from
 * simulation to production.
 */
@Injectable({ providedIn: 'root' })
export class TelemetrySimulatorService {
  private endpoint = '/api/telemetry';
  private driverId = 'demo-driver-001';
  private vehicleId = 'VEH-1001';

  private lat = 12.9716;
  private lon = 77.5946;

  running$ = new BehaviorSubject<boolean>(false);
  lastSample$ = new BehaviorSubject<TelemetrySample | null>(null);

  constructor(private http: HttpClient) {}

  start(): void {
    if (this.running$.value) return;
    this.running$.next(true);

    interval(3000).subscribe(() => {
      if (!this.running$.value) return;
      const sample = this.generate();
      this.lastSample$.next(sample);
      this.http.post(this.endpoint, sample).subscribe({
        error: err => console.warn('Telemetry POST failed', err)
      });
    });
  }

  stop(): void {
    this.running$.next(false);
  }

  private generate(): TelemetrySample {
    this.lat += (Math.random() - 0.5) * 0.001;
    this.lon += (Math.random() - 0.5) * 0.001;

    const harshBraking = Math.random() < 0.08;
    const harshAcceleration = Math.random() < 0.05;

    return {
      driverId: this.driverId,
      vehicleId: this.vehicleId,
      latitude: this.lat,
      longitude: this.lon,
      speedKmh: Math.round(60 + Math.random() * 70),
      headingDegrees: Math.round(Math.random() * 359),
      fuelLevelPercent: Math.round(40 + Math.random() * 60),
      engineTempCelsius: Math.round(85 + Math.random() * 35),
      harshBrakingDetected: harshBraking,
      harshAccelerationDetected: harshAcceleration,
    };
  }
}
