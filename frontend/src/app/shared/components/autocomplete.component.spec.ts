import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AutocompleteComponent } from './autocomplete.component';

describe('AutocompleteComponent', () => {
  let component: AutocompleteComponent<{ id: string; name: string }>;
  let fixture: ComponentFixture<AutocompleteComponent<{ id: string; name: string }>>;

  const testOptions = [
    { id: '1', name: 'Brazil' },
    { id: '2', name: 'Belgium' },
    { id: '3', name: 'Canada' },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AutocompleteComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(
      AutocompleteComponent<{ id: string; name: string }>,
    );
    component = fixture.componentInstance;
    component.id = 'test-autocomplete';
    component.options = testOptions;
    component.displayFn = (item) => item.name;
    fixture.detectChanges();
  });

  it('renders input with placeholder and search icon', () => {
    component.placeholder = 'Search country';
    fixture.detectChanges();
    const input = fixture.nativeElement.querySelector('input');
    expect(input).toBeTruthy();
    expect(input.placeholder).toBe('Search country');
  });

  it('opens dropdown on focus and shows options', () => {
    const input = fixture.nativeElement.querySelector('input');
    input.dispatchEvent(new Event('focus'));
    fixture.detectChanges();

    expect(component.isOpen()).toBe(true);
    const options = fixture.nativeElement.querySelectorAll('.dr-autocomplete-option');
    expect(options.length).toBe(3);
    expect(options[0].textContent).toContain('Brazil');
  });

  it('highlights matching text when query is present', () => {
    component.query = 'Bra';
    component.openDropdown();
    fixture.detectChanges();

    const highlight = fixture.nativeElement.querySelector('.highlight');
    expect(highlight).toBeTruthy();
    expect(highlight.textContent).toBe('Bra');
  });

  it('navigates with arrow keys and selects with enter', () => {
    component.openDropdown();
    fixture.detectChanges();

    const selectedSpy = vi.fn();
    component.itemSelected.subscribe(selectedSpy);

    const input = fixture.nativeElement.querySelector('input');
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown' }));
    fixture.detectChanges();
    expect(component.activeIndex()).toBe(0);

    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown' }));
    fixture.detectChanges();
    expect(component.activeIndex()).toBe(1);

    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    fixture.detectChanges();
    expect(selectedSpy).toHaveBeenCalledWith(testOptions[1]);
    expect(component.isOpen()).toBe(false);
  });

  it('clears query and emits cleared event when clear button is clicked', () => {
    component.query = 'Bra';
    fixture.detectChanges();

    const clearedSpy = vi.fn();
    component.cleared.subscribe(clearedSpy);

    const clearBtn = fixture.nativeElement.querySelector('.dr-autocomplete-clear');
    expect(clearBtn).toBeTruthy();
    clearBtn.dispatchEvent(new MouseEvent('mousedown'));
    fixture.detectChanges();

    expect(component.query).toBe('');
    expect(clearedSpy).toHaveBeenCalled();
  });

  it('shows custom action when allowCustom is true and no matches exist', () => {
    component.allowCustom = true;
    component.customLabel = 'Add "Gotham"';
    component.options = [];
    component.query = 'Gotham';
    component.openDropdown();
    fixture.detectChanges();

    const customOpt = fixture.nativeElement.querySelector('.dr-autocomplete-custom');
    expect(customOpt).toBeTruthy();
    expect(customOpt.textContent).toContain('Add "Gotham"');

    const customSpy = vi.fn();
    component.customSelected.subscribe(customSpy);
    customOpt.dispatchEvent(new MouseEvent('mousedown'));
    expect(customSpy).toHaveBeenCalledWith('Gotham');
  });

  it('closes dropdown when escape is pressed', () => {
    component.openDropdown();
    fixture.detectChanges();
    expect(component.isOpen()).toBe(true);

    const input = fixture.nativeElement.querySelector('input');
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    fixture.detectChanges();
    expect(component.isOpen()).toBe(false);
  });
});
