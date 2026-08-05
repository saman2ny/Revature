import { Injectable, OnDestroy } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';
import { FleetNotification } from '../models/notification.model';

/**
 * Wraps a STOMP-over-SockJS connection to notification-alert-service (proxied
 * through the api-gateway at /ws). Emits every notification pushed to the
 * "/topic/supervisor-feed" destination — see WebSocketConfig on the backend.
 */
@Injectable({ providedIn: 'root' })
export class WebSocketService implements OnDestroy {
  private client: Client;
  private feed$ = new Subject<FleetNotification>();

  constructor() {
    this.client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        this.client.subscribe('/topic/supervisor-feed', (message: IMessage) => {
          this.feed$.next(JSON.parse(message.body) as FleetNotification);
        });
      },
    });
    this.client.activate();
  }

  onNotification() {
    return this.feed$.asObservable();
  }

  ngOnDestroy(): void {
    this.client.deactivate();
  }
}
