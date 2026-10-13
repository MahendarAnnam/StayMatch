import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PgService } from '../../../../services/pg.service';
import { AuthService } from '../../../../services/auth.service';
import { ResolvedLocation } from '../../../../models/location.model';

const DAYS_OF_WEEK = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

const SHORT_DAYS: Record<string, string> = {
  MONDAY: 'Mon', TUESDAY: 'Tue', WEDNESDAY: 'Wed',
  THURSDAY: 'Thu', FRIDAY: 'Fri', SATURDAY: 'Sat', SUNDAY: 'Sun'
};

const FACILITY_OPTIONS = [
  'WiFi', 'AC', 'Laundry', 'Parking', 'Gym',
  'Power Backup', 'Meals', 'TV', 'Geyser', 'Fridge',
  'Water Purifier', 'CCTV', 'Housekeeping', 'Lift'
];

@Component({
  selector: 'app-pg-form-page',
  templateUrl: './pg-form-page.component.html',
  styleUrls: ['./pg-form-page.component.css']
})
export class PgFormPageComponent implements OnInit {

  form!: FormGroup;
  editMode = false;
  pgId: string | null = null;
  loading = false;
  submitting = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  /** Step-based navigation: 0 = Basic, 1 = Details, 2 = Media */
  currentStep = 0;
  readonly stepLabels = ['Basic Info', 'Details', 'Media & Submit'];

  readonly daysOfWeek = DAYS_OF_WEEK;
  readonly shortDays = SHORT_DAYS;
  readonly facilityOptions = FACILITY_OPTIONS;
  readonly foodTypes = [
    { label: 'Pure Veg',       value: 'VEG' },
    { label: 'Non-Veg',        value: 'NON_VEG' },
    { label: 'Veg & Non-Veg',  value: 'BOTH' }
  ];

  readonly priceTypes = [
    { label: 'Exact Price',  value: 'EXACT' },
    { label: 'Price Range',  value: 'RANGE' },
    { label: 'Hidden',       value: 'HIDDEN' }
  ];

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly pgService: PgService,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.pgId = this.route.snapshot.paramMap.get('id');
    this.editMode = !!this.pgId;
    this.initForm();

