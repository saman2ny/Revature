export interface FleetNotification {
  id: string;
  driverId: string;
  vehicleId: string;
  sourceEvent: 'DRIVER_ALERT' | 'ROUTE_UPDATE';
  audience: 'DRIVER' | 'SUPERVISOR' | 'BOTH' | 'SUPPRESSED';
  message: string;
  severity: 'INFO' | 'WARNING' | 'CRITICAL';
  requiresApproval: boolean;
  approvalStatus: 'NONE' | 'PENDING' | 'AUTO_APPROVED' | 'APPROVED' | 'REJECTED';
  createdAt: string;
}

export interface AgentChatMessage {
  role: 'user' | 'assistant';
  text: string;
  citations?: string[];
}
