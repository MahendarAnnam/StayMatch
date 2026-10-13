import { NgModule } from '@angular/core';
import { PgFormRoutingModule } from './pg-form-routing.module';
import { PgFormPageComponent } from './components/pg-form-page/pg-form-page.component';
import { SharedComponentsModule } from '../../components/shared-components.module';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [PgFormPageComponent],
  imports: [SharedComponentsModule, ReactiveFormsModule, PgFormRoutingModule]
})
export class PgFormModule {}

