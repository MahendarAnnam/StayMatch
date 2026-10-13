import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MaterialModule } from '../shared/material.module';
import { FiltersPanelComponent } from './filters-panel/filters-panel.component';
import { PgCardComponent } from './pg-card/pg-card.component';
import { TenantCardComponent } from './tenant-card/tenant-card.component';
import { LocationPickerComponent } from './location-picker/location-picker.component';

@NgModule({
  declarations: [FiltersPanelComponent, PgCardComponent, TenantCardComponent, LocationPickerComponent],
  imports: [CommonModule, ReactiveFormsModule, RouterModule, MaterialModule],
  exports: [FiltersPanelComponent, PgCardComponent, TenantCardComponent, LocationPickerComponent, CommonModule, MaterialModule]
})
export class SharedComponentsModule {}

