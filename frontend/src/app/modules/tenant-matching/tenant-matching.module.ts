import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { TenantMatchingRoutingModule } from './tenant-matching-routing.module';
import { TenantMatchingPageComponent } from './components/tenant-matching-page/tenant-matching-page.component';
import { TenantCreatePostComponent } from './components/tenant-create-post/tenant-create-post.component';
import { SharedComponentsModule } from '../../components/shared-components.module';

@NgModule({
  declarations: [TenantMatchingPageComponent, TenantCreatePostComponent],
  imports: [SharedComponentsModule, ReactiveFormsModule, TenantMatchingRoutingModule]
})
export class TenantMatchingModule {}