    if (this.editMode && this.pgId) {
      this.loadPg(this.pgId);
    } else {
      this.prefillOwnerFromAuth();
    }
  }

  // ── Form setup ──────────────────────────────────────────────────────

  private initForm(): void {
    this.form = this.fb.group({
      // Owner
      ownerFullName:  ['', [Validators.required, Validators.maxLength(100)]],
      ownerEmail:     ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
      ownerPhone:     ['', [Validators.maxLength(20)]],
      // PG basic
      name:           ['', [Validators.required, Validators.maxLength(150)]],
      description:    ['', [Validators.maxLength(600)]],
      city:           ['', [Validators.maxLength(100)]],
      latitude:       [null, [Validators.required, Validators.min(-90), Validators.max(90)]],
      longitude:      [null, [Validators.required, Validators.min(-180), Validators.max(180)]],
      locationLabel:  ['', [Validators.required, Validators.maxLength(150)]],
      // Sharing options
      sharingOptions: this.fb.array([], Validators.required),
      // Facilities
      selectedFacilities: [[] as string[]],
      // Food
      foodType:       [null],
      nonVegDays:     [[] as string[]],
      eggDays:        [[] as string[]],
      // Pricing
      priceType:      ['EXACT'],
      minPrice:       [null],
      maxPrice:       [null],
      // Images (array of URL strings)
      imageUrls:      this.fb.array([]),
      // Contact details
      contactPhone:   ['', [Validators.maxLength(20)]],
      contactEmail:   ['', [Validators.email, Validators.maxLength(120)]],
      // Weekly menu
      weeklyMenu:     this.fb.group(
        DAYS_OF_WEEK.reduce((acc, day) => {
          acc[day] = [''];
          return acc;
        }, {} as Record<string, any>)
      )
    });
  }

  private prefillOwnerFromAuth(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      this.form.patchValue({
        ownerFullName: user.fullName || '',
        ownerEmail: user.email,
        ownerPhone: user.phoneNumber || '',
        contactEmail: user.email,
        contactPhone: user.phoneNumber || ''
      });
    }
  }

  get sharingOptions(): FormArray {
    return this.form.get('sharingOptions') as FormArray;
  }

  get weeklyMenuGroup(): FormGroup {
    return this.form.get('weeklyMenu') as FormGroup;
  }

  get imageUrls(): FormArray {
    return this.form.get('imageUrls') as FormArray;
  }

  get showNonVegDays(): boolean {
    const ft = this.form.get('foodType')?.value;
    return ft === 'NON_VEG' || ft === 'BOTH';
  }

  get showRangeFields(): boolean {
    return this.form.get('priceType')?.value === 'RANGE';
  }

  addSharingOption(): void {
    this.sharingOptions.push(this.fb.group({
      sharingType: ['SINGLE', Validators.required],
      rent: [null, [Validators.required, Validators.min(1)]],
      availabilityCount: [1, [Validators.required, Validators.min(0)]]
    }));
  }

  removeSharingOption(i: number): void {
    this.sharingOptions.removeAt(i);
  }

  addImageUrl(): void {
    this.imageUrls.push(this.fb.control('', [Validators.required]));
  }

  removeImageUrl(i: number): void {
    this.imageUrls.removeAt(i);
  }

  // ── Location picker integration ────────────────────────────────────

  onLocationPicked(location: ResolvedLocation | null): void {
    if (location) {
      this.form.patchValue({
        latitude: location.latitude,
        longitude: location.longitude,
        locationLabel: location.address || location.label || '',
        city: location.area || ''
      });
    } else {
      this.form.patchValue({
        latitude: null,
        longitude: null,
        locationLabel: '',
        city: ''
      });
    }
  }

  // ── Day toggle helpers ──────────────────────────────────────────────

  toggleDay(controlName: string, day: string): void {
    const current: string[] = this.form.get(controlName)?.value ?? [];
    const idx = current.indexOf(day);
    if (idx >= 0) {
      current.splice(idx, 1);
    } else {
      current.push(day);
    }
    this.form.get(controlName)?.setValue([...current]);
  }

  isDaySelected(controlName: string, day: string): boolean {
    const current: string[] = this.form.get(controlName)?.value ?? [];
    return current.includes(day);
  }

  toggleFacility(name: string): void {
    const current: string[] = this.form.get('selectedFacilities')?.value ?? [];
    const idx = current.indexOf(name);
    if (idx >= 0) {
      current.splice(idx, 1);
    } else {
      current.push(name);
    }
    this.form.get('selectedFacilities')?.setValue([...current]);
  }

  isFacilitySelected(name: string): boolean {
    const current: string[] = this.form.get('selectedFacilities')?.value ?? [];
    return current.includes(name);
  }

  // ── Step navigation ─────────────────────────────────────────────────

  nextStep(): void {
    if (this.currentStep === 0 && !this.isStep0Valid()) {
      this.markStep0Touched();
      return;
    }
    if (this.currentStep < this.stepLabels.length - 1) {
      this.currentStep++;
    }
  }

  prevStep(): void {
    if (this.currentStep > 0) {
      this.currentStep--;
    }
  }

  goToStep(step: number): void {
    if (step < this.currentStep) {
      this.currentStep = step;
    } else if (step === this.currentStep + 1) {
      this.nextStep();
    }
  }

  private isStep0Valid(): boolean {
    const fields = ['name', 'latitude', 'longitude', 'locationLabel'];
    if (!this.editMode) {
      fields.push('ownerFullName', 'ownerEmail');
    }
    return fields.every(f => this.form.get(f)?.valid);
  }

  private markStep0Touched(): void {
    const fields = ['name', 'latitude', 'longitude', 'locationLabel', 'ownerFullName', 'ownerEmail', 'ownerPhone'];
    fields.forEach(f => this.form.get(f)?.markAsTouched());
  }

  // ── Helper for inline validation ────────────────────────────────────

  hasError(controlName: string, error: string): boolean {
    const ctrl = this.form.get(controlName);
    return !!ctrl && ctrl.hasError(error) && ctrl.touched;
  }

  // ── Load existing PG (edit mode) ────────────────────────────────────

  private loadPg(id: string): void {
    this.loading = true;
    this.pgService.getPgById(id).subscribe({
      next: (pg) => {
        this.form.patchValue({
          ownerFullName: pg.owner?.fullName,
          ownerEmail: pg.owner?.email,
          ownerPhone: pg.owner?.phoneNumber,
          name: pg.name,
          description: pg.description,
          latitude: pg.latitude,
          longitude: pg.longitude,
          locationLabel: pg.locationLabel,
          foodType: pg.foodType ?? null,
          nonVegDays: pg.nonVegDays ?? [],
          eggDays: pg.eggDays ?? [],
          selectedFacilities: pg.facilities ?? [],
          priceType: pg.priceType ?? 'EXACT',
          minPrice: pg.minPrice ?? null,
          maxPrice: pg.maxPrice ?? null
        });

        // Sharing options
        this.sharingOptions.clear();
        (pg.sharingOptions ?? []).forEach(opt => {
          this.sharingOptions.push(this.fb.group({
            sharingType: [opt.sharingType, Validators.required],
            rent: [opt.rent, [Validators.required, Validators.min(1)]],
            availabilityCount: [opt.availabilityCount, [Validators.required, Validators.min(0)]]
          }));
        });

        // Images
        this.imageUrls.clear();
        (pg.images ?? []).forEach(img => {
          this.imageUrls.push(this.fb.control(img.imageUrl, [Validators.required]));
        });

        // Weekly menu
        if (pg.weeklyMenu) {
          for (const day of DAYS_OF_WEEK) {
            const items = pg.weeklyMenu[day] ?? [];
            this.weeklyMenuGroup.get(day)?.setValue(items.join(', '));
          }
        }

        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message ?? 'Failed to load PG for editing.';
        this.loading = false;
      }
    });
  }

  // ── Submit ──────────────────────────────────────────────────────────

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.errorMessage = null;
    this.successMessage = null;

    const val = this.form.value;

    // Build weeklyMenu map from comma-separated strings
    const weeklyMenu: Record<string, string[]> = {};
    let hasMenu = false;
    for (const day of DAYS_OF_WEEK) {
      const raw = (val.weeklyMenu?.[day] ?? '').trim();
      if (raw) {
        weeklyMenu[day] = raw.split(',').map((s: string) => s.trim()).filter((s: string) => s);
        hasMenu = true;
      }
    }

    // Build images array from URL strings
    const images = (val.imageUrls ?? [])
      .filter((url: string) => url?.trim())
      .map((url: string, idx: number) => ({ imageUrl: url.trim(), displayOrder: idx + 1 }));

    const body: Record<string, unknown> = {
      owner: {
        fullName: val.ownerFullName,
        email: val.ownerEmail,
        phoneNumber: val.ownerPhone || null
      },
      name: val.name,
      description: val.description || null,
      latitude: val.latitude,
      longitude: val.longitude,
      locationLabel: val.locationLabel,
      sharingOptions: val.sharingOptions,
      facilityNames: val.selectedFacilities?.length > 0 ? val.selectedFacilities : null,
      foodType: val.foodType || null,
      weeklyMenu: hasMenu ? weeklyMenu : null,
      nonVegDays: val.nonVegDays?.length > 0 ? val.nonVegDays : null,
      eggDays: val.eggDays?.length > 0 ? val.eggDays : null,
      priceType: val.priceType || 'EXACT',
      minPrice: val.priceType === 'RANGE' ? val.minPrice : null,
      maxPrice: val.priceType === 'RANGE' ? val.maxPrice : null,
      images: images.length > 0 ? images : null
    };

    const req$ = this.editMode
      ? this.pgService.updatePg(this.pgId!, body)
      : this.pgService.createPg(body);

    req$.subscribe({
      next: () => {
        this.submitting = false;
        this.successMessage = this.editMode
          ? 'PG updated successfully! Redirecting…'
          : 'PG created successfully! Redirecting to listings…';
        // Redirect to PG search (listings) after short delay
        setTimeout(() => this.router.navigate(['/pg-search']), 1500);
      },
      error: (err) => {
        this.submitting = false;
        this.errorMessage = err.error?.message ?? 'Failed to save PG. Please try again.';
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/pg-search']);
  }
}

