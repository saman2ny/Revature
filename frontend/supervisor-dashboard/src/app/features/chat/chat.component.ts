import { Component } from '@angular/core';
import { AgentChatService } from '../../core/services/agent-chat.service';
import { AgentChatMessage } from '../../core/models/notification.model';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.scss'
})
export class ChatComponent {
  messages: AgentChatMessage[] = [];
  draft = '';
  asking = false;

  constructor(private agentChat: AgentChatService) {}

  send(): void {
    const query = this.draft.trim();
    if (!query || this.asking) return;

    this.messages.push({ role: 'user', text: query });
    this.draft = '';
    this.asking = true;

    this.agentChat.ask(query).subscribe({
      next: res => {
        this.messages.push({ role: 'assistant', text: res.summary, citations: res.citations });
        this.asking = false;
      },
      error: () => {
        this.messages.push({ role: 'assistant', text: 'The fleet assistant is unavailable right now — please try again shortly.' });
        this.asking = false;
      }
    });
  }
}
