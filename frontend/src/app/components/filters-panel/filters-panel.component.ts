import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';

/** Label-value pair for select options where display text differs from the value. */
export interface SelectOption {
  label: string;
  value: string;
}

export interface FilterField {
  key: string;
  label: string;
  type: 'text' | 'number' | 'select' | 'checkbox-group';
  options?: (string | SelectOption)[];   // plain strings or {label, value} pairs
  placeholder?: string;
}

/** Helper: resolve the display label from a plain string or SelectOption. */
function optionLabel(opt: string | SelectOption): string {
  return typeof opt === 'string' ? opt : opt.label;
}

/** Helper: resolve the value from a plain string or SelectOption. */
function optionValue(opt: string | SelectOption): string {
  return typeof opt === 'string' ? opt : opt.value;
}

@Component({
  selector: 'app-filters-panel',
  templateUrl: './filters-panel.component.html',
  styleUrls: ['./filters-panel.component.css']
})
export class FiltersPanelComponent implements OnInit {
  @Input() fields: FilterField[] = [];
  @Output() applyFilters = new EventEmitter<Record<string, unknown>>();

  form!: FormGroup;

  constructor(private readonly fb: FormBuilder) {}

  ngOnInit(): void {
    const group: Record<string, FormControl> = {};
    for (const field of this.fields) {
      if (field.type === 'checkbox-group') {
        // Value is a string[] of selected options
        group[field.key] = new FormControl([] as string[]);
      } else {
        group[field.key] = new FormControl('');
      }
    }
    this.form = new FormGroup(group);
  }

  /** Toggle a checkbox item in/out of the array value. */
  toggleCheckbox(fieldKey: string, option: string): void {
    const ctrl = this.form.get(fieldKey) as FormControl;
    const current: string[] = ctrl.value ?? [];
    const idx = current.indexOf(option);
    if (idx >= 0) {
      ctrl.setValue(current.filter((v) => v !== option));
    } else {
      ctrl.setValue([...current, option]);
    }
  }

  isChecked(fieldKey: string, option: string): boolean {
    const val: string[] = this.form.get(fieldKey)?.value ?? [];
    return val.includes(option);
  }

  onApply(): void {
    this.applyFilters.emit(this.form.value);
  }

  onReset(): void {
    // Reset each control to its proper empty default
    for (const field of this.fields) {
      const ctrl = this.form.get(field.key);
      ctrl?.setValue(field.type === 'checkbox-group' ? [] : '');
    }
    this.applyFilters.emit(this.form.value);
  }

  /** Get display label from a plain string or SelectOption. */
  getOptionLabel(opt: string | SelectOption): string {
    return optionLabel(opt);
  }

  /** Get value from a plain string or SelectOption. */
  getOptionValue(opt: string | SelectOption): string {
    return optionValue(opt);
  }
}

