import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  Output,
  computed,
  signal,
  ViewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';

export interface TextPart {
  text: string;
  match: boolean;
}

function normalizeStr(val: string): string {
  return (val || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase();
}

@Component({
  selector: 'app-autocomplete',
  standalone: true,
  imports: [FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="dr-autocomplete">
      @if (label) {
        <label [for]="id" class="dr-autocomplete-label">{{ label }}</label>
      }
      <div
        class="dr-autocomplete-box"
        [class.disabled]="disabled"
        [class.dr-open]="isOpen()"
      >
        <span class="dr-autocomplete-left-icon" aria-hidden="true">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"></circle>
            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
          </svg>
        </span>

        <input
          #inputEl
          [id]="id"
          type="text"
          role="combobox"
          autocomplete="off"
          autocorrect="off"
          spellcheck="false"
          class="dr-autocomplete-input"
          [placeholder]="placeholder"
          [disabled]="disabled"
          [ngModel]="query"
          [ngModelOptions]="{ standalone: true }"
          (ngModelChange)="onInputChange($event)"
          (focus)="onFocus()"
          (keydown)="onKeyDown($event)"
          [attr.aria-expanded]="isOpen()"
          [attr.aria-controls]="id + '-listbox'"
          [attr.aria-activedescendant]="activeDescendantId()"
        />

        <div class="dr-autocomplete-actions">
          @if (loading) {
            <span class="dr-autocomplete-loading" aria-hidden="true">
              <svg class="dr-autocomplete-spinner" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-opacity="0.2"></circle>
                <path d="M12 3a9 9 0 0 1 9 9" stroke="currentColor" stroke-linecap="round"></path>
              </svg>
            </span>
          }

          @if (query && !disabled) {
            <button
              type="button"
              class="dr-autocomplete-clear"
              (mousedown)="onClear($event)"
              [attr.aria-label]="clearAriaLabel || 'Limpar'"
              tabindex="-1"
            >
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          }

          <button
            type="button"
            class="dr-autocomplete-chevron"
            [class.rotated]="isOpen()"
            (mousedown)="onToggleChevron($event)"
            [disabled]="disabled"
            tabindex="-1"
            aria-hidden="true"
          >
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
          </button>
        </div>
      </div>

      @if (isOpen() && !disabled) {
        <div
          class="dr-autocomplete-dropdown"
          role="listbox"
          [id]="id + '-listbox'"
          (mousedown)="$event.preventDefault()"
        >
          @if (loading && options.length === 0) {
            <div class="dr-autocomplete-status">
              <svg class="dr-autocomplete-spinner" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-opacity="0.2"></circle>
                <path d="M12 3a9 9 0 0 1 9 9" stroke="currentColor" stroke-linecap="round"></path>
              </svg>
              <span>{{ loadingText || 'Buscando…' }}</span>
            </div>
          } @else if (options.length === 0 && query.trim()) {
            @if (allowCustom) {
              <div
                class="dr-autocomplete-custom"
                [class.active]="activeIndex() === 0"
                [id]="id + '-opt-custom'"
                role="option"
                (mousedown)="onCustomClick($event)"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="12" y1="5" x2="12" y2="19"></line>
                  <line x1="5" y1="12" x2="19" y2="12"></line>
                </svg>
                <span>{{ customLabel || ('+ ' + query.trim()) }}</span>
              </div>
            } @else {
              <div class="dr-autocomplete-empty">
                <span>{{ emptyText || 'Nenhum resultado encontrado' }}</span>
              </div>
            }
          } @else {
            @for (item of options; track trackItem($index, item)) {
              <div
                class="dr-autocomplete-option"
                [id]="id + '-opt-' + $index"
                role="option"
                [attr.aria-selected]="isItemSelected(item)"
                [class.active]="activeIndex() === $index"
                [class.selected]="isItemSelected(item)"
                (mouseenter)="activeIndex.set($index)"
                (mousedown)="onOptionClick(item, $event)"
              >
                <div class="dr-autocomplete-option-left">
                  @if (getItemThumbnail(item); as thumb) {
                    <img [src]="thumb" alt="" class="dr-autocomplete-thumb" loading="lazy" />
                  }
                  <div class="dr-autocomplete-option-content">
                    <div class="dr-autocomplete-option-text">
                      @for (part of getHighlightParts(getItemDisplay(item), query); track $index) {
                        @if (part.match) {
                          <strong class="highlight">{{ part.text }}</strong>
                        } @else {
                          <span>{{ part.text }}</span>
                        }
                      }
                    </div>
                    @if (getItemSubtitle(item); as sub) {
                      <span class="dr-autocomplete-subtitle">{{ sub }}</span>
                    }
                  </div>
                </div>

                <div class="dr-autocomplete-option-meta">
                  @if (getItemBadge(item); as badge) {
                    <span class="dr-autocomplete-badge">{{ badge }}</span>
                  }
                  @if (isItemSelected(item)) {
                    <svg class="dr-autocomplete-check" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                  }
                </div>
              </div>
            }
          }
        </div>
      }
    </div>
  `,
})
export class AutocompleteComponent<T = any> {
  @Input({ required: true }) id!: string;
  @Input() label = '';
  @Input() placeholder = '';
  @Input() query = '';
  @Input() options: T[] = [];
  @Input() loading = false;
  @Input() disabled = false;
  @Input() allowCustom = false;
  @Input() customLabel = '';
  @Input() emptyText = '';
  @Input() loadingText = '';
  @Input() clearAriaLabel = '';
  @Input() selectedValue: any = null;
  @Input() displayFn: (item: T) => string = (item) => String(item);
  @Input() subtitleFn?: (item: T) => string;
  @Input() thumbnailFn?: (item: T) => string;
  @Input() badgeFn?: (item: T) => string;
  @Input() trackByFn?: (item: T) => any;
  @Input() isSelectedFn?: (item: T) => boolean;

  @Output() readonly queryChange = new EventEmitter<string>();
  @Output() readonly itemSelected = new EventEmitter<T>();
  @Output() readonly customSelected = new EventEmitter<string>();
  @Output() readonly cleared = new EventEmitter<void>();
  @Output() readonly opened = new EventEmitter<void>();

  @ViewChild('inputEl') inputEl?: ElementRef<HTMLInputElement>;

  readonly isOpen = signal(false);
  readonly activeIndex = signal(-1);

  readonly activeDescendantId = computed(() => {
    const idx = this.activeIndex();
    if (!this.isOpen() || idx < 0) return null;
    if (this.options.length === 0 && this.allowCustom) return `${this.id}-opt-custom`;
    return `${this.id}-opt-${idx}`;
  });

  constructor(private readonly elementRef: ElementRef) {}

  @HostListener('document:pointerdown', ['$event'])
  onDocumentPointerDown(event: PointerEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target as Node)) {
      this.close();
    }
  }

  onFocus(): void {
    if (this.disabled) return;
    this.openDropdown();
  }

  onToggleChevron(event: MouseEvent): void {
    event.preventDefault();
    if (this.disabled) return;
    if (this.isOpen()) {
      this.close();
    } else {
      this.openDropdown();
      this.inputEl?.nativeElement.focus();
    }
  }

  openDropdown(): void {
    if (this.isOpen()) return;
    this.isOpen.set(true);
    this.activeIndex.set(-1);
    this.opened.emit();
  }

  close(): void {
    this.isOpen.set(false);
    this.activeIndex.set(-1);
  }

  onInputChange(val: string): void {
    this.query = val;
    this.queryChange.emit(val);
    if (!this.isOpen() && !this.disabled) {
      this.openDropdown();
    }
    this.activeIndex.set(0);
  }

  onClear(event: MouseEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.query = '';
    this.queryChange.emit('');
    this.cleared.emit();
    this.activeIndex.set(-1);
    this.inputEl?.nativeElement.focus();
    if (!this.isOpen()) {
      this.openDropdown();
    }
  }

  onOptionClick(item: T, event: MouseEvent): void {
    event.preventDefault();
    this.selectItem(item);
  }

  onCustomClick(event: MouseEvent): void {
    event.preventDefault();
    const q = this.query.trim();
    if (q) {
      this.customSelected.emit(q);
      this.close();
    }
  }

  selectItem(item: T): void {
    this.itemSelected.emit(item);
    this.close();
  }

  onKeyDown(event: KeyboardEvent): void {
    if (this.disabled) return;

    if (event.key === 'Escape') {
      this.close();
      return;
    }

    if (!this.isOpen()) {
      if (event.key === 'ArrowDown' || event.key === 'ArrowUp' || event.key === 'Enter') {
        this.openDropdown();
        event.preventDefault();
      }
      return;
    }

    const total = this.options.length;
    const hasCustom = total === 0 && this.allowCustom && this.query.trim().length > 0;

    if (event.key === 'ArrowDown') {
      event.preventDefault();
      if (hasCustom) {
        this.activeIndex.set(0);
      } else if (total > 0) {
        const next = (this.activeIndex() + 1) % total;
        this.activeIndex.set(next);
      }
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      if (hasCustom) {
        this.activeIndex.set(0);
      } else if (total > 0) {
        const prev = this.activeIndex() <= 0 ? total - 1 : this.activeIndex() - 1;
        this.activeIndex.set(prev);
      }
    } else if (event.key === 'Enter') {
      event.preventDefault();
      const idx = this.activeIndex();
      if (hasCustom && (idx === 0 || idx === -1)) {
        this.onCustomClick(event as unknown as MouseEvent);
      } else if (idx >= 0 && idx < total) {
        this.selectItem(this.options[idx]);
      }
    }
  }

  getItemDisplay(item: T): string {
    return this.displayFn ? this.displayFn(item) : String(item);
  }

  getItemSubtitle(item: T): string | null {
    return this.subtitleFn ? this.subtitleFn(item) : null;
  }

  getItemThumbnail(item: T): string | null {
    return this.thumbnailFn ? this.thumbnailFn(item) : null;
  }

  getItemBadge(item: T): string | null {
    return this.badgeFn ? this.badgeFn(item) : null;
  }

  trackItem(index: number, item: T): any {
    if (this.trackByFn) return this.trackByFn(item);
    return index;
  }

  isItemSelected(item: T): boolean {
    if (this.isSelectedFn) return this.isSelectedFn(item);
    return false;
  }

  getHighlightParts(text: string, rawQuery: string): TextPart[] {
    if (!text || !rawQuery) return [{ text, match: false }];
    const normText = normalizeStr(text);
    const normQuery = normalizeStr(rawQuery.trim());
    if (!normQuery) return [{ text, match: false }];

    const idx = normText.indexOf(normQuery);
    if (idx === -1) return [{ text, match: false }];

    const parts: TextPart[] = [];
    if (idx > 0) {
      parts.push({ text: text.slice(0, idx), match: false });
    }
    parts.push({ text: text.slice(idx, idx + normQuery.length), match: true });
    if (idx + normQuery.length < text.length) {
      parts.push({ text: text.slice(idx + normQuery.length), match: false });
    }
    return parts;
  }
}
