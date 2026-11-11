import { Component, input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  template: `
    <div class="flex flex-col items-center justify-center gap-3 py-16 text-center">
      <span class="text-5xl">{{ icon() }}</span>
      <h3 class="text-lg font-semibold text-brand-700 dark:text-gray-100">{{ title() }}</h3>
      @if (description()) {
        <p class="max-w-sm text-sm text-gray-500 dark:text-gray-400">{{ description() }}</p>
      }
      <ng-content />
    </div>
  `,
})
export class EmptyStateComponent {
  readonly icon = input('🔍');
  readonly title = input('Aucun résultat');
  readonly description = input<string | undefined>(undefined);
}
