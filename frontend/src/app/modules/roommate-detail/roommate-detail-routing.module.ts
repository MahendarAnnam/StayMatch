import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RoommateDetailPageComponent } from './components/roommate-detail-page/roommate-detail-page.component';

const routes: Routes = [{ path: '', component: RoommateDetailPageComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RoommateDetailRoutingModule {}

