# Google Photos Sync - Synchronizacja zdjęć z Google Photos

Program do automatycznej synchronizacji wszystkich zdjęć i filmów z Twojego konta Google Photos na lokalny dysk.

## Funkcje

- Automatyczne pobieranie wszystkich zdjęć i filmów z Google Photos
- Organizacja plików według daty (rok/rok-miesiąc)
- Śledzenie pobranych plików (nie pobiera dwa razy tego samego)
- Pobieranie w pełnej jakości
- Wznawianie przerwanej synchronizacji
- Statystyki synchronizacji

## Wymagania

- Python 3.7 lub nowszy
- Konto Google z dostępem do Google Photos
- Dostęp do Google Cloud Console (do utworzenia aplikacji)

## Instalacja

### 1. Zainstaluj zależności

```bash
pip install -r requirements.txt
```

### 2. Skonfiguruj Google Cloud Project

1. Przejdź do [Google Cloud Console](https://console.cloud.google.com/)
2. Utwórz nowy projekt lub wybierz istniejący
3. Włącz Google Photos Library API:
   - W menu nawigacji wybierz "APIs & Services" > "Library"
   - Wyszukaj "Photos Library API"
   - Kliknij "Enable"

4. Utwórz dane uwierzytelniające OAuth 2.0:
   - Przejdź do "APIs & Services" > "Credentials"
   - Kliknij "Create Credentials" > "OAuth client ID"
   - Wybierz "Desktop app" jako typ aplikacji
   - Nadaj nazwę (np. "Google Photos Sync")
   - Kliknij "Create"

5. Pobierz plik JSON:
   - Po utworzeniu kliknij ikonę pobierania przy swoim kliencie OAuth
   - Zapisz plik jako `credentials.json` w katalogu z programem

### 3. Skonfiguruj OAuth Consent Screen

1. W Google Cloud Console przejdź do "APIs & Services" > "OAuth consent screen"
2. Wybierz "External" i kliknij "Create"
3. Wypełnij wymagane pola:
   - App name: "Google Photos Sync"
   - User support email: Twój email
   - Developer contact information: Twój email
4. Kliknij "Save and Continue"
5. W sekcji "Scopes" kliknij "Add or Remove Scopes"
6. Znajdź i dodaj scope: `https://www.googleapis.com/auth/photoslibrary.readonly`
7. Kliknij "Save and Continue"
8. W sekcji "Test users" dodaj swój adres email
9. Kliknij "Save and Continue"

## Użycie

### Podstawowa synchronizacja

Synchronizuj wszystkie zdjęcia do domyślnego katalogu `./google_photos`:

```bash
python google_photos_sync.py --sync
```

### Synchronizacja do niestandardowego katalogu

```bash
python google_photos_sync.py --dir /mnt/photos --sync
```

### Synchronizacja z limitem (np. pierwsze 100 zdjęć)

```bash
python google_photos_sync.py --sync --max 100
```

### Wyświetl statystyki

```bash
python google_photos_sync.py --stats
```

### Pomoc

```bash
python google_photos_sync.py --help
```

## Pierwsze uruchomienie

Przy pierwszym uruchomieniu:

1. Program otworzy przeglądarkę internetową
2. Zaloguj się na swoje konto Google
3. Przyznaj uprawnienia dostępu do Google Photos (tylko odczyt)
4. Program zapisze token autoryzacyjny w pliku `token.pickle`
5. Przy kolejnych uruchomieniach logowanie nie będzie potrzebne

## Struktura katalogów

Program organizuje pobrane pliki według daty utworzenia:

```
google_photos/
├── 2024/
│   ├── 2024-01/
│   │   ├── IMG_001.jpg
│   │   └── IMG_002.jpg
│   ├── 2024-02/
│   │   └── IMG_003.jpg
├── 2023/
│   └── 2023-12/
│       └── VID_001.mp4
└── .sync_state.json  # Stan synchronizacji
```

## Pliki konfiguracyjne

- `credentials.json` - Dane uwierzytelniające z Google Cloud Console (musisz pobrać)
- `token.pickle` - Token autoryzacyjny (tworzony automatycznie)
- `.sync_state.json` - Stan synchronizacji (tworzony automatycznie w katalogu docelowym)

## Uwagi

- Program pobiera zdjęcia i filmy w pełnej jakości (oryginalne pliki)
- Stan synchronizacji jest zapisywany co 10 pobranych plików
- Można bezpiecznie przerwać i wznowić synchronizację
- Pliki są organizowane według daty utworzenia zdjęcia, nie daty pobrania
- Program nie usuwa plików z Google Photos - tylko je pobiera

## Rozwiązywanie problemów

### Błąd: "credentials.json not found"

Upewnij się, że pobrałeś plik `credentials.json` z Google Cloud Console i umieściłeś go w katalogu z programem.

### Błąd: "Access blocked: This app's request is invalid"

Upewnij się, że:
1. Włączyłeś Photos Library API w Google Cloud Console
2. Skonfigurowałeś OAuth Consent Screen
3. Dodałeś swój email do listy użytkowników testowych

### Błąd: "The user did not consent to the scopes required"

Musisz zaakceptować uprawnienia dostępu do Google Photos podczas procesu autoryzacji.

### Program pobiera tylko część zdjęć

Sprawdź:
- Czy nie użyłeś parametru `--max`
- Czy masz stabilne połączenie internetowe
- Uruchom program ponownie - wznowi synchronizację od miejsca, w którym przerwał

## Bezpieczeństwo

- Plik `credentials.json` zawiera dane uwierzytelniające aplikacji (nie hasło)
- Plik `token.pickle` zawiera token dostępu - NIE UDOSTĘPNIAJ GO NIKOMU
- Program ma dostęp tylko do odczytu (nie może modyfikować ani usuwać zdjęć)
- Dodaj `credentials.json` i `token.pickle` do `.gitignore`

## Licencja

MIT License

## Wsparcie

W razie problemów utwórz issue na GitHubie lub skontaktuj się z autorem.
