import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TenantMatchingPageComponent } from './components/tenant-matching-page/tenant-matching-page.component';
import { TenantCreatePostComponent } from './components/tenant-create-post/tenant-create-post.component';

const routes: Routes = [
  { path: '', component: TenantMatchingPageComponent },
  { path: 'create', component: TenantCreatePostComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TenantMatchingRoutingModule {}

