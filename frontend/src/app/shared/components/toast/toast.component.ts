import { Component, inject } from '@angular/core';
import { ToastService } from './toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  template: `
    <div class="fixed bottom-4 right-4 z-50 flex flex-col gap-2 w-80 max-w-[90vw]">
      @for (toast of toastService.toasts(); track toast.id) {
        <div
          class="flex items-start gap-3 rounded-full border-l-4 border-gold-500 px-5 py-3 shadow-lg text-sm font-semibold text-white animate-in"
          [class.bg-emerald-600]="toast.type === 'success'"
          [class.bg-accent-500]="toast.type === 'error'"
          [class.bg-brand-600]="toast.type === 'info'"
          [class.bg-amber-500]="toast.type === 'warning'"
        >
          <span class="flex-1">{{ toast.message }}</span>
          <button
            type="button"
            class="opacity-80 hover:opacity-100"
            (click)="toastService.dismiss(toast.id)"
            aria-label="Fermer"
          >
            &times;
          </button>
        </div>
      }
    </div>
  `,
})
export class ToastContainerComponent {
  readonly toastService = inject(ToastService);
}
