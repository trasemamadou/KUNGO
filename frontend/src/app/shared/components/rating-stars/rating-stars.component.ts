import { Component, computed, input, output } from '@angular/core';

@Component({
  selector: 'app-rating-stars',
  standalone: true,
  template: `
    <div class="inline-flex items-center gap-0.5" [attr.aria-label]="'Note ' + rating() + ' sur 5'">
      @for (star of stars(); track $index) {
        <button
          type="button"
          class="leading-none transition-transform"
          [class.cursor-default]="!interactive()"
          [class.hover:scale-110]="interactive()"
          [style.fontSize.px]="size()"
          [disabled]="!interactive()"
          (click)="onStarClick($index + 1)"
        >
          <span [class]="star ? 'text-gold-500' : 'text-gray-300 dark:text-gray-600'">&#9733;</span>
        </button>
      }
      @if (showValue()) {
        <span class="ml-1 text-sm font-medium text-gray-600 dark:text-gray-300">{{ rating().toFixed(1) }}</span>
      }
    </div>
  `,
})
export class RatingStarsComponent {
  readonly rating = input(0);
  readonly interactive = input(false);
  readonly size = input(18);
  readonly showValue = input(false);

  readonly ratingChange = output<number>();

  readonly stars = computed(() => {
    const rounded = Math.round(this.rating());
    return Array.from({ length: 5 }, (_, i) => i < rounded);
  });

  onStarClick(value: number): void {
    if (this.interactive()) {
      this.ratingChange.emit(value);
    }
  }
}
