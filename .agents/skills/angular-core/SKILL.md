---
name: angular-core
description: >
  Angular core patterns: standalone components, signals, inject, control flow, zoneless.
  Trigger: When creating Angular components, using signals, or setting up zoneless.
metadata:
  author: gentleman-programming
  version: "1.0"
---

## Standalone Components (REQUIRED)

Components are standalone by default. Do NOT set `standalone: true`.

```typescript
@Component({
  selector: 'app-user',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `...`
})
export class UserComponent {}
```

---

## Input/Output Functions (REQUIRED)

```typescript
// ✅ ALWAYS: Function-based
readonly user = input.required<User>();
readonly disabled = input(false);
readonly selected = output<User>();
readonly checked = model(false);  // Two-way binding

// ❌ NEVER: Decorators
@Input() user: User;
@Output() selected = new EventEmitter<User>();
```

---

## Signals for State (REQUIRED)

```typescript
readonly count = signal(0);
readonly doubled = computed(() => this.count() * 2);

// Update
this.count.set(5);
this.count.update(prev => prev + 1);

// Side effects
effect(() => localStorage.setItem('count', this.count().toString()));
```

---

## NO Lifecycle Hooks (REQUIRED)

Signals replace lifecycle hooks. Do NOT use `ngOnInit`, `ngOnChanges`, `ngOnDestroy`.

```typescript
// ❌ NEVER: Lifecycle hooks
ngOnInit() {
  this.loadUser();
}

ngOnChanges(changes: SimpleChanges) {
  if (changes['userId']) {
    this.loadUser();
  }
}

// ✅ ALWAYS: Signals + effect
readonly userId = input.required<string>();
readonly user = signal<User | null>(null);

private userEffect = effect(() => {
  // Runs automatically when userId() changes
  this.loadUser(this.userId());
});

// ✅ For derived data, use computed
readonly displayName = computed(() => this.user()?.name ?? 'Guest');
```

### When to Use What

| Need | Use |
|------|-----|
| React to input changes | `effect()` watching the input signal |
| Derived/computed state | `computed()` |
| Side effects (API calls, localStorage) | `effect()` |
| Cleanup on destroy | `DestroyRef` + `inject()` |

```typescript
// Cleanup example
private readonly destroyRef = inject(DestroyRef);

constructor() {
  const subscription = someObservable$.subscribe();
  this.destroyRef.onDestroy(() => subscription.unsubscribe());
}
```

---

## inject() Over Constructor (REQUIRED)

```typescript
// ✅ ALWAYS
private readonly http = inject(HttpClient);

// ❌ NEVER
constructor(private http: HttpClient) {}\n```

---

## Native Control Flow (REQUIRED)

```html
@if (loading()) {
  <spinner />
} @else {
  @for (item of items(); track item.id) {
    <item-card [data]="item" />
  } @empty {
    <p>No items</p>\n  }\n}\n\n@switch (status()) {\n  @case ('active') {\n    <span>Active</span>\n  }\n  @default {\n    <span>Unknown</span>\n  }\n}\n```\n\n---\n\n## RxJS - Only When Needed\n\nSignals are the default. Use RxJS ONLY for complex async operations.\n\n| Use Signals | Use RxJS |\n|-------------|----------|\n| Component state | Combining multiple streams |\n| Derived values | Debounce/throttle |\n| Simple async (single API call) | Race conditions |\n| Input/Output | WebSockets, real-time |\n| | Complex error retry logic |\n\n```typescript\n// ✅ Simple API call - use signals\nreadonly user = signal<User | null>(null);\nreadonly loading = signal(false);\n\nasync loadUser(id: string) {\n  this.loading.set(true);\n  this.user.set(await firstValueFrom(this.http.get<User>(`/api/users/${id}`)));\n  this.loading.set(false);\n}\n\n// ✅ Complex stream - use RxJS\nreadonly searchResults$ = this.searchTerm$.pipe(\n  debounceTime(300),\n  distinctUntilChanged(),\n  switchMap(term => this.http.get<Results>(`/api/search?q=${term}`))\n);\n\n// Convert to signal when needed in template\nreadonly searchResults = toSignal(this.searchResults$, { initialValue: [] });\n```\n\n---\n\n## Zoneless Angular (REQUIRED)\n\nAngular is zoneless. Use `provideZonelessChangeDetection()`.\n\n```typescript\nbootstrapApplication(AppComponent, {\n  providers: [provideZonelessChangeDetection()]\n});\n```\n\nRemove ZoneJS:\n```bash\nnpm uninstall zone.js\n```\n\nRemove from `angular.json` polyfills: `zone.js` and `zone.js/testing`.\n\n### Zoneless Requirements\n- Use `OnPush` change detection\n- Use signals for state (auto-notifies Angular)\n- Use `AsyncPipe` for observables\n- Use `markForCheck()` when needed\n\n---\n\n## Resources\n\n- https://angular.dev/guide/signals\n- https://angular.dev/guide/templates/control-flow\n- https://angular.dev/guide/zoneless\n
