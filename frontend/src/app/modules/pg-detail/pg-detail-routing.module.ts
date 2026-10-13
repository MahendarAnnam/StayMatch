import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PgDetailPageComponent } from './components/pg-detail-page/pg-detail-page.component';

const routes: Routes = [{ path: '', component: PgDetailPageComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PgDetailRoutingModule {}

