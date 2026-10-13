import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PgSearchPageComponent } from './components/pg-search-page/pg-search-page.component';

const routes: Routes = [{ path: '', component: PgSearchPageComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PgSearchRoutingModule {}

