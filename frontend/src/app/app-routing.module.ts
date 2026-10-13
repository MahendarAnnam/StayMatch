import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';

const routes: Routes = [
  { path: '', redirectTo: 'pg-search', pathMatch: 'full' },
  {
    path: 'login',
    loadChildren: () =>
      import('./modules/auth/auth.module').then((m) => m.AuthModule)
  },
  {
    path: 'pg-search',
    loadChildren: () =>
      import('./modules/pg-search/pg-search.module').then((m) => m.PgSearchModule)
  },
  {
    path: 'pg/new',
    canActivate: [AuthGuard],
    data: { roles: ['PG_OWNER', 'ADMIN'] },
    loadChildren: () =>
      import('./modules/pg-form/pg-form.module').then((m) => m.PgFormModule)
  },
  {
    path: 'pg/:id/edit',
    canActivate: [AuthGuard],
    data: { roles: ['PG_OWNER', 'ADMIN'] },
    loadChildren: () =>
      import('./modules/pg-form/pg-form.module').then((m) => m.PgFormModule)
  },
  {
    path: 'pg/:id',
    loadChildren: () =>
      import('./modules/pg-detail/pg-detail.module').then((m) => m.PgDetailModule)
  },
  {
    path: 'tenant-matching',
    loadChildren: () =>
      import('./modules/tenant-matching/tenant-matching.module').then((m) => m.TenantMatchingModule)
  },
  {
    path: 'roommate/:id',
    loadChildren: () =>
      import('./modules/roommate-detail/roommate-detail.module').then((m) => m.RoommateDetailModule)
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}

