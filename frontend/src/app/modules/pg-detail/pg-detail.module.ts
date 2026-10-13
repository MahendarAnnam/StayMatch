import { NgModule } from '@angular/core';
import { PgDetailRoutingModule } from './pg-detail-routing.module';
import { PgDetailPageComponent } from './components/pg-detail-page/pg-detail-page.component';
import { SharedComponentsModule } from '../../components/shared-components.module';

@NgModule({
  declarations: [PgDetailPageComponent],
  imports: [SharedComponentsModule, PgDetailRoutingModule]
})
export class PgDetailModule {}

