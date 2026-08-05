import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';

export interface DriverNotification {
  message: string;
  severity: string;
  sourceEvent: string;
  createdAt: string;
}

/** Subscribes to this driver's personal channel: /topic/driver/{driverId}. */
@Injectable({ providedIn: 'root' })
export class AlertListenerService {
  private client: Client;
  private alerts$ = new Subject<DriverNotification>();
  private driverId = 'demo-driver-001';

  constructor() {
    this.client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        this.client.subscribe(`/topic/driver/${this.driverId}`, (msg: IMessage) => {
          this.alerts$.next(JSON.parse(msg.body) as DriverNotification);
        });
      },
    });
    this.client.activate();
  }

  onAlert() {
    return this.alerts$.asObservable();
  }
}
