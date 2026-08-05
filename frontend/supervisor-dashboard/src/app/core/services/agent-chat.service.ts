import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

interface AgentResponse {
  agentName: string;
  summary: string;
  citations?: string[];
}

/** Talks to the Conversation Agent via api-gateway -> ai-agent-service. */
@Injectable({ providedIn: 'root' })
export class AgentChatService {
  private base = '/api/agents/conversation/chat';

  constructor(private http: HttpClient) {}

  ask(query: string): Observable<AgentResponse> {
    return this.http.post<AgentResponse>(this.base, { query });
  }
}
