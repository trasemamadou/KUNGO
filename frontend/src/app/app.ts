import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth/auth.service';
import { TokenStorageService } from './core/auth/token-storage.service';
import { FooterComponent } from './core/layout/footer/footer.component';
import { HeaderComponent } from './core/layout/header/header.component';
import { NotificationService } from './core/services/notification.service';
import { ToastContainerComponent } from './shared/components/toast/toast.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, FooterComponent, ToastContainerComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly notificationService = inject(NotificationService);

  ngOnInit(): void {
    if (this.tokenStorage.isAuthenticated()) {
      this.authService.loadCurrentUser().subscribe();
      this.notificationService.refreshUnreadCount().subscribe();
    }
  }
}
