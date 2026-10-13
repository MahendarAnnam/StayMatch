import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PgFormPageComponent } from './components/pg-form-page/pg-form-page.component';

const routes: Routes = [
  { path: '', component: PgFormPageComponent },
  { path: ':id/edit', component: PgFormPageComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PgFormRoutingModule {}

