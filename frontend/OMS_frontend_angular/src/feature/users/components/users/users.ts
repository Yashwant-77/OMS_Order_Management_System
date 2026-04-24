import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { UserService } from '../../../../app/services/user/user.service';
import { environment } from '../../../../environments/environment';

interface UserRow {
  userId: number;
  name: string;
  email: string;
  role: string;
  status?: string;
}

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class Users implements OnInit {
  constructor(
    private http: HttpClient,
    private snackBar: MatSnackBar,
    private userService: UserService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  isLoading = false;
  isCreating = false;
  showCreateForm = false;

  users: UserRow[] = [];
  filteredUsers: UserRow[] = [];
  searchText = '';

  roles = ['ADMINISTRATOR', 'SALES_REPRESENTATIVE', 'PRODUCT_MANAGER', 'FINANCE_MANAGER', 'BUSINESS_ANALYST', 'PURCHASING_OFFICER'];

  newUser = { name: '', email: '', role: '' };

  editingUserId: number | null = null;
  editData: { name: string; email: string; role: string } = { name: '', email: '', role: '' };

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers() {
    this.isLoading = true;
    this.http.get<UserRow[]>(`${environment.baseUrl}/api/users`).subscribe({
      next: (res) => {
        this.users = res || [];
        this.applyFilter();
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoading = false;
        this.handleError(err, 'Failed to load users');
        this.cdr.detectChanges();
      },
    });
  }

  applyFilter() {
    const term = this.searchText.toLowerCase();
    this.filteredUsers = this.users.filter(
      (u) =>
        u.name.toLowerCase().includes(term) ||
        u.email.toLowerCase().includes(term) ||
        u.role.toLowerCase().includes(term),
    );
  }

  createUser() {
    if (!this.newUser.name || !this.newUser.email || !this.newUser.role) {
      this.snackBar.open('Please fill in all fields', 'Close', { duration: 3000 });
      return;
    }

    this.isCreating = true;
    this.http.post<UserRow>(`${environment.baseUrl}/api/users`, this.newUser).subscribe({
      next: () => {
        this.snackBar.open('User created! An email with set password link has been sent.', 'Close', {
          duration: 5000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
        });
        this.newUser = { name: '', email: '', role: '' };
        this.showCreateForm = false;
        this.isCreating = false;
        this.loadUsers();
      },
      error: (err) => {
        this.isCreating = false;
        this.handleError(err, 'Failed to create user');
      },
    });
  }

  startEdit(user: UserRow) {
    this.editingUserId = user.userId;
    this.editData = { name: user.name, email: user.email, role: user.role };
  }

  cancelEdit() {
    this.editingUserId = null;
  }

  saveEdit(user: UserRow) {
    this.http
      .put<UserRow>(`${environment.baseUrl}/api/users/${user.userId}`, this.editData)
      .subscribe({
        next: (updated) => {
          const idx = this.users.findIndex((u) => u.userId === user.userId);
          if (idx !== -1) {
            this.users[idx] = updated;
          }
          this.applyFilter();
          this.editingUserId = null;
          this.snackBar.open('User updated successfully!', 'Close', { duration: 3000 });
          this.cdr.detectChanges();
        },
        error: (err) => this.handleError(err, 'Failed to update user'),
      });
  }

  deleteUser(userId: number) {
    this.http.delete(`${environment.baseUrl}/api/users/${userId}`, { responseType: 'text' }).subscribe({
      next: () => {
        this.users = this.users.filter((u) => u.userId !== userId);
        this.applyFilter();
        this.snackBar.open('User deleted.', 'Close', { duration: 3000 });
        this.cdr.detectChanges();
      },
      error: (err) => this.handleError(err, 'Failed to delete user'),
    });
  }

  private handleError(err: any, fallback: string) {
    if (err.status === 401) {
      this.userService.clear();
      this.router.navigate(['/login']);
      return;
    }
    this.snackBar.open(err?.error?.message || fallback, 'Close', { duration: 3000 });
  }
}
