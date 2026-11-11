import { Component, input } from '@angular/core';

@Component({
  selector: 'app-skeleton',
  standalone: true,
  template: `
    <div
      class="animate-pulse rounded-md bg-gray-200 dark:bg-gray-700"
      [style.width]="width()"
      [style.height]="height()"
    ></div>
  `,
})
export class SkeletonComponent {
  readonly width = input('100%');
  readonly height = input('1rem');
}
