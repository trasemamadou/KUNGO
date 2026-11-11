import { Component, input } from '@angular/core';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  template: `
    <div
      class="flex items-center gap-4 rounded-2xl border border-brand-100 border-t-4 border-t-gold-500 bg-white p-5 shadow-sm dark:border-gray-800 dark:bg-gray-900"
    >
      <div class="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-brand-100 text-2xl dark:bg-brand-900">
        {{ icon() }}
      </div>
      <div>
        <p class="text-2xl font-bold text-brand-700 dark:text-gray-100">{{ value() }}</p>
        <p class="text-sm text-gray-500 dark:text-gray-400">{{ label() }}</p>
      </div>
    </div>
  `,
})
export class StatCardComponent {
  readonly icon = input('📊');
  readonly label = input.required<string>();
  readonly value = input.required<string | number>();
}
