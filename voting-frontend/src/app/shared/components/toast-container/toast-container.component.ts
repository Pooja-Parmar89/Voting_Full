import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-stack position-fixed top-0 end-0 p-3" style="z-index: 1080;">
      @for (t of toastService.toasts(); track t.id) {
        <div class="toast show mb-2 border-0 shadow"
             [class.text-bg-success]="t.type === 'success'"
             [class.text-bg-danger]="t.type === 'error'"
             [class.text-bg-secondary]="t.type === 'info'">
          <div class="d-flex">
            <div class="toast-body">{{ t.message }}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" (click)="toastService.dismiss(t.id)"></button>
          </div>
        </div>
      }
    </div>
  `
})
export class ToastContainerComponent {
  toastService = inject(ToastService);
}
