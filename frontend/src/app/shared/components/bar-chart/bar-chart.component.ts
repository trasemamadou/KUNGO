import { Component, computed, input } from '@angular/core';

export interface BarChartDatum {
  label: string;
  value: number;
}

/** Mini bar chart SVG sans dépendance externe, pour les KPI des tableaux de bord. */
@Component({
  selector: 'app-bar-chart',
  standalone: true,
  template: `
    <div class="flex items-end gap-3" [style.height.px]="height()">
      @for (bar of bars(); track bar.label) {
        <div class="flex flex-1 flex-col items-center gap-1">
          <span class="text-xs font-medium text-gray-600 dark:text-gray-300">{{ bar.value }}</span>
          <div
            class="w-full rounded-t-md bg-brand-500 transition-all dark:bg-brand-400"
            [style.height.%]="bar.percent"
          ></div>
          <span class="mt-1 text-xs text-gray-400">{{ bar.label }}</span>
        </div>
      }
    </div>
  `,
})
export class BarChartComponent {
  readonly data = input.required<BarChartDatum[]>();
  readonly height = input(160);

  readonly bars = computed(() => {
    const values = this.data().map((d) => d.value);
    const max = Math.max(...values, 1);
    return this.data().map((d) => ({ ...d, percent: Math.max((d.value / max) * 100, 2) }));
  });
}
