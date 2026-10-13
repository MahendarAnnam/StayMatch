import { NgModule } from '@angular/core';
import { RoommateDetailRoutingModule } from './roommate-detail-routing.module';
import { RoommateDetailPageComponent } from './components/roommate-detail-page/roommate-detail-page.component';
import { SharedComponentsModule } from '../../components/shared-components.module';

@NgModule({
  declarations: [RoommateDetailPageComponent],
  imports: [SharedComponentsModule, RoommateDetailRoutingModule]
})
export class RoommateDetailModule {}

