import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TenantService } from '../../../../services/tenant.service';
import { AuthService } from '../../../../services/auth.service';
import { CreateTenantPostRequest } from '../../../../models/tenant.model';
import { ResolvedLocation } from '../../../../models/location.model';

@Component({
  selector: 'app-tenant-create-post',
  templateUrl: './tenant-create-post.component.html',
  styleUrls: ['./tenant-create-post.component.css']
})
export class TenantCreatePostComponent implements OnInit {

  form: FormGroup;
  loading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  readonly foodOptions = ['VEG', 'NON_VEG', 'BOTH'];
  readonly smokingOptions = ['NO_SMOKING', 'SMOKING_ALLOWED'];
  readonly genderOptions = ['MALE', 'FEMALE', 'ANY'];

  constructor(
    private readonly fb: FormBuilder,
    private readonly tenantService: TenantService,
    private readonly router: Router,
    private readonly authService: AuthService
  ) {
    const today = new Date().toISOString().split('T')[0];

    this.form = this.fb.group({
      // User
      fullName:    ['', [Validators.required, Validators.maxLength(100)]],
      email:       ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
      phoneNumber: ['', [Validators.maxLength(20)]],

      // Post
      title:              ['', [Validators.required, Validators.maxLength(120)]],
      description:        ['', [Validators.maxLength(500)]],
      minBudget:          [null, [Validators.required, Validators.min(1)]],
      maxBudget:          [null, [Validators.required, Validators.min(1)]],
      totalCapacity:      [null, [Validators.required, Validators.min(1)]],
      currentOccupancy:   [null, [Validators.required, Validators.min(0)]],
      preferredLatitude:  [null, [Validators.required, Validators.min(-90), Validators.max(90)]],
      preferredLongitude: [null, [Validators.required, Validators.min(-180), Validators.max(180)]],
      locationLabel:      ['', [Validators.required, Validators.maxLength(120)]],
      moveInDate:         [today, [Validators.required]],

      // Preferences
      foodPreference:    [''],
      smokingPreference: [''],
      preferredGender:   [''],
      additionalNotes:   ['', [Validators.maxLength(300)]]
    });
  }

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      this.form.patchValue({
        fullName: user.fullName || '',
        email: user.email,
        phoneNumber: user.phoneNumber || ''
      });
    }
  }

  /**
   * Called by the location picker when a location is resolved (GPS or city).
   * Auto-fills lat, lng, and locationLabel from the reverse-geocoded address.
   */
  onLocationSelected(location: ResolvedLocation | null): void {
    if (location) {
      this.form.patchValue({
        preferredLatitude: location.latitude,
        preferredLongitude: location.longitude,
        locationLabel: location.address || location.label
      });
    } else {
      this.form.patchValue({
        preferredLatitude: null,
        preferredLongitude: null,
        locationLabel: ''
      });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.value;

    // Validate maxBudget >= minBudget
    if (v.maxBudget < v.minBudget) {
      this.errorMessage = 'Max budget must be greater than or equal to min budget.';
      return;
    }

    // Validate capacity
    if (v.currentOccupancy > v.totalCapacity) {
      this.errorMessage = 'Current occupancy cannot exceed total capacity.';
      return;
    }

    const request: CreateTenantPostRequest = {
      user: {
        fullName: v.fullName,
        email: v.email,
        phoneNumber: v.phoneNumber || undefined
      },
      title: v.title,
      description: v.description || undefined,
      minBudget: v.minBudget,
      maxBudget: v.maxBudget,
      totalCapacity: v.totalCapacity,
      currentOccupancy: v.currentOccupancy,
      preferredLatitude: v.preferredLatitude,
      preferredLongitude: v.preferredLongitude,
      locationLabel: v.locationLabel,
      moveInDate: v.moveInDate,
      preference: {
        foodPreference: v.foodPreference || undefined,
        smokingPreference: v.smokingPreference || undefined,
        preferredGender: v.preferredGender || undefined,
        additionalNotes: v.additionalNotes || undefined
      }
    };

    this.loading = true;
    this.errorMessage = null;
    this.successMessage = null;

    this.tenantService.createTenantPost(request).subscribe({
      next: () => {
        this.successMessage = 'Room posted successfully! Redirecting…';
        this.loading = false;
        setTimeout(() => this.router.navigate(['/tenant-matching']), 1500);
      },
      error: (err) => {
        this.errorMessage = err.error?.message ?? 'Failed to post room. Please try again.';
        this.loading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/tenant-matching']);
  }

  /** Helper for template — check if a field is invalid and touched. */
  isInvalid(field: string): boolean {
    const ctrl = this.form.get(field);
    return !!(ctrl && ctrl.invalid && ctrl.touched);
  }
}

