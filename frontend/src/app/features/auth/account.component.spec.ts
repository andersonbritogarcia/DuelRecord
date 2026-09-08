import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AccountComponent } from './account.component';
import { Session, apiInterceptor } from '../../core/auth';
import { RUNTIME_CONFIG } from '../../core/runtime-config';

describe('AccountComponent', () => {
  let component: AccountComponent;
  let fixture: ComponentFixture<AccountComponent>;
  let http: HttpTestingController;

  const mockConfig = {
    apiBaseUrl: 'http://localhost:8080/api',
    googleClientId: '',
    requestTimeoutMs: 15000,
  };

  const mockCountries = [
    { code: 'BR', name: 'Brasil' },
    { code: 'US', name: 'Estados Unidos' },
    { code: 'FR', name: 'França' },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([apiInterceptor])),
        provideHttpClientTesting(),
        { provide: RUNTIME_CONFIG, useValue: mockConfig },
      ],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    const session = TestBed.inject(Session);
    session.accept(
      'mock-token',
      Date.now() + 60000,
      { id: 'acc-1', email: 'anderson@example.com', status: 'ACTIVE' },
      {
        id: 'ply-1',
        userId: 'acc-1',
        name: 'Anderson',
        displayName: 'Anderson BG',
        mtgoUsername: null,
        arenaUsername: null,
        city: null,
      },
    );

    fixture = TestBed.createComponent(AccountComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const countryReq = http.expectOne('http://localhost:8080/api/geo/countries');
    countryReq.flush(mockCountries);
    fixture.detectChanges();
  });

  afterEach(() => {
    http.verify();
    TestBed.inject(Session).clear();
  });

  it('loads and sorts countries on init', () => {
    expect(component.countries().length).toBe(3);
    expect(component.filteredCountries()[0].name).toBe('Brasil');
  });

  it('filters countries in real time when typing', () => {
    component.onCountryQueryChange('fran');
    expect(component.filteredCountries().length).toBe(1);
    expect(component.filteredCountries()[0].code).toBe('FR');

    component.onCountryQueryChange('br');
    expect(component.filteredCountries().length).toBe(1);
    expect(component.filteredCountries()[0].code).toBe('BR');
  });

  it('fetches cities when a country is selected', () => {
    component.onCountrySelect({ code: 'BR', name: 'Brasil' });
    fixture.detectChanges();

    const citiesReq = http.expectOne(
      (req) =>
        req.url === 'http://localhost:8080/api/geo/cities' &&
        req.params.get('country') === 'BR',
    );
    citiesReq.flush({
      content: [
        { id: 'city-1', name: 'São Paulo', country: { code: 'BR', name: 'Brasil' } },
        { id: 'city-2', name: 'Rio de Janeiro', country: { code: 'BR', name: 'Brasil' } },
      ],
    });

    expect(component.cities().length).toBe(2);
    expect(component.cities()[0].name).toBe('São Paulo');
  });

  it('saves updated profile with selected cityId', async () => {
    component.form.displayName = 'Anderson Champion';
    component.form.cityId = 'city-1';

    const savePromise = component.save();

    const saveReq = http.expectOne('http://localhost:8080/api/players/me');
    expect(saveReq.request.method).toBe('PUT');
    expect(saveReq.request.body).toEqual({
      displayName: 'Anderson Champion',
      mtgoUsername: null,
      arenaUsername: null,
      cityId: 'city-1',
    });

    saveReq.flush({
      id: 'ply-1',
      userId: 'acc-1',
      name: 'Anderson',
      displayName: 'Anderson Champion',
      mtgoUsername: null,
      arenaUsername: null,
      city: { id: 'city-1', name: 'São Paulo' },
    });

    await savePromise;
    expect(component.saved()).toBe(true);
  });
});
