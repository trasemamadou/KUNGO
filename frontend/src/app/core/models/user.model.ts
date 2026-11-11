export type Role = 'SUPER_ADMIN' | 'ADMIN' | 'MODERATEUR' | 'CLIENT' | 'PROFESSIONNEL';
export type UserStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'BANNED';

export interface User {
  id: string;
  email: string;
  phone?: string;
  firstName: string;
  lastName: string;
  fullName: string;
  role: Role;
  status: UserStatus;
  emailVerified: boolean;
  twoFactorEnabled: boolean;
  avatarUrl?: string;
  locale: string;
  lastLoginAt?: string;
  createdAt: string;
}

export interface UserSummary {
  id: string;
  fullName: string;
  avatarUrl?: string;
  role: Role;
}

export interface UserUpdateRequest {
  firstName?: string;
  lastName?: string;
  phone?: string;
  locale?: string;
}
