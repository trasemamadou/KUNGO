import { Component, input } from '@angular/core';

/**
 * Logo KÛNGO — masque stylisé aux motifs traditionnels ouest-africains
 * (scarifications, vannerie), repris du template de marque fourni.
 */
@Component({
  selector: 'app-kungo-logo',
  standalone: true,
  template: `
    <svg [attr.width]="size()" [attr.height]="size()" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
      <circle cx="50" cy="50" r="48" fill="#6b2e1a" stroke="#d4a017" stroke-width="2.5" />
      <path d="M12 30 Q25 20 40 28" stroke="#d4a017" stroke-width="3" fill="none" stroke-linecap="round" />
      <path d="M12 36 Q28 26 44 34" stroke="#b03a2e" stroke-width="3" fill="none" stroke-linecap="round" />
      <path d="M88 30 Q75 20 60 28" stroke="#d4a017" stroke-width="3" fill="none" stroke-linecap="round" />
      <path d="M88 36 Q72 26 56 34" stroke="#b03a2e" stroke-width="3" fill="none" stroke-linecap="round" />
      <path d="M20 82 L30 78 L40 82 L50 78 L60 82 L70 78 L80 82" stroke="#d4a017" stroke-width="2.5" fill="none" stroke-linejoin="round" />
      <path d="M20 88 L30 84 L40 88 L50 84 L60 88 L70 84 L80 88" stroke="#b03a2e" stroke-width="2.5" fill="none" stroke-linejoin="round" />
      <ellipse cx="38" cy="46" rx="8" ry="5" fill="#faf7f0" stroke="#d4a017" stroke-width="1.5" />
      <ellipse cx="62" cy="46" rx="8" ry="5" fill="#faf7f0" stroke="#d4a017" stroke-width="1.5" />
      <circle cx="38" cy="46" r="2.5" fill="#2c3e50" />
      <circle cx="62" cy="46" r="2.5" fill="#2c3e50" />
      <polygon points="50,52 44,62 56,62" fill="#d4a017" />
      <rect x="40" y="68" width="20" height="6" rx="2" fill="#b03a2e" />
      <line x1="44" y1="68" x2="44" y2="74" stroke="#faf7f0" stroke-width="1.2" />
      <line x1="50" y1="68" x2="50" y2="74" stroke="#faf7f0" stroke-width="1.2" />
      <line x1="56" y1="68" x2="56" y2="74" stroke="#faf7f0" stroke-width="1.2" />
      <circle cx="28" cy="58" r="1.8" fill="#d4a017" />
      <circle cx="24" cy="64" r="1.8" fill="#d4a017" />
      <circle cx="72" cy="58" r="1.8" fill="#d4a017" />
      <circle cx="76" cy="64" r="1.8" fill="#d4a017" />
      <line x1="44" y1="20" x2="44" y2="30" stroke="#d4a017" stroke-width="2" />
      <line x1="50" y1="18" x2="50" y2="28" stroke="#d4a017" stroke-width="2" />
      <line x1="56" y1="20" x2="56" y2="30" stroke="#d4a017" stroke-width="2" />
      <polygon points="18,18 22,14 26,18 22,22" fill="#d4a017" />
      <polygon points="74,18 78,14 82,18 78,22" fill="#d4a017" />
      <polygon points="18,82 22,78 26,82 22,86" fill="#b03a2e" />
      <polygon points="74,82 78,78 82,82 78,86" fill="#b03a2e" />
    </svg>
  `,
  styles: [
    `
      :host {
        display: inline-flex;
        filter: drop-shadow(0 2px 8px rgba(107, 46, 26, 0.3));
      }
    `,
  ],
})
export class KungoLogoComponent {
  readonly size = input(48);
}
