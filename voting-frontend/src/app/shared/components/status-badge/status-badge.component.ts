import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  template: `<span class="status-badge" [ngClass]="'status-' + status">{{ label() }}</span>`
})
export class StatusBadgeComponent {
  @Input() status = '';
  label(): string {
    return this.status.replace('_', ' ');
  }
}
