import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { Availability } from '../../../core/models/professional.model';
import { ProfessionalService } from '../../../core/services/professional.service';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

const DAYS = [
  { value: 'MONDAY', label: 'Lundi' },
  { value: 'TUESDAY', label: 'Mardi' },
  { value: 'WEDNESDAY', label: 'Mercredi' },
  { value: 'THURSDAY', label: 'Jeudi' },
  { value: 'FRIDAY', label: 'Vendredi' },
  { value: 'SATURDAY', label: 'Samedi' },
  { value: 'SUNDAY', label: 'Dimanche' },
];

@Component({
  selector: 'app-pro-availabilities',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule,
    EmptyStateComponent,
  ],
  template: `
    <div class="mx-auto max-w-3xl px-4 py-8 sm:px-6">
      <h1 class="mb-6 text-2xl font-bold text-gray-900 dark:text-gray-100">Mes disponibilités</h1>

      <form [formGroup]="form" class="mb-8 grid gap-3 rounded-xl border border-gray-200 p-4 dark:border-gray-800 sm:grid-cols-2" (ngSubmit)="add()">
        <mat-checkbox formControlName="recurring" class="sm:col-span-2">Créneau récurrent (hebdomadaire)</mat-checkbox>

        @if (form.controls.recurring.value) {
          <mat-form-field appearance="outline">
            <mat-label>Jour de la semaine</mat-label>
            <mat-select formControlName="dayOfWeek">
              @for (day of days; track day.value) {
                <mat-option [value]="day.value">{{ day.label }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
        } @else {
          <mat-form-field appearance="outline">
            <mat-label>Date précise</mat-label>
            <input matInput [matDatepicker]="picker" formControlName="specificDate" />
            <mat-datepicker-toggle matSuffix [for]="picker" />
            <mat-datepicker #picker />
          </mat-form-field>
        }

        <div class="flex gap-2">
          <mat-form-field appearance="outline">
            <mat-label>Heure de début</mat-label>
            <input matInput type="time" formControlName="startTime" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Heure de fin</mat-label>
            <input matInput type="time" formControlName="endTime" />
          </mat-form-field>
        </div>

        <button mat-flat-button color="primary" type="submit" class="self-start" [disabled]="form.invalid">
          Ajouter le créneau
        </button>
      </form>

      @if (!availabilities().length) {
        <app-empty-state icon="🗓️" title="Aucune disponibilité renseignée" />
      } @else {
        <div class="flex flex-col gap-2">
          @for (availability of availabilities(); track availability.id) {
            <div class="flex items-center justify-between rounded-lg border border-gray-200 p-3 dark:border-gray-800">
              <span class="text-sm text-gray-700 dark:text-gray-300">
                @if (availability.recurring) {
                  {{ dayLabel(availability.dayOfWeek) }}
                } @else {
                  {{ availability.specificDate }}
                }
                — {{ availability.startTime }} à {{ availability.endTime }}
              </span>
              <button mat-icon-button (click)="remove(availability)" aria-label="Supprimer">
                <mat-icon>delete</mat-icon>
              </button>
            </div>
          }
        </div>
      }
    </div>
  `,
})
export class ProAvailabilitiesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly professionalService = inject(ProfessionalService);

  readonly availabilities = signal<Availability[]>([]);
  readonly days = DAYS;
  private professionalId = '';

  readonly form = this.fb.nonNullable.group({
    recurring: [true],
    dayOfWeek: ['MONDAY'],
    specificDate: [null as Date | null],
    startTime: ['09:00', Validators.required],
    endTime: ['18:00', Validators.required],
  });

  ngOnInit(): void {
    this.professionalService.getCurrentProfile().subscribe((profile) => {
      this.professionalId = profile.id;
      this.load();
    });
  }

  private load(): void {
    this.professionalService.listAvailabilities(this.professionalId).subscribe((list) => this.availabilities.set(list));
  }

  dayLabel(value?: string): string {
    return DAYS.find((d) => d.value === value)?.label ?? value ?? '';
  }

  add(): void {
    if (this.form.invalid) return;
    const value = this.form.getRawValue();
    this.professionalService
      .addAvailability(this.professionalId, {
        recurring: value.recurring,
        dayOfWeek: value.recurring ? (value.dayOfWeek as Availability['dayOfWeek']) : undefined,
        specificDate: !value.recurring && value.specificDate ? value.specificDate.toISOString().split('T')[0] : undefined,
        startTime: value.startTime,
        endTime: value.endTime,
      })
      .subscribe(() => this.load());
  }

  remove(availability: Availability): void {
    this.professionalService.removeAvailability(this.professionalId, availability.id).subscribe(() => this.load());
  }
}
