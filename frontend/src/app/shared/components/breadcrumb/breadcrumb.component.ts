import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

export interface BreadcrumbItem {
  label: string;
  link?: string;
}

@Component({
  selector: 'app-breadcrumb',
  standalone: true,
  imports: [RouterLink],
  template: `
    <nav aria-label="Fil d'Ariane" class="text-sm text-gray-500 dark:text-gray-400">
      <ol class="flex flex-wrap items-center gap-1">
        @for (item of items(); track item.label; let last = $last) {
          <li class="flex items-center gap-1">
            @if (item.link && !last) {
              <a [routerLink]="item.link" class="hover:text-brand-600 dark:hover:text-brand-400">{{ item.label }}</a>
            } @else {
              <span class="text-gray-800 dark:text-gray-200">{{ item.label }}</span>
            }
            @if (!last) {
              <span aria-hidden="true">/</span>
            }
          </li>
        }
      </ol>
    </nav>
  `,
})
export class BreadcrumbComponent {
  readonly items = input<BreadcrumbItem[]>([]);
}
