import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { PgSearchRoutingModule } from './pg-search-routing.module';
import { PgSearchPageComponent } from './components/pg-search-page/pg-search-page.component';
import { SharedComponentsModule } from '../../components/shared-components.module';

@NgModule({
  declarations: [PgSearchPageComponent],
  imports: [SharedComponentsModule, RouterModule, PgSearchRoutingModule]
})
export class PgSearchModule {}

